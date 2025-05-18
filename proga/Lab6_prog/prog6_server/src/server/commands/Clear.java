package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

public class Clear extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Clear.class);
	private final CollectionManager collectionManager;

	public Clear(CollectionManager collectionManager) {
		super("clear", "очистить коллекцию");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.info("Выполнение команды clear...");
		try {
			int sizeBefore = collectionManager.getCollection().size(); // Размер до очистки
			collectionManager.clearCollection(); // Вызываем метод очистки
			int sizeAfter = collectionManager.getCollection().size(); // Размер после

			if (sizeAfter == 0) {
				commandLogger.info("Коллекция успешно очищена. Было удалено {} элементов.", sizeBefore);
				return new ExecutionResponse("Коллекция успешно очищена!");
			} else {
				// Эта ветка не должна достигаться, если clearCollection() использует HashSet.clear()
				commandLogger.error("Коллекция не была полностью очищена. Осталось {} элементов.", sizeAfter);
				return new ExecutionResponse(false, "Ошибка: Коллекция не была полностью очищена. Осталось " + sizeAfter + " элементов.");
			}
		} catch (Exception e) {
			commandLogger.error("Исключение во время выполнения команды clear: {}", e.getMessage(), e);
			return new ExecutionResponse(false, "Произошла ошибка при очистке коллекции: " + e.getMessage());
		}
	}
}