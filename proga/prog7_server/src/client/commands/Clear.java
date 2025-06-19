package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class Clear extends Command {
	public Clear(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("clear", "очистить коллекцию (удаляет только ваши объекты)", networkIO, console, userManager);
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