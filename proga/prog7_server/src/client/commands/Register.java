package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.dto.User;
import common.util.ExecutionResponse;

public class Register extends Command {
    public Register(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("register", "зарегистрировать нового пользователя", networkIO, console, userManager);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        try {
            console.println("--- User Registration ---");
            String username = Ask.askString("Enter a new username: ");
            String password = Ask.askPassword("Enter a new password: ");

            User newUser = new User(username, password);

            // For registration, we send the new credentials in the request's User object.
            Request request = new Request("register", "", null, newUser);

            ExecutionResponse response = sendRequestAndGetResponse(request);

            // If registration was successful on the server, automatically log the user in locally.
            if (response.getExitCode()) {
                userManager.setCurrentUser(newUser);
                console.println("Registration successful! You are now logged in as '" + username + "'.");
            }

            return response;

        } catch (Ask.AskBreak e) {
            return new ExecutionResponse(false, "Registration cancelled.");
        }
    }
}