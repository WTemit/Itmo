package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class CountByStartDate extends Command {
    private static final Logger commandLogger = LogManager.getLogger(CountByStartDate.class);
    private final CollectionManager collectionManager;

    public CountByStartDate(CollectionManager collectionManager) {
        super("count_by_start_date startDate", "вывести количество элементов с заданной датой начала");
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(Request request) {
        String dateString = request.getStringArg();
        commandLogger.debug("Выполнение команды count_by_start_date с датой '{}'", dateString);

        if (dateString == null || dateString.isEmpty()) {
            return new ExecutionResponse(false, "Необходимо указать дату начала в формате ISO 8601 (например, '2023-10-27T10:15:30+01:00[Europe/Paris]')");
        }

        ZonedDateTime targetDate;
        try {
            // Парсим строку как ZonedDateTime
            targetDate = ZonedDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            commandLogger.warn("Неверный формат даты: {}", dateString, e);
            return new ExecutionResponse(false, "Неверный формат даты! Используйте полный формат ISO 8601 (ZonedDateTime).");
        }

        long count = collectionManager.getCollection().stream()
                .filter(worker -> worker.getStartDate() != null && worker.getStartDate().equals(targetDate))
                .count();

        commandLogger.info("Найдено {} работников с датой начала {}", count, dateString);
        return new ExecutionResponse("Количество работников с датой начала " + dateString + ": " + count);
    }
}