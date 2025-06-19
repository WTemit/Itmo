package server.commands;

import common.dto.Request;
import common.dto.User;
import common.util.ExecutionResponse;
import server.db.DatabaseManager;

public class Register extends Command {
    private final DatabaseManager databaseManager;

    public Register(DatabaseManager databaseManager) {
        super("register", "зарегистрировать нового пользователя");
        this.databaseManager = databaseManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        User user = request.getUser();
        if (user == null || user.getUsername() == null || user.getPassword() == null ||
                user.getUsername().isBlank() || user.getPassword().isBlank()) {
            return new ExecutionResponse(false, "Username and password cannot be empty.");
        }

        boolean success = databaseManager.registerUser(user.getUsername(), user.getPassword());

        if (success) {
            logger.info("User '{}' registered successfully.", user.getUsername());
            return new ExecutionResponse("User '" + user.getUsername() + "' registered successfully. You can now use other commands.");
        } else {
            logger.warn("Failed to register user '{}'. It might already exist.", user.getUsername());
            return new ExecutionResponse(false, "Failed to register user '" + user.getUsername() + "'. The username may already be taken.");
        }
    }
}