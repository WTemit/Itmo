package server.commands;

import common.dto.Request;
import common.models.*; // Импортируем все модели
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
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
        commandLogger.debug("Выполнение команды add_if_max...");
        Serializable objectArg = request.getObjectArg();

        if (!(objectArg instanceof Worker)) {
            commandLogger.warn("Получен некорректный тип объекта для команды add_if_max: {}", (objectArg == null ? "null" : objectArg.getClass()));
            return new ExecutionResponse(false, "Ошибка: для команды 'add_if_max' требуется объект Worker.");
        }

        Worker newWorker = (Worker) objectArg;

        // Валидация на сервере
        if (!newWorker.validate()) {
            commandLogger.warn("Полученный Worker не прошел валидацию: {}", newWorker);
            return new ExecutionResponse(false, "Поля Рабочего не валидны! Рабочий не создан!");
        }

        // Находим максимальный элемент в текущей коллекции
        // Используем compareTo, который по умолчанию сравнивает по ID (или другому основному полю)
        // Если нужно сравнивать по другому критерию, нужно явно указать Comparator
        Optional<Worker> maxWorkerOpt = collectionManager.getCollection().stream()
                .max(Comparator.naturalOrder()); // Использует compareTo() из Worker

        // Сравнение. Worker должен реализовать Comparable<Worker>
        boolean shouldAdd = maxWorkerOpt.map(maxWorker -> newWorker.compareTo(maxWorker) > 0).orElse(true);
        // Если коллекция пуста (orElse(true)), добавляем всегда.
        // Если не пуста, добавляем, если newWorker > maxWorker.

        if (shouldAdd) {
            // Генерация ID и установка дат на сервере
            long freeId = collectionManager.getFreeId();
            newWorker.setId(freeId);
            newWorker.setCreationDate(ZonedDateTime.now());

            boolean added = collectionManager.add(newWorker);
            if (added) {
                commandLogger.info("Worker (ID {}) добавлен, т.к. он больше максимального.", newWorker.getId());
                return new ExecutionResponse("Рабочий добавлен (ID: " + newWorker.getId() + ") - он больше максимального элемента.");
            } else {
                commandLogger.error("Не удалось добавить Worker с ID {}, хотя проверка add_if_max пройдена.", newWorker.getId());
                return new ExecutionResponse(false, "Не удалось добавить рабочего (внутренняя ошибка).");
            }
        } else {
            commandLogger.info("Worker не добавлен, т.к. он не больше максимального элемента.");
            return new ExecutionResponse("Рабочий НЕ добавлен - он не превышает максимальный элемент в коллекции.");
        }
    }
}