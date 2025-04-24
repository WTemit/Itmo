package commands;

import managers.CollectionManager;
import models.Worker;
import utility.Console;
import utility.ExecutionResponse;

/**
 * Команда 'min_by_id'. Вывести любой объект из коллекции, значение поля id которого является минимальным.
 */
public class MinById extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public MinById(Console console, CollectionManager collectionManager) {
        super("min_by_id", "вывести любой объект из коллекции, значение поля id которого является минимальным");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length > 1 && !arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        if (collectionManager.getCollection().isEmpty()) {
            return new ExecutionResponse(false, "Коллекция работников пуста.");
        }

        Worker minWorker = null;
        long minId = Long.MAX_VALUE;

        for (Worker worker : collectionManager.getCollection()) {
            if (worker.getId() < minId) {
                minId = worker.getId();
                minWorker = worker;
            }
        }

        if (minWorker == null) {
            return new ExecutionResponse(false, "Не удалось найти работника с минимальным ID.");
        }

        return new ExecutionResponse("Работник с минимальным ID (" + minId + "):\n" + minWorker);
    }
}