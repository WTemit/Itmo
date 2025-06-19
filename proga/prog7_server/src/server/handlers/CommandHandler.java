package server.handlers;

import common.dto.Request;
import common.dto.Response;
import common.dto.User;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.Command;
import server.db.DatabaseManager;
import server.managers.CommandManager;

import java.util.Set;

public class CommandHandler {
    private static final Logger logger = LogManager.getLogger(CommandHandler.class);
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private static final Set<String> PUBLIC_COMMANDS = Set.of("help", "register");


    public CommandHandler(CommandManager commandManager, DatabaseManager databaseManager) {
        this.commandManager = commandManager;
        this.databaseManager = databaseManager;
    }

    public Response handle(Request request) {
        String commandName = request.getCommandName();
        User user = request.getUser();

        if (!PUBLIC_COMMANDS.contains(commandName)) {
            if (user == null || user.getUsername() == null || user.getPassword() == null) {
                logger.warn("Authentication failed for command '{}': no credentials provided.", commandName);
                return new Response(new ExecutionResponse(false, "Authentication required. Please provide username and password via login/register."));
            }
            if (!databaseManager.authenticateUser(user.getUsername(), user.getPassword())) {
                logger.warn("Authentication failed for user '{}' trying to execute '{}'", user.getUsername(), commandName);
                return new Response(new ExecutionResponse(false, "Authentication failed: Invalid username or password."));
            }
            logger.info("User '{}' authenticated successfully for command '{}'", user.getUsername(), commandName);
        }

        logger.info("Processing command '{}' from {}", commandName, (user != null ? user.getUsername() : "an unauthenticated source"));
        Command command = commandManager.getCommands().get(commandName);

        if (command == null) {
            logger.warn("Command '{}' not found.", commandName);
            return new Response(new ExecutionResponse(false, "Command '" + commandName + "' not found on server. Use 'help'."));
        }

        try {
            ExecutionResponse result = command.apply(request);
            if (!commandName.equals("history")) {
                commandManager.addToHistory(commandName);
            }
            logger.info("Command '{}' executed with result: {}", commandName, result.getExitCode());
            return new Response(result);
        } catch (Exception e) {
            logger.error("Error executing command '{}': {}", commandName, e.getMessage(), e);
            return new Response(new ExecutionResponse(false, "Internal server error while executing command '" + commandName + "'."));
        }
    }
}