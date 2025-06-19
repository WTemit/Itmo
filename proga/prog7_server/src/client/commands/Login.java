package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.User;
import common.util.ExecutionResponse;

public class Login extends Command {
    public Login(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("login", "войти в систему", networkIO, console, userManager);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (userManager.isUserLoggedIn()) {
            return new ExecutionResponse(false, "You are already logged in as '" + userManager.getCurrentUser().getUsername() + "'. Please 'logout' first.");
        }

        try {
            console.println("--- User Login ---");
            String username = Ask.askString("Username: ");
            String password = Ask.askPassword("Password: ");

            User userToLogin = new User(username, password);
            // Login is a client-side action: just store the credentials.
            // The server will verify them on the next command.
            userManager.setCurrentUser(userToLogin);

            return new ExecutionResponse("You are now logged in as '" + username + "'. Your credentials will be sent with subsequent commands.");

        } catch (Ask.AskBreak e) {
            return new ExecutionResponse(false, "Login cancelled.");
        }
    }
}