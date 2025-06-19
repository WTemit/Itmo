package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.util.Comparator;
import java.util.Optional;

public class MaxByStartDate extends Command {
	private static final Logger commandLogger = LogManager.getLogger(MaxByStartDate.class);
	private final CollectionManager collectionManager;

	public MaxByStartDate(CollectionManager collectionManager) {
		super("max_by_start_date", "вывести элемент с максимальной датой начала");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Executing max_by_start_date command...");
		var collection = collectionManager.getCollection();

		if (collection.isEmpty()) {
			commandLogger.info("Collection is empty, cannot find max_by_start_date.");
			return new ExecutionResponse("The collection is empty.");
		}

		Optional<Worker> maxWorkerOpt = collection.stream()
				.filter(worker -> worker.getStartDate() != null)
				.max(Comparator.comparing(Worker::getStartDate));

		return maxWorkerOpt.map(worker -> {
			commandLogger.info("Found worker with max start date: ID {}", worker.getId());
			return new ExecutionResponse("Worker with maximum start date:\n" + worker);
		}).orElseGet(() -> {
			commandLogger.info("No workers with a start date were found.");
			return new ExecutionResponse("No workers with a start date were found.");
		});
	}
}