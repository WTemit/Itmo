package server.commands;

import common.dto.Request;
import common.dto.User;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

public class RemoveById extends Command {
	private static final Logger commandLogger = LogManager.getLogger(RemoveById.class);
	private final CollectionManager collectionManager;

	public RemoveById(CollectionManager collectionManager) {
		super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		String idString = request.getStringArg();
		User user = request.getUser();
		commandLogger.debug("Executing remove_by_id command for ID '{}' by user '{}'", idString, user.getUsername());

		if (idString == null || idString.isEmpty()) {
			return new ExecutionResponse(false, "You must provide an ID for removal.");
		}

		long id;
		try {
			id = Long.parseLong(idString.trim());
		} catch (NumberFormatException e) {
			commandLogger.warn("Invalid ID format: {}", idString);
			return new ExecutionResponse(false, "ID must be an integer.");
		}

		boolean removed = collectionManager.remove(id, user);

		if (removed) {
			commandLogger.info("Worker with ID {} successfully removed by user '{}'.", id, user.getUsername());
			return new ExecutionResponse("Worker with ID " + id + " successfully removed!");
		} else {
			commandLogger.warn("Failed to remove element with ID {} for user '{}'. It may not exist or they may not have permission.", id, user.getUsername());
			return new ExecutionResponse(false, "Failed to remove element with ID " + id + ". Either it does not exist, or you do not have permission to modify it.");
		}
	}
}