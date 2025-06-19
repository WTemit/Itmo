package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class RemoveById extends Command {
	public RemoveById(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("remove_by_id", "удалить элемент из коллекции по ID", networkIO, console, userManager);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (!userManager.isUserLoggedIn()) {
			return new ExecutionResponse(false, "You must be logged in to use this command.");
		}
		if (arguments.length < 2 || arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Usage: " + getName() + " {id}");
		}
		try {
			Long.parseLong(arguments[1].trim());
		} catch (NumberFormatException e) {
			return new ExecutionResponse(false, "ID must be a number.");
		}
		Request request = createRequest(getName(), arguments[1].trim(), null);
		return sendRequestAndGetResponse(request);
	}
}