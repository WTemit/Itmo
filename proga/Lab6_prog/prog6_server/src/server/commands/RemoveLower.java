package server.commands;

import common.dto.Request;
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
        super("remove_lower {element}", "удалить элементы, меньшие, чем заданный");
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        commandLogger.debug("Выполнение команды remove_lower...");
        Serializable objectArg = request.getObjectArg();

        if (!(objectArg instanceof Worker)) {
            commandLogger.warn("Получен некорректный тип объекта для команды remove_lower: {}", (objectArg == null ? "null" : objectArg.getClass()));
            return new ExecutionResponse(false, "Ошибка: для команды 'remove_lower' требуется объект Worker.");
        }

        Worker referenceWorker = (Worker) objectArg;

        // Валидация эталонного Worker (на всякий случай)
        if (!referenceWorker.validate()) {
            commandLogger.warn("Эталонный Worker не прошел валидацию: {}", referenceWorker);
            return new ExecutionResponse(false, "Поля эталонного Рабочего не валидны!");
        }

        int countBefore = collectionManager.getCollection().size();

        // Собираем ID элементов, которые нужно удалить
        Set<Long> idsToRemove = collectionManager.getCollection().stream()
                .filter(worker -> worker.compareTo(referenceWorker) < 0) // Используем compareTo
                .map(Worker::getId)
                .collect(Collectors.toSet());

        if (idsToRemove.isEmpty()) {
            commandLogger.info("Нет элементов меньших, чем заданный.");
            return new ExecutionResponse("Не найдено элементов, меньших, чем заданный.");
        }

        // Удаляем найденные элементы через менеджер
        idsToRemove.forEach(id -> collectionManager.remove(id));

        int countAfter = collectionManager.getCollection().size();
        int countRemoved = countBefore - countAfter;

        commandLogger.info("Удалено {} элементов, меньших чем эталонный.", countRemoved);
        return new ExecutionResponse("Удалено работников: " + countRemoved);
    }
}