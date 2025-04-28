package utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.nio.file.*;

import managers.CommandManager;
import models.Ask;
import utility.Console;

public class Runner {
	private Console console;
	private final CommandManager commandManager;
	private final List<String> scriptStack = new ArrayList<>();
	private Scanner userScanner;
	private Scanner scriptScanner;
	private int recursionDepth = -1;

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
				
				if (commandStatus.getMessage().equals("exit")) break;
				console.println(commandStatus.getMessage());
			}
		} catch (NoSuchElementException exception) {
			console.printError("Пользовательский ввод не обнаружен!");
		} catch (IllegalStateException exception) {
			console.printError("Непредвиденная ошибка!");
		}
	}

	/**
	 * Запускает выполнение команд из файла скрипта.
	 *
	 * @param argument путь к файлу скрипта
	 * @return результат выполнения скрипта
	 */
	public ExecutionResponse scriptMode(String argument) {
		if (!new File(argument).exists()) {
			return new ExecutionResponse(false, "Файл не существует: " + argument);
		}

		if (scriptStack.contains(argument)) {
			return new ExecutionResponse(false, "Обнаружена рекурсия! Скрипт " + argument + " уже выполняется.");
		}

		scriptStack.add(argument);
		try {
			scriptScanner = new Scanner(new File(argument));
			Ask.setScanner(scriptScanner); // Устанавливаем сканер для чтения из файла

			StringBuilder fullOutput = new StringBuilder();

			while (scriptScanner.hasNextLine()) {
				String line = scriptScanner.nextLine().trim();

				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String[] command = (line + " ").split(" ", 2);
				command[1] = command[1].trim();

				ExecutionResponse response = launchCommand(command);
				fullOutput.append(response.getMessage()).append("\n");

				if (response.getMessage().equals("exit")) {
					break;
				}
			}
			return new ExecutionResponse(fullOutput.toString());
		} catch (FileNotFoundException e) {
			return new ExecutionResponse(false, "Файл скрипта не найден: " + argument);
		} catch (Exception e) {
			return new ExecutionResponse(false, "Ошибка выполнения скрипта: " + e.getMessage());
		} finally {
			scriptStack.remove(argument);
			if (scriptStack.isEmpty()) {
				Ask.setScanner(userScanner); // Возвращаем сканер для ручного ввода
				scriptScanner = null;
			}
		}
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
				return new ExecutionResponse(tmp2.getExitCode(), tmp.getMessage()+"\n"+tmp2.getMessage().trim());
			}
			default -> { return command.apply(userCommand); }
		}
	}
}