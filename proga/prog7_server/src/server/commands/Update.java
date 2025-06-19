package server.commands;

import common.dto.Request;
import common.dto.User;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;

public class Update extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Update.class);
	private final CollectionManager collectionManager;

	public Update(CollectionManager collectionManager) {
		super("update <ID> {element}", "обновить значение элемента коллекции по ID");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		String idString = request.getStringArg();
		Serializable objectArg = request.getObjectArg();
		User user = request.getUser();
		commandLogger.debug("Executing update command for ID '{}' by user '{}'", idString, user.getUsername());

		if (idString == null || idString.isEmpty()) {
			return new ExecutionResponse(false, "You must provide an ID to update.");
		}
		if (!(objectArg instanceof Worker)) {
			commandLogger.warn("Received incorrect object type for update: {}", (objectArg == null ? "null" : objectArg.getClass()));
			return new ExecutionResponse(false, "Error: 'update' command requires a Worker object with new data.");
		}

		long id;
		try {
			id = Long.parseLong(idString.trim());
		} catch (NumberFormatException e) {
			commandLogger.warn("Invalid ID format for update: {}", idString);
			return new ExecutionResponse(false, "ID must be an integer.");
		}

		Worker updatedWorkerData = (Worker) objectArg;

		// Validate new data
		if (!updatedWorkerData.validate()) {
			commandLogger.warn("New data for Worker ID {} failed validation: {}", id, updatedWorkerData);
			return new ExecutionResponse(false, "The fields of the updated Worker are not valid!");
		}
		boolean updated = collectionManager.update(id, updatedWorkerData, user);

		if (updated) {
			commandLogger.info("Element with ID {} was successfully updated by user '{}'.", id, user.getUsername());
			return new ExecutionResponse("Element with ID " + id + " was successfully updated!");
		} else {
			commandLogger.warn("Failed to update element with ID {} for user '{}'. It may not exist or they may not have permission.", id, user.getUsername());
			return new ExecutionResponse(false, "Failed to update element with ID " + id + ". Either it does not exist, or you do not have permission to modify it.");
		}
	}
}