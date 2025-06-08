package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

public class Save extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Save.class);
	private final CollectionManager collectionManager;

	public Save(CollectionManager collectionManager) {
		super("save", "сохранить коллекцию в файл (команда сервера)");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.info("Команда 'save' вызвана (возможно, внутренне через Request). Сохранение коллекции...");
		return executeSaveLogic(); // Вызываем общую логику сохранения
	}

	/**
	 * Основная логика выполнения сохранения коллекции.
	 * Может быть вызвана из apply(Request) или напрямую (executeSaveForServerConsole).
	 * @return ExecutionResponse с результатом операции.
	 */
	private ExecutionResponse executeSaveLogic() {
		try {
			collectionManager.saveCollection();
			commandLogger.info("Коллекция успешно сохранена.");
			return new ExecutionResponse("Коллекция успешно сохранена на сервере.");
		} catch (Exception e) {
			commandLogger.error("Ошибка при сохранении коллекции: {}", e.getMessage(), e);
			return new ExecutionResponse(false, "Ошибка на сервере при сохранении коллекции: " + e.getMessage());
		}
	}

	/**
	 * Метод для вызова команды сохранения непосредственно с серверной стороны
	 * (например, из серверной консоли).
	 * @return Строковое сообщение о результате для вывода в консоль сервера.
	 */
	public String executeSaveForServerConsole() {
		commandLogger.info("Команда 'save' вызвана из серверной консоли.");
		ExecutionResponse response = executeSaveLogic();
		return response.getMessage(); // Возвращаем сообщение для вывода в консоль
	}
}