package commands;

import managers.CollectionManager;
import utility.Console;
import models.Ask;
import utility.ExecutionResponse;

/**
 * Команда 'update'. Обновляет элемент коллекции.
 */
public class Update extends Command {
	private final Console console;
	private final CollectionManager collectionManager;

	public Update(Console console, CollectionManager collectionManager) {
		super("update <ID> {element}", "обновить значение элемента коллекции по ID");
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
			if (arguments.length < 2 || arguments[1].isEmpty()) {
				return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
			}

			long id;
			try {
				id = Long.parseLong(arguments[1].trim());
			} catch (NumberFormatException e) {
				return new ExecutionResponse(false, "ID должен быть числом");
			}

			var old = collectionManager.byId(id);
			if (old == null) {
				return new ExecutionResponse(false, "Элемент с ID " + id + " не найден");
			}

			console.println("* Создание нового Рабочего :");
			var updated = Ask.askWorker(console, old.getId());
			if (updated == null || !updated.validate()) {
				return new ExecutionResponse(false, "Поля Рабочего  не валидны! Рабочего не создан!");
			}

			// For HashSet, we need to remove and re-add to ensure proper hashing
			if (!collectionManager.remove(old.getId())) {
				return new ExecutionResponse(false, "Ошибка при удалении старого элемента");
			}

			if (!collectionManager.add(updated)) {
				// Rollback if addition fails
				collectionManager.add(old);
				return new ExecutionResponse(false, "Не удалось добавить обновленный элемент (возможно, конфликт ID)");
			}

			return new ExecutionResponse("Элемент с ID " + id + " успешно обновлен!");
		} catch (Ask.AskBreak e) {
			return new ExecutionResponse(false, "Создание элемента прервано");
		}
	}
}