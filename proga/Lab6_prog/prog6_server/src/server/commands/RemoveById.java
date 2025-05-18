package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

public class RemoveById extends Command {
	private static final Logger commandLogger = LogManager.getLogger(RemoveById.class);
	private final CollectionManager collectionManager;

	public RemoveById(CollectionManager collectionManager) {
		super("remove_by_id <ID>", "удалить элемент из коллекции по ID");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		String idString = request.getStringArg();
		commandLogger.debug("Выполнение команды remove_by_id с ID '{}'", idString);

		if (idString == null || idString.isEmpty()) {
			return new ExecutionResponse(false, "Необходимо указать ID для удаления.");
		}

		long id;
		try {
			id = Long.parseLong(idString.trim());
		} catch (NumberFormatException e) {
			commandLogger.warn("Неверный формат ID: {}", idString);
			return new ExecutionResponse(false, "ID должен быть целым числом.");
		}

		// Проверяем существование элемента перед удалением
		Worker workerToRemove = collectionManager.byId(id); // Метод byId должен быть в CollectionManager
		if (workerToRemove == null) {
			commandLogger.warn("Попытка удаления несуществующего ID: {}", id);
			return new ExecutionResponse(false, "Элемент с ID " + id + " не найден.");
		}


		boolean removed = collectionManager.remove(id);

		if (removed) {
			commandLogger.info("Рабочий с ID {} успешно удален.", id);
			return new ExecutionResponse("Рабочий с ID " + id + " успешно удален!");
		} else {
			// Эта ситуация странная, если проверка выше прошла
			commandLogger.error("Не удалось удалить рабочего с ID {}, хотя он был найден.", id);
			return new ExecutionResponse(false, "Не удалось удалить рабочего с ID " + id + " (внутренняя ошибка).");
		}
	}
}