package commands;

import managers.CollectionManager;
import models.Ask;
import models.Worker;
import utility.Console;
import utility.ExecutionResponse;

/**
 * Команда 'add'. Добавляет новый элемент в коллекцию.
 */
public class Add extends Command {
	private final Console console;
	private final CollectionManager collectionManager;

	public Add(Console console, CollectionManager collectionManager) {
		super("add {element}", "добавить новый элемент в коллекцию");
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
			if (!arguments[1].isEmpty()) return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		
			console.println("* Создание нового Рабочего:");
			Worker w = Ask.askWorker(console, collectionManager.getFreeId());
			
			if (w != null && w.validate()) {
				collectionManager.add(w);
				return new ExecutionResponse("Рабочий успешно добавлен!");
			} else return new ExecutionResponse(false,"Поля Рабочего не валидны! Рабочий не создан!");
		} catch (Ask.AskBreak e) {
			return new ExecutionResponse(false,"Отмена...");
		}
	}
}
