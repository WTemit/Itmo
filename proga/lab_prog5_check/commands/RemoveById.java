package commands;

import managers.CollectionManager;
import utility.Console;
import models.Worker;
import utility.ExecutionResponse;

/**
 * Команда 'remove_by_id'. Удаляет элемент из коллекции.
 */
public class RemoveById extends Command {
	private final Console console;
	private final CollectionManager collectionManager;

	public RemoveById(Console console, CollectionManager collectionManager) {
		super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
		this.console = console;
		this.collectionManager = collectionManager;
	}

	/**
	 * Выполняет команду
	 * @return Успешность выполнения команды.
	 */
	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments[1].isEmpty()) return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		long id = -1;
		try { id = Long.parseLong(arguments[1].trim()); } catch (NumberFormatException e) { return new ExecutionResponse(false, "ID не распознан"); }

		Worker worker = collectionManager.byId(id);
		if (worker == null || !collectionManager.getCollection().contains(worker))
			return new ExecutionResponse(false, "Не существующий ID");

		boolean success = collectionManager.remove(id);

		if (success) {
			return new ExecutionResponse("Рабочий успешно удалён!");
		} else {
			return new ExecutionResponse(false, "Не удалось удалить рабочего");
		}
	}
}