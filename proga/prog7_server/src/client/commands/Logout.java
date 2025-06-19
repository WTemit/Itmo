package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.util.ExecutionResponse;

public class Logout extends Command {
    public Logout(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("logout", "выйти из системы", networkIO, console, userManager);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (!userManager.isUserLoggedIn()) {
            return new ExecutionResponse(false, "You are not logged in.");
        }
        String username = userManager.getCurrentUser().getUsername();
        userManager.logout();
        return new ExecutionResponse("You have been successfully logged out, " + username + ".");
    }
}