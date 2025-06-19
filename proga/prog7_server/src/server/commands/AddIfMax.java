package server.commands;

import common.dto.Request;
import common.dto.User;
import common.models.*; // Импортируем все модели
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Optional;

public class AddIfMax extends Command {
    private static final Logger commandLogger = LogManager.getLogger(AddIfMax.class);
    private final CollectionManager collectionManager;

    public AddIfMax(CollectionManager collectionManager) {
        super("add_if_max {element}", "добавить новый элемент, если он превышает максимальный");
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        commandLogger.debug("Executing add_if_max command...");
        Serializable objectArg = request.getObjectArg();
        User user = request.getUser();

        if (!(objectArg instanceof Worker)) {
            commandLogger.warn("Received incorrect object type for add_if_max: {}", (objectArg == null ? "null" : objectArg.getClass()));
            return new ExecutionResponse(false, "Error: 'add_if_max' command requires a Worker object.");
        }

        Worker newWorker = (Worker) objectArg;

        if (!newWorker.validate()) {
            commandLogger.warn("Received Worker failed validation: {}", newWorker);
            return new ExecutionResponse(false, "Worker fields are not valid! Worker not created!");
        }

        Optional<Worker> maxWorkerOpt = collectionManager.getCollection().stream()
                .max(Comparator.naturalOrder());

        boolean shouldAdd = maxWorkerOpt.map(maxWorker -> newWorker.compareTo(maxWorker) > 0).orElse(true);

        if (shouldAdd) {
            newWorker.setCreationDate(ZonedDateTime.now());

            long newId = collectionManager.add(newWorker, user);
            if (newId > 0) {
                commandLogger.info("Worker (ID {}) added by user '{}' as it's larger than the max.", newId, user.getUsername());
                return new ExecutionResponse("Worker added (ID: " + newId + ") - it was larger than the maximum element.");
            } else {
                commandLogger.error("Failed to add Worker for user '{}' even though add_if_max check passed.", user.getUsername());
                return new ExecutionResponse(false, "Failed to add worker (database operation failed).");
            }
        } else {
            commandLogger.info("Worker not added by user '{}' as it's not larger than the max element.", user.getUsername());
            return new ExecutionResponse("Worker was NOT added - it does not exceed the maximum element in the collection.");
        }
    }
}