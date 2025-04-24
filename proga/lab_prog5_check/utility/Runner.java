package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.NoSuchElementException;
import java.nio.file.*;

import managers.CommandManager;
import utility.Console;

public class Runner {
	private Console console;
	private final CommandManager commandManager;
	private final Set<String> scriptStack = new HashSet<>();
	private int lengthRecursion = -1;

	public Runner(Console console, CommandManager commandManager) {
		this.console = console;
		this.commandManager = commandManager;
	}

	/**
	 * Интерактивный режим
	 */
	public void interactiveMode() {
		try {
			ExecutionResponse commandStatus;
			String[] userCommand = {"", ""};
			
			while (true) {
				console.prompt();
				userCommand = (console.readln().trim() + " ").split(" ", 2);
				userCommand[1] = userCommand[1].trim();
				
				commandManager.addToHistory(userCommand[0]);
				commandStatus = launchCommand(userCommand);
				
				if (commandStatus.getMassage().equals("exit")) break;
				console.println(commandStatus.getMassage());
			}
		} catch (NoSuchElementException exception) {
			console.printError("Пользовательский ввод не обнаружен!");
		} catch (IllegalStateException exception) {
			console.printError("Непредвиденная ошибка!");
		}
	}

	/**
	 * Проверяет рекурсивность выполнения скриптов.
	 * @param argument Название запускаемого скрипта
	 * @return можно ли выполнять скрипт.
	 */
	private boolean checkRecursion(String argument, BufferedReader scriptReader) {
		if (scriptStack.contains(argument)) {
			if (lengthRecursion < 0) {
				console.selectConsoleReader();
				console.println("Была замечена рекурсия! Введите максимальную глубину рекурсии (0..500)");
				while (lengthRecursion < 0 || lengthRecursion > 500) {
					try { 
						console.print("> "); 
						lengthRecursion = Integer.parseInt(console.readln().trim()); 
					} catch (NumberFormatException e) { 
						console.println("длина не распознана"); 
					}
				}
				console.selectFileReader(scriptReader);
			}
			
			// Since HashSet doesn't maintain order, we'll use a fixed limit based on the lengthRecursion value
			if (scriptStack.size() > lengthRecursion || scriptStack.size() > 500) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Режим для запуска скрипта.
	 * @param argument Аргумент скрипта
	 * @return Код завершения.
	 */
	private ExecutionResponse scriptMode(String argument) {
		String[] userCommand = {"", ""};
		StringBuilder executionOutput = new StringBuilder();
		
		if (!new File(argument).exists()) return new ExecutionResponse(false, "Файл не существет!");
		if (!Files.isReadable(Paths.get(argument))) return new ExecutionResponse(false, "Прав для чтения нет!");
		
		scriptStack.add(argument);
		try (BufferedReader scriptReader = new BufferedReader(new FileReader(argument))) {
			
			ExecutionResponse commandStatus;
			
			String line = scriptReader.readLine();
			if (line == null) throw new NoSuchElementException();
			
			console.selectFileReader(scriptReader);
			do {
				userCommand = (console.readln().trim() + " ").split(" ", 2);
				userCommand[1] = userCommand[1].trim();
				while (console.isCanReadln() && userCommand[0].isEmpty()) {
					userCommand = (console.readln().trim() + " ").split(" ", 2);
					userCommand[1] = userCommand[1].trim();
				}
				executionOutput.append(console.getPrompt() + String.join(" ", userCommand) + "\n");
				var needLaunch = true;
				if (userCommand[0].equals("execute_script")) {
					needLaunch = checkRecursion(userCommand[1], scriptReader);
				}
				
				commandStatus = needLaunch ? launchCommand(userCommand) : new ExecutionResponse("Превышена максимальная глубина рекурсии");
				if (userCommand[0].equals("execute_script")) console.selectFileReader(scriptReader);
				executionOutput.append(commandStatus.getMassage()+"\n");
			} while (commandStatus.getExitCode() && !commandStatus.getMassage().equals("exit") && console.isCanReadln());
			
			console.selectConsoleReader();
			if (!commandStatus.getExitCode() && !(userCommand[0].equals("execute_script") && !userCommand[1].isEmpty())) {
				executionOutput.append("Проверьте скрипт на корректность введенных данных!\n");
			}
			
			return new ExecutionResponse(commandStatus.getExitCode(), executionOutput.toString());
		} catch (FileNotFoundException exception) {
			return new ExecutionResponse(false, "Файл со скриптом не найден!");
		} catch (NoSuchElementException exception) {
			return new ExecutionResponse(false, "Файл со скриптом пуст!");
		} catch (IOException exception) {
			return new ExecutionResponse(false, "Ошибка чтения из файла!");
		} catch (IllegalStateException exception) {
			console.printError("Непредвиденная ошибка!");
			System.exit(0);
		} finally {
			scriptStack.remove(argument);
		}
		return new ExecutionResponse("");
	}

	/**
	 * Launchs the command.
	 * @param userCommand Команда для запуска
	 * @return Код завершения.
	 */
	private ExecutionResponse launchCommand(String[] userCommand) {
		if (userCommand[0].equals("")) return new ExecutionResponse("");
		var command = commandManager.getCommands().get(userCommand[0]);
		
		if (command == null) return new ExecutionResponse(false, "Команда '" + userCommand[0] + "' не найдена. Наберите 'help' для справки");
		
		switch (userCommand[0]) {
			case "execute_script" -> {
				ExecutionResponse tmp = commandManager.getCommands().get("execute_script").apply(userCommand);
				if (!tmp.getExitCode()) return tmp;
				ExecutionResponse tmp2 = scriptMode(userCommand[1]);
				return new ExecutionResponse(tmp2.getExitCode(), tmp.getMassage()+"\n"+tmp2.getMassage().trim());
			}
			default -> { return command.apply(userCommand); }
		}
	}
}