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
        commandLogger.debug("Выполнение команды min_by_id...");

        if (collectionManager.getCollection().isEmpty()) {
            commandLogger.info("Коллекция пуста, невозможно найти min_by_id.");
            return new ExecutionResponse("Коллекция пуста.");
        }

        // Используем Stream API для поиска минимума по ID
        Optional<Worker> minWorkerOpt = collectionManager.getCollection().stream()
                .min(Comparator.comparing(Worker::getId)); // Сравниваем по ID
        if (minWorkerOpt.isPresent()) {
            Worker minWorker = minWorkerOpt.get();
            commandLogger.info("Найден работник с минимальным ID: {}", minWorker.getId());
            return new ExecutionResponse("Работник с минимальным ID (" + minWorker.getId() + "):\n" + minWorker.toString());
        } else {
            // Эта ветка не должна достигаться при непустой коллекции
            commandLogger.error("Не удалось найти минимальный ID в непустой коллекции!");
            return new ExecutionResponse(false, "Внутренняя ошибка: не удалось найти min_by_id.");
        }
    }
}