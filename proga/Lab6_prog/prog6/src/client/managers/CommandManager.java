package client.managers;

import client.commands.Command; // Используем интерфейс/абстрактный класс Command клиента
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Управляет клиентскими командами и историей.
 */
public class CommandManager {
	private static final Logger logger = LogManager.getLogger(CommandManager.class);
	private final Map<String, Command> commands = new LinkedHashMap<>();
	// Используем Deque для истории, ограничиваем размер
	private final Deque<String> commandHistory = new ArrayDeque<>(15); // Храним последние 15 команд
	private static final int HISTORY_SIZE = 15;

	/**
	 * Регистрирует клиентскую команду.
	 * @param commandName Имя команды.
	 * @param command Объект команды.
	 */
	public void register(String commandName, Command command) {
		commands.put(commandName, command);
		logger.trace("Зарегистрирована команда: {}", commandName);
	}

	/**
	 * @return Карта зарегистрированных клиентских команд.
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * @return История команд (deque).
	 */
	public Deque<String> getCommandHistory() {
		// Возвращаем копию или неизменяемое представление, если это необходимо извне
		return new ArrayDeque<>(commandHistory);
	}

	/**
	 * Добавляет имя команды в историю, поддерживая ограничение по размеру.
	 * @param commandName Имя выполненной команды.
	 */
	public void addToHistory(String commandName) {
		if (commandName == null || commandName.trim().isEmpty()) {
			return; // Не добавляем пустые команды
		}
		if (commandHistory.size() >= HISTORY_SIZE) {
			commandHistory.pollLast(); // Удаляем самую старую команду, если история заполнена
		}
		commandHistory.offerFirst(commandName); // Добавляем новую команду в начало
		logger.trace("Добавлено в историю: {}", commandName);
	}
}