package server.commands;

import common.dto.Request;
import common.dto.User;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Add extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Add.class);
	private final CollectionManager collectionManager;

	public Add(CollectionManager collectionManager) {
		super("add {element}", "добавить новый элемент в коллекцию");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Executing add command...");
		Serializable objectArg = request.getObjectArg();
		User user = request.getUser();

		if (!(objectArg instanceof Worker)) {
			commandLogger.warn("Received incorrect object type for add: {}", (objectArg == null ? "null" : objectArg.getClass()));
			return new ExecutionResponse(false, "Error: 'add' command requires a Worker object.");
		}

		Worker workerToAdd = (Worker) objectArg;

		if (!workerToAdd.validate()) {
			commandLogger.warn("Received Worker failed validation: {}", workerToAdd);
			return new ExecutionResponse(false, "Worker fields are not valid! Worker not created!");
		}

		workerToAdd.setCreationDate(ZonedDateTime.now());

		long newId = collectionManager.add(workerToAdd, user);

		if (newId > 0) {
			commandLogger.info("Worker with ID {} successfully added by user '{}'.", newId, user.getUsername());
			return new ExecutionResponse("Worker successfully added! (ID: " + newId + ")");
		} else {
			commandLogger.error("Failed to add Worker for user '{}', DB operation failed.", user.getUsername());
			return new ExecutionResponse(false, "Failed to add worker (the database rejected the operation).");
		}
	}
}