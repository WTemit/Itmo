package commands;

import managers.CollectionManager;
import utility.Console;
import models.Worker;
import utility.ExecutionResponse;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Команда 'clear'. Очищает коллекцию.
 */
public class Clear extends Command {
	private final Console console;
	private final CollectionManager collectionManager;

	public Clear(Console console, CollectionManager collectionManager) {
		super("clear", "очистить коллекцию");
		this.console = console;
		this.collectionManager = collectionManager;
	}

	/**
	 * Выполняет команду
	 * @return Успешность выполнения команды.
	 */
	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (!arguments[1].isEmpty())
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");

		// Создаем копию коллекции для безопасного удаления элементов
		HashSet<Worker> collectionCopy = new HashSet<>(collectionManager.getCollection());

		// Удаляем каждого работника по его ID
		for (Worker worker : collectionCopy) {
			collectionManager.remove(worker.getId());
		}

		// Альтернативный подход (если хотим сразу очистить коллекцию)
		// collectionManager.getCollection().clear();

		return new ExecutionResponse("Коллекция очищена!");
	}
}