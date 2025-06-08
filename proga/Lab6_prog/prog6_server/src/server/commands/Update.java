package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Update extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Update.class);
	private final CollectionManager collectionManager;

	public Update(CollectionManager collectionManager) {
		super("update <ID> {element}", "обновить значение элемента коллекции по ID");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		String idString = request.getStringArg(); // ID берем из строкового аргумента
		Serializable objectArg = request.getObjectArg(); // Обновленные данные Worker из объекта
		commandLogger.debug("Выполнение команды update для ID '{}'", idString);

		if (idString == null || idString.isEmpty()) {
			return new ExecutionResponse(false, "Необходимо указать ID для обновления.");
		}
		if (!(objectArg instanceof Worker)) {
			commandLogger.warn("Получен некорректный тип объекта для команды update: {}", (objectArg == null ? "null" : objectArg.getClass()));
			return new ExecutionResponse(false, "Ошибка: для команды 'update' требуется объект Worker с новыми данными.");
		}

		long id;
		try {
			id = Long.parseLong(idString.trim());
		} catch (NumberFormatException e) {
			commandLogger.warn("Неверный формат ID для update: {}", idString);
			return new ExecutionResponse(false, "ID должен быть целым числом.");
		}

		Worker updatedWorkerData = (Worker) objectArg;

		// 1. Проверяем, существует ли элемент с таким ID
		Worker oldWorker = collectionManager.byId(id);
		if (oldWorker == null) {
			commandLogger.warn("Попытка обновить несуществующий ID: {}", id);
			return new ExecutionResponse(false, "Элемент с ID " + id + " не найден.");
		}

		// 2. Валидируем новые данные
		if (!updatedWorkerData.validate()) {
			commandLogger.warn("Новые данные для Worker ID {} не прошли валидацию: {}", id, updatedWorkerData);
			return new ExecutionResponse(false, "Поля обновленного Рабочего не валидны!");
		}

		// 3. Устанавливаем корректный ID и дату создания в обновленных данных
		updatedWorkerData.setId(id); // Гарантируем правильный ID
		updatedWorkerData.setCreationDate(oldWorker.getCreationDate()); // Сохраняем исходную дату создания

		// 4. Выполняем замену в менеджере коллекции
		boolean updated = collectionManager.update(id, updatedWorkerData);

		if (updated) {
			commandLogger.info("Элемент с ID {} успешно обновлен.", id);
			return new ExecutionResponse("Элемент с ID " + id + " успешно обновлен!");
		} else {
			commandLogger.error("Не удалось обновить элемент с ID {}.", id);
			return new ExecutionResponse(false, "Не удалось обновить элемент с ID " + id + " (внутренняя ошибка).");
		}
	}
}