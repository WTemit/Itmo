package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class RemoveLower extends Command {
    public RemoveLower(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("remove_lower", "удалить из коллекции все элементы, меньшие, чем заданный", networkIO, console, userManager);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (!userManager.isUserLoggedIn()) {
            return new ExecutionResponse(false, "You must be logged in to use this command.");
        }
        if (arguments.length > 1 && !arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "This command does not take any arguments.");
        }
        try {
            console.println("* Creating a reference Worker for comparison:");
            Worker worker = Ask.askWorker();
            if (worker == null) return new ExecutionResponse(false, "Worker creation cancelled.");

            Request request = createRequest(getName(), "", worker);
            return sendRequestAndGetResponse(request);
        } catch (Ask.AskBreak e) {
            return new ExecutionResponse(false, "Operation cancelled.");
        }
    }
}