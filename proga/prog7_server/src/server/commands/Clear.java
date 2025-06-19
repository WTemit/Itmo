package server.commands;

import common.dto.Request;
import common.dto.User;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

public class Clear extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Clear.class);
	private final CollectionManager collectionManager;

	public Clear(CollectionManager collectionManager) {
		super("clear", "очистить коллекцию (удаляет только ваши объекты)");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		User user = request.getUser();
		commandLogger.info("Executing clear command for user '{}'...", user.getUsername());
		try {
			int removedCount = collectionManager.clear(user);

			if (removedCount >= 0) {
				commandLogger.info("Successfully cleared {} elements for user '{}'.", removedCount, user.getUsername());
				return new ExecutionResponse("Successfully cleared " + removedCount + " of your elements from the collection!");
			} else {
				commandLogger.error("An error occurred in the database while clearing elements for user '{}'.", user.getUsername());
				return new ExecutionResponse(false, "Error: The collection could not be cleared due to a database error.");
			}
		} catch (Exception e) {
			commandLogger.error("Exception during clear command for user '{}': {}", user.getUsername(), e.getMessage(), e);
			return new ExecutionResponse(false, "An unexpected error occurred while clearing the collection: " + e.getMessage());
		}
	}
}