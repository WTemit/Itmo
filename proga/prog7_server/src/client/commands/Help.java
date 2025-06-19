package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class Help extends Command {
	public Help(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("help", "вывести справку по доступным командам", networkIO, console, userManager);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		Request request = createRequest(getName(), "", null);
		return sendRequestAndGetResponse(request);
	}
}