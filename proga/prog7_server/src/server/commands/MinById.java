package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.util.Comparator;
import java.util.Optional;

public class MinById extends Command {
    private static final Logger commandLogger = LogManager.getLogger(MinById.class);
    private final CollectionManager collectionManager;

    public MinById(CollectionManager collectionManager) {
        super("min_by_id", "вывести элемент с минимальным ID");
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        commandLogger.debug("Executing min_by_id command...");
        var collection = collectionManager.getCollection();

        if (collection.isEmpty()) {
            commandLogger.info("Collection is empty, cannot find min_by_id.");
            return new ExecutionResponse("The collection is empty.");
        }

        Optional<Worker> minWorkerOpt = collection.stream()
                .min(Comparator.comparing(Worker::getId));

        return minWorkerOpt.map(worker -> {
            commandLogger.info("Found worker with minimum ID: {}", worker.getId());
            return new ExecutionResponse("Worker with minimum ID (" + worker.getId() + "):\n" + worker);
        }).orElseGet(() -> {
            commandLogger.error("Could not find minimum ID in a non-empty collection!");
            return new ExecutionResponse(false, "Internal error: could not find min_by_id.");
        });
    }
}