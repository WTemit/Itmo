package client.managers;

import client.commands.Command; // Use client's Command interface/abstract class
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages client-side commands and history.
 */
public class CommandManager {
	private static final Logger logger = LogManager.getLogger(CommandManager.class);
	private final Map<String, Command> commands = new LinkedHashMap<>();
	// Use Deque for history, limit size
	private final Deque<String> commandHistory = new ArrayDeque<>(15); // Store last 15 commands
	private static final int HISTORY_SIZE = 15;

	/**
	 * Registers a client-side command.
	 * @param commandName Command name.
	 * @param command The command object.
	 */
	public void register(String commandName, Command command) {
		commands.put(commandName, command);
		logger.trace("Registered command: {}", commandName);
	}

	/**
	 * @return The map of registered client-side commands.
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * @return The command history (deque).
	 */
	public Deque<String> getCommandHistory() {
		// Return a copy or unmodifiable view if needed externally
		return new ArrayDeque<>(commandHistory);
	}

	/**
	 * Adds a command name to the history, maintaining size limit.
	 * @param commandName Command name executed.
	 */
	public void addToHistory(String commandName) {
		if (commandName == null || commandName.trim().isEmpty()) {
			return; // Don't add empty commands
		}
		if (commandHistory.size() >= HISTORY_SIZE) {
			commandHistory.pollLast(); // Remove the oldest command if full
		}
		commandHistory.offerFirst(commandName); // Add the new command to the front
		logger.trace("Added to history: {}", commandName);
	}
}