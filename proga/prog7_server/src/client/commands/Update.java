package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class Update extends Command {
	public Update(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		super("update", "обновить значение элемента коллекции по ID", networkIO, console, userManager);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (!userManager.isUserLoggedIn()) {
			return new ExecutionResponse(false, "You must be logged in to use this command.");
		}
		if (arguments.length < 2 || arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Usage: " + getName() + " {id}");
		}
		long id;
		try {
			id = Long.parseLong(arguments[1].trim());
		} catch (NumberFormatException e) {
			return new ExecutionResponse(false, "ID must be a number.");
		}
		try {
			console.println("* Creating new data for Worker with ID=" + id);
			Worker worker = Ask.askWorker();
			if (worker == null) return new ExecutionResponse(false, "Worker update cancelled.");

			Request request = createRequest(getName(), String.valueOf(id), worker);
			return sendRequestAndGetResponse(request);
		} catch (Ask.AskBreak e) {
			return new ExecutionResponse(false, "Operation cancelled.");
		}
	}
}