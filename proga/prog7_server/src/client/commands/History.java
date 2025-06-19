package client.commands;

import client.managers.CommandManager;
import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.util.ExecutionResponse;

public class History extends Command {
	private final CommandManager clientCommandManager;

	public History(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager, CommandManager clientCommandManager) {
		super("history", "вывести историю последних 15 команд клиента", networkIO, console, userManager);
		this.clientCommandManager = clientCommandManager;
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		String historyText = String.join("\n", clientCommandManager.getCommandHistory());
		if (historyText.isEmpty()) {
			return new ExecutionResponse("Client command history is empty.");
		} else {
			return new ExecutionResponse("Client command history:\n" + historyText);
		}
	}
}