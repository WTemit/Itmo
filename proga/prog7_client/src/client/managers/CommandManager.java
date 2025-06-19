package client.managers;

import client.commands.Command;
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
	private final Deque<String> commandHistory = new ArrayDeque<>(15);
	private static final int HISTORY_SIZE = 15;

	public void register(String commandName, Command command) {
		commands.put(commandName, command);
		logger.trace("Registered command: {}", commandName);
	}

	public Map<String, Command> getCommands() {
		return commands;
	}

	public Deque<String> getCommandHistory() {
		return new ArrayDeque<>(commandHistory);
	}

	public void addToHistory(String commandName) {
		if (commandName == null || commandName.trim().isEmpty()) {
			return;
		}
		if (commandHistory.size() >= HISTORY_SIZE) {
			commandHistory.pollLast();
		}
		commandHistory.offerFirst(commandName);
		logger.trace("Added to history: {}", commandName);
	}
}