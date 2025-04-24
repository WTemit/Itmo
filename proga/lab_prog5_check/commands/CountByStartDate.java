package commands;

import managers.CollectionManager;
import models.Worker;
import utility.Console;
import utility.ExecutionResponse;

import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Команда 'count_by_start_date'. Вывести количество элементов, значение поля startDate которых равно заданному.
 */
public class CountByStartDate extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public CountByStartDate(Console console, CollectionManager collectionManager) {
        super("count_by_start_date startDate",
                "вывести количество элементов, значение поля startDate которых равно заданному");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length != 2) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        String targetDate;
        try {
            targetDate = arguments[1];
        } catch (DateTimeParseException e) {
            return new ExecutionResponse(false, "Неверный формат даты! Используйте формат yyyy-MM-dd");
        }

        int count = 0;
        for (Worker worker : collectionManager.getCollection()) {
            Date workerDate = worker.getStartData();
            if (workerDate != null && (String.valueOf(workerDate).equals(targetDate))) {
                count++;
            }
        }

        return new ExecutionResponse("Количество работников с датой начала работы " + targetDate + ": " + count);
    }
}