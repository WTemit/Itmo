package server.managers;

import server.commands.Command;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Управляет командами.
 */
public class CommandManager {
	private final Map<String, Command> commands = new LinkedHashMap<>();
	private final Set<String> commandHistory = new HashSet<>();

	/**
	 * Добавляет команду.
	 * @param commandName Название команды.
	 * @param command Команда.
	 */
	public void register(String commandName, Command command) {
		commands.put(commandName, command);
	}

	/**
	 * @return Словарь команд.
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * @return История команд.
	 */
	public Set<String> getCommandHistory() {
		return commandHistory;
	}

	/**
	 * Добавляет команду в историю.
	 * @param command Команда.
	 */
	public void addToHistory(String command) {
		commandHistory.add(command);
	}
}