package client.commands;

import client.managers.CommandManager; // Ссылка на менеджер команд клиента
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;
import java.util.stream.Collectors;

public class History extends Command {
	// Если нужно показывать ИСТОРИЮ КЛИЕНТА, а не сервера:
	private final CommandManager clientCommandManager;

	public History(ClientNetworkIO networkIO, StandardConsole console, CommandManager clientCommandManager) {
		super("history", "вывести историю последних 15 команд клиента", networkIO, console);
		this.clientCommandManager = clientCommandManager;
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		}

		// --- Вариант 1: Показать ЛОКАЛЬНУЮ историю команд клиента ---
		logger.debug("Отображение локальной истории команд клиента.");
		String historyText = clientCommandManager.getCommandHistory().stream()
				.collect(Collectors.joining("\n"));
		if (historyText.isEmpty()) {
			return new ExecutionResponse("История команд клиента пуста.");
		} else {
			return new ExecutionResponse("История команд клиента:\n" + historyText);
		}
	}
}