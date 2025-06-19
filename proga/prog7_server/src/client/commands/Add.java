package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class Add extends Command {

	public Add(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("add", "добавить новый элемент в коллекцию", networkIO, console, userManager);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (!userManager.isUserLoggedIn()) {
			return new ExecutionResponse(false, "You must be logged in to run this command. Use 'login' or 'register'.");
		}
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Incorrect number of arguments!\nUsage: '" + getName() + "'");
		}

		try {
			console.println("* Creating a new Worker (to be sent to the server):");
			Worker worker = Ask.askWorker();

			if (worker == null) {
				return new ExecutionResponse(false,"Worker creation was cancelled or failed.");
			}

			logger.debug("Creating Request for '{}' command with Worker object", getName());
			Request request = createRequest(getName(), "", worker);
			return sendRequestAndGetResponse(request);

		} catch (Ask.AskBreak e) {
			logger.warn("'add' operation cancelled by user.");
			return new ExecutionResponse(false, "Add operation cancelled.");
		}
	}
}