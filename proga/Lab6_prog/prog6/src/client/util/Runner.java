package client.util;

import client.commands.Command;
import client.managers.CommandManager;
import client.network.ClientNetworkIO;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Runner {
	private static final Logger logger = LogManager.getLogger(Runner.class);
	private final StandardConsole console;
	private final CommandManager commandManager;
	private final ClientNetworkIO networkIO;
	private final List<String> scriptStack = new ArrayList<>();
	// private final Scanner consoleScanner; // Больше не нужно хранить здесь

	public Runner(StandardConsole console, CommandManager commandManager, ClientNetworkIO networkIO) {
		this.console = console;
		this.commandManager = commandManager;
		this.networkIO = networkIO;
		// Ask инициализируется своим собственным Scanner'ом при загрузке класса
		// Ask.setScanner(this.consoleScanner, false); // Больше не нужно
	}

	/**
	 * Интерактивный режим: Читает команды из консоли.
	 */
	public void interactiveMode() {
		logger.info("Вход в интерактивный режим...");
		// Убедимся, что Ask использует консольный сканер
		Ask.setScanner(null, false); // Передаем null, чтобы Ask использовал свой systemInScanner
		try {
			ExecutionResponse commandStatus;
			String[] userCommand = {"", ""};

			while (true) {
				console.prompt();
				try {
					// Используем Ask.readLine() для согласованности и обработки 'exit'
					// String line = Ask.readLine(); // Альтернатива, если StandardConsole.readln() ненадежна
					String line = console.readln(); // Продолжаем использовать console.readln()
					userCommand = (line.trim() + " ").split(" ", 2);
					userCommand[1] = userCommand[1].trim();
				} catch (NoSuchElementException e) {
					logger.warn("Поток ввода закрыт (Ctrl+D?). Выход.");
					break; // Выходим из цикла, если ввод закончился
				} catch (IllegalStateException e) {
					logger.error("Ридер консоли в недопустимом состоянии. Выход.", e);
					break;
				} /*catch (Ask.AskBreak e) { // Если используем Ask.readLine()
                    logger.info("Пользователь ввел 'exit'. Завершение клиента.");
                    break;
                }*/

				if (userCommand[0].isEmpty()) {
					continue; // Пропускаем пустой ввод
				}

				commandManager.addToHistory(userCommand[0]);
				commandStatus = launchCommand(userCommand);

				// Специальная обработка команды Exit
				if (commandStatus != null && "exit".equals(commandStatus.getMessage()) && commandStatus.getExitCode()) {
					logger.info("Команда Exit успешна. Завершение клиента.");
					break; // Выходим из цикла
				}

				// Выводим результат, если не null
				if (commandStatus != null) {
					if (commandStatus.getExitCode()){
						console.println(commandStatus.getMessage());
					} else {
						console.printError(commandStatus.getMessage()); // Выводим ошибки в stderr
					}
				} else {
					console.printError("Получен null ответ, выполнение команды могло завершиться с внутренней ошибкой.");
				}
			}
		} catch (Exception e) { // Ловим более широкие исключения в главном цикле
			logger.fatal("Необработанное исключение в интерактивном режиме: {}", e.getMessage(), e);
			console.printError("Произошла непредвиденная ошибка. Проверьте логи. Выход.");
		} finally {
			logger.info("Выход из интерактивного режима.");
		}
	}

	/**
	 * Режим скрипта: Выполняет команды из файла.
	 * @param filename Путь к файлу скрипта.
	 * @return ExecutionResponse, указывающий результат выполнения скрипта.
	 */
	public ExecutionResponse scriptMode(String filename) {
		File scriptFile = new File(filename);
		if (!scriptFile.exists()) {
			logger.error("Файл скрипта не найден: {}", filename);
			return new ExecutionResponse(false, "Файл скрипта не существует: " + filename);
		}
		if (!scriptFile.canRead()) {
			logger.error("Файл скрипта не может быть прочитан (права доступа): {}", filename);
			return new ExecutionResponse(false, "Нет прав на чтение файла скрипта: " + filename);
		}

		String absolutePath = scriptFile.getAbsolutePath(); // Используем абсолютный путь для проверки рекурсии
		if (scriptStack.contains(absolutePath)) {
			logger.error("Обнаружена рекурсия! Скрипт '{}' уже выполняется.", absolutePath);
			return new ExecutionResponse(false, "Обнаружена рекурсия! Скрипт '" + filename + "' уже выполняется.");
		}

		scriptStack.add(absolutePath);
		logger.info("Вход в режим скрипта: {}", filename);
		StringBuilder scriptOutput = new StringBuilder();
		boolean scriptOk = true;

		Object savedAskState = Ask.saveState(); // Сохраняем состояние Ask ПЕРЕД try-with-resources

		try (Scanner fileScanner = new Scanner(scriptFile)) {
			Ask.setScanner(fileScanner, true); // Переключаем Ask на чтение из файла скрипта

			while (fileScanner.hasNextLine()) { // Безопасная проверка наличия следующей строки
				String line = "";
				try {
					// Читаем строку внутри цикла, чтобы обработать NoSuchElementException, если файл закончился неожиданно
					line = fileScanner.nextLine().trim();
					scriptOutput.append(console.getPrompt()).append(line).append("\n"); // Эхо команды

					if (line.isEmpty() || line.startsWith("#")) {
						continue; // Пропускаем пустые строки и комментарии
					}

					String[] scriptCommand = (line + " ").split(" ", 2);
					scriptCommand[1] = scriptCommand[1].trim();

					logger.debug("Выполнение команды скрипта: [{}] с аргументами [{}]", scriptCommand[0], scriptCommand[1]);
					commandManager.addToHistory(scriptCommand[0]); // Добавляем команду скрипта в историю
					ExecutionResponse response = launchCommand(scriptCommand);


					if (response == null) {
						scriptOutput.append("Ошибка: Выполнение команды завершилось с внутренней ошибкой (null ответ).\n");
						logger.error("Команда [{}] в скрипте [{}] завершилась с внутренней ошибкой.", scriptCommand[0], filename);
						scriptOk = false;
						break; // Останавливаем скрипт при серьезной ошибке
					} else if ("exit".equals(response.getMessage()) && response.getExitCode()) {
						scriptOutput.append("В скрипте встречена команда Exit. Завершение клиента.\n");
						logger.info("Команда Exit в скрипте '{}'. Завершение.", filename);
						// ВАЖНО: Восстанавливаем состояние ПЕРЕД возвратом сигнала exit
						Ask.restoreState(savedAskState);
						scriptStack.remove(absolutePath);
						return response; // Позволяем вызывающему коду обработать exit
					} else if (!response.getExitCode()) {
						scriptOutput.append("Ошибка: ").append(response.getMessage()).append("\n");
						logger.warn("Команда [{}] в скрипте [{}] не удалась: {}", scriptCommand[0], filename, response.getMessage());
						scriptOk = false;
						// Решите, останавливать ли скрипт при ошибке команды (опционально)
						// break;
					} else {
						scriptOutput.append(response.getMessage()).append("\n");
					}
					// Добавьте небольшую задержку при необходимости, например, Thread.sleep(10);

				} catch (NoSuchElementException e){
					// Это нормальное завершение скрипта, если файл закончился
					logger.debug("Достигнут конец файла скрипта {}", filename);
					break; // Выходим из цикла while
				} catch (Exception e) { // Ловим другие ошибки чтения или выполнения команды
					logger.error("Ошибка при обработке строки '{}' в скрипте {}: {}", line, filename, e.getMessage(), e);
					scriptOk = false;
					scriptOutput.append("Критическая ошибка во время выполнения скрипта: ").append(e.getMessage()).append("\n");
					break; // Останавливаем скрипт
				}
			}

		} catch (FileNotFoundException e) {
			// Должно быть поймано раньше, но обрабатываем для надежности
			logger.error("Файл скрипта не найден во время выполнения: {}", filename, e);
			scriptOk = false; // Устанавливаем флаг ошибки
			scriptOutput.append("Ошибка: Файл скрипта не найден: ").append(filename).append("\n");
			// Не возвращаем здесь, чтобы finally выполнился
		} catch (Exception e) { // Ловим другие ошибки, например, при открытии Scanner'а
			logger.error("Ошибка при выполнении скрипта {}: {}", filename, e.getMessage(), e);
			scriptOk = false;
			scriptOutput.append("Критическая ошибка во время выполнения скрипта: ").append(e.getMessage()).append("\n");
		} finally {
			// ОЧЕНЬ ВАЖНО: Восстанавливаем предыдущее состояние Ask
			Ask.restoreState(savedAskState);
			scriptStack.remove(absolutePath);
			logger.info("Выход из режима скрипта: {}", filename);
		}

		return new ExecutionResponse(scriptOk, scriptOutput.toString().trim());
	}


	/**
	 * Находит и запускает команду.
	 * @param userCommand Массив с именем команды и аргументами.
	 * @return ExecutionResponse от метода apply команды или ответ об ошибке.
	 */
	private ExecutionResponse launchCommand(String[] userCommand) {
		String commandName = userCommand[0];
		if (commandName.isEmpty()) {
			return new ExecutionResponse(true, ""); // Пустая команда валидна, ничего не делает
		}

		Command command = commandManager.getCommands().get(commandName);

		if (command == null) {
			logger.warn("Команда '{}' не найдена.", commandName);
			return new ExecutionResponse(false, "Команда '" + commandName + "' не найдена. Наберите 'help' для справки.");
		}

		logger.debug("Запуск команды: {}", commandName);
		try {
			// Выполняем метод apply команды на стороне клиента
			// Этот метод обрабатывает Ask, создание Request, сетевой ввод-вывод и обработку Response
			return command.apply(userCommand);
		} catch (Exception e) {
			logger.error("Исключение во время выполнения команды '{}': {}", commandName, e.getMessage(), e);
			return new ExecutionResponse(false, "Ошибка при выполнении команды '" + commandName + "': " + e.getMessage());
		}
	}
}