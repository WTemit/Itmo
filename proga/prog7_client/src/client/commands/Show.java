package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class Show extends Command {
	public Show(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("show", "вывести все элементы коллекции", networkIO, console, userManager);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (!userManager.isUserLoggedIn()) {
			return new ExecutionResponse(false, "You must be logged in to use this command.");
		}
		Request request = createRequest(getName(), "", null);
		return sendRequestAndGetResponse(request);
	}
}