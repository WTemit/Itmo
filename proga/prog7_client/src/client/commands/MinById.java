package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class MinById extends Command {
    public MinById(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("min_by_id", "вывести элемент с минимальным ID", networkIO, console, userManager);
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