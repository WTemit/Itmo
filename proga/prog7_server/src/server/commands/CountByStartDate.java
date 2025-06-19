package server.commands;

import common.dto.Request;
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
        commandLogger.debug("Executing count_by_start_date with date '{}'", dateString);

        if (dateString == null || dateString.isEmpty()) {
            return new ExecutionResponse(false, "You must specify a start date in ISO 8601 format (e.g., '2023-10-27T10:15:30+01:00[Europe/Paris]')");
        }

        ZonedDateTime targetDate;
        try {
            targetDate = ZonedDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            commandLogger.warn("Invalid date format: {}", dateString, e);
            return new ExecutionResponse(false, "Invalid date format! Please use the full ISO 8601 format (ZonedDateTime).");
        }

        long count = collectionManager.getCollection().stream()
                .filter(worker -> worker.getStartDate() != null && worker.getStartDate().isEqual(targetDate))
                .count();

        commandLogger.info("Found {} workers with start date {}", count, dateString);
        return new ExecutionResponse("Number of workers with start date " + dateString + ": " + count);
    }
}