package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CommandManager;

public class History extends Command {
	private static final Logger commandLogger = LogManager.getLogger(History.class);
	private final CommandManager commandManager;

	public History(CommandManager commandManager) {
		super("history", "вывести историю последних команд сервера");
		this.commandManager = commandManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Executing history command...");
		String historyText = String.join("\n", commandManager.getCommandHistory());

		if (historyText.isEmpty()) {
			commandLogger.info("Command history is empty.");
			return new ExecutionResponse("Server command history is empty.");
		} else {
			commandLogger.info("Command history has been generated.");
			return new ExecutionResponse("Server command history:\n" + historyText);
		}
	}
}