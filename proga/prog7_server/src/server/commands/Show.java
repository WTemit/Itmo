package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.util.Comparator;
import java.util.stream.Collectors;

public class Show extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Show.class);
	private final CollectionManager collectionManager;

	public Show(CollectionManager collectionManager) {
		super("show", "вывести все элементы коллекции (отсортированные по имени)");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Executing show command...");
		var collection = collectionManager.getCollection();
		if (collection.isEmpty()) {
			commandLogger.info("Collection is empty, show command returns empty message.");
			return new ExecutionResponse("The collection is empty!");
		}

		try {
			String result = collection.stream()
					.sorted(Comparator.comparing(Worker::getName, String.CASE_INSENSITIVE_ORDER))
					.map(Worker::toString)
					.collect(Collectors.joining("\n\n"));

			commandLogger.debug("Show command successfully generated result.");
			return new ExecutionResponse(result);
		} catch (Exception e) {
			commandLogger.error("Error formatting the output for show: {}", e.getMessage(), e);
			return new ExecutionResponse(false, "Server error while displaying the collection.");
		}
	}
}