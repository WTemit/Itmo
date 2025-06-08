package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Add extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Add.class); // Используем свой логгер
	private final CollectionManager collectionManager;

	public Add(CollectionManager collectionManager) {
		super("add {element}", "добавить новый элемент в коллекцию");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды add...");
		Serializable objectArg = request.getObjectArg();

		if (!(objectArg instanceof Worker)) {
			commandLogger.warn("Получен некорректный тип объекта для команды add: {}", (objectArg == null ? "null" : objectArg.getClass()));
			return new ExecutionResponse(false, "Ошибка: для команды 'add' требуется объект Worker.");
		}

        var workerToAdd = (Worker) objectArg;

		commandLogger.debug("Worker получен от клиента перед валидацией: {}", workerToAdd.toString());
		commandLogger.debug("Поля Worker: ID={}, Name={}, Coords={}, Salary={}, Pos={}, Org={}, CreateDate={}, StartDate={}, EndDate={}",
				workerToAdd.getId(),
				workerToAdd.getName(),
				workerToAdd.getCoordinates(),
				workerToAdd.getSalary(),
				workerToAdd.getPosition(),
				workerToAdd.getOrganization(),
				workerToAdd.getCreationDate(),
				workerToAdd.getStartDate(),
				workerToAdd.getEndDate());

		// Валидация на сервере
		if (!workerToAdd.validate()) {
			commandLogger.warn("Полученный Worker не прошел валидацию: {}", workerToAdd);
			return new ExecutionResponse(false, "Поля Рабочего не валидны! Рабочий не создан!");
		}

		// Генерация ID и установка дат на сервере
		long freeId = collectionManager.getFreeId();
		workerToAdd.setId(freeId);
		workerToAdd.setCreationDate(ZonedDateTime.now()); // Устанавливаем серверное время создания

		boolean added = collectionManager.add(workerToAdd);

		if (added) {
			commandLogger.info("Worker с ID {} успешно добавлен.", workerToAdd.getId());
			return new ExecutionResponse("Рабочий успешно добавлен! (ID: " + workerToAdd.getId() + ")");
		} else {
			commandLogger.error("Не удалось добавить Worker с ID {}, хотя ID был сгенерирован как свободный.", workerToAdd.getId());
			return new ExecutionResponse(false, "Не удалось добавить рабочего (возможна внутренняя ошибка сервера).");
		}
	}
}