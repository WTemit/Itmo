package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CommandManager; // CommandManager сервера

import java.util.stream.Collectors;

public class History extends Command {
	private static final Logger commandLogger = LogManager.getLogger(History.class);
	private final CommandManager commandManager; // CommandManager сервера

	public History(CommandManager commandManager) {
		super("history", "вывести историю последних команд сервера (до 15)"); // Уточним описание
		this.commandManager = commandManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды history...");
		// Получаем историю команд, хранящуюся на СЕРВЕРЕ
		String historyText = commandManager.getCommandHistory().stream()
				.collect(Collectors.joining("\n"));

		if (historyText.isEmpty()) {
			commandLogger.info("История команд пуста.");
			return new ExecutionResponse("История команд на сервере пуста.");
		} else {
			commandLogger.info("Сформирована история команд.");
			return new ExecutionResponse("История команд сервера:\n" + historyText);
		}
	}
}