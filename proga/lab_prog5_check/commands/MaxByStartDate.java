package commands;

import managers.CollectionManager;
import models.Worker;
import utility.Console;
import utility.ExecutionResponse;
import java.util.*;

/**
 * Команда 'max_by_start_date'. Вывести любой объект из коллекции, значение поля startDate которого является максимальным.
 */
public class MaxByStartDate extends Command {
	private final Console console;
	private final CollectionManager collectionManager;

	public MaxByStartDate(Console console, CollectionManager collectionManager) {
		super("max_by_start_date", "вывести любой объект из коллекции, значение поля startDate которого является максимальным");
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

		Worker maxWorker = null;
		Date maxDate = null;

		for (Worker worker : collectionManager.getCollection()) {
			Date currentDate = worker.getStartData();
			if (currentDate != null) {
				if (maxDate == null || currentDate.after(maxDate)) {
					maxDate = currentDate;
					maxWorker = worker;
				}
			}
		}

		if (maxWorker == null) {
			return new ExecutionResponse(false, "Не найдено работников с указанной датой начала работы.");
		}

		return new ExecutionResponse("Работник с максимальной датой начала работы:\n" + maxWorker);
	}
}