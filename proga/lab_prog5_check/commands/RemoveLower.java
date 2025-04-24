package commands;

import managers.CollectionManager;
import models.Worker;
import utility.Console;
import utility.ExecutionResponse;
import models.Ask;

/**
 * Команда 'remove_lower {element}'. Удалить из коллекции все элементы, меньшие, чем заданный.
 */
public class RemoveLower extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveLower(Console console, CollectionManager collectionManager) {
        super("remove_lower {element}", "удалить из коллекции все элементы, меньшие, чем заданный");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Выполняет команду
     * @return Успешность выполнения команды.
     */
    @Override
    public ExecutionResponse apply(String[] arguments) {
        try {
            if (arguments.length > 1 && !arguments[1].isEmpty()) {
                return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
            }

            console.println("* Создание эталонного работника для сравнения:");
            Worker referenceWorker = Ask.askWorker(console, 0L); // ID 0 для нового работника

            if (referenceWorker == null || !referenceWorker.validate()) {
                return new ExecutionResponse(false, "Поля работника не валидны! Работник не создан!");
            }

            int countBefore = collectionManager.getCollection().size();
            collectionManager.getCollection().removeIf(worker -> worker.compareTo(referenceWorker) < 0);
            int countRemoved = countBefore - collectionManager.getCollection().size();
            return new ExecutionResponse("Удалено работников: " + countRemoved);

        } catch (Ask.AskBreak e) {
            return new ExecutionResponse(false, "Создание элемента прервано");
        }
    }
}