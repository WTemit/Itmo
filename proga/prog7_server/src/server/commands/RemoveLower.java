package server.commands;

import common.dto.Request;
import common.dto.User;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class RemoveLower extends Command {
    private static final Logger commandLogger = LogManager.getLogger(RemoveLower.class);
    private final CollectionManager collectionManager;

    public RemoveLower(CollectionManager collectionManager) {
        super("remove_lower {element}", "удалить элементы, меньшие, чем заданный (среди ваших объектов)");
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        commandLogger.debug("Executing remove_lower command...");
        Serializable objectArg = request.getObjectArg();
        User user = request.getUser();

        if (!(objectArg instanceof Worker)) {
            commandLogger.warn("Received incorrect object type for remove_lower: {}", (objectArg == null ? "null" : objectArg.getClass()));
            return new ExecutionResponse(false, "Error: 'remove_lower' command requires a Worker object.");
        }

        Worker referenceWorker = (Worker) objectArg;

        if (!referenceWorker.validate()) {
            commandLogger.warn("Reference Worker failed validation: {}", referenceWorker);
            return new ExecutionResponse(false, "The fields of the reference Worker are not valid!");
        }

        // We only remove elements that belong to the current user and are smaller than the reference.
        Set<Worker> userOwnedWorkers = collectionManager.getCollection().stream()
                .filter(worker -> worker.getOwnerUsername().equals(user.getUsername()))
                .collect(Collectors.toSet());

        Set<Long> idsToRemove = userOwnedWorkers.stream()
                .filter(worker -> worker.compareTo(referenceWorker) < 0)
                .map(Worker::getId)
                .collect(Collectors.toSet());

        if (idsToRemove.isEmpty()) {
            commandLogger.info("No elements smaller than the given one found for user '{}'.", user.getUsername());
            return new ExecutionResponse("No elements smaller than the given one were found.");
        }

        int removedCount = 0;
        for (Long id : idsToRemove) {
            if (collectionManager.remove(id, user)) {
                removedCount++;
            }
        }

        commandLogger.info("Removed {} elements smaller than the reference for user '{}'.", removedCount, user.getUsername());
        return new ExecutionResponse("Workers removed: " + removedCount);
    }
}