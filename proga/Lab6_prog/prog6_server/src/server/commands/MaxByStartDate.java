package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.util.Comparator;
import java.util.Optional;

public class MaxByStartDate extends Command {
	private static final Logger commandLogger = LogManager.getLogger(MaxByStartDate.class);
	private final CollectionManager collectionManager;

	public MaxByStartDate(CollectionManager collectionManager) {
		super("max_by_start_date", "вывести элемент с максимальной датой начала");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды max_by_start_date...");

		if (collectionManager.getCollection().isEmpty()) {
			commandLogger.info("Коллекция пуста, невозможно найти max_by_start_date.");
			return new ExecutionResponse("Коллекция пуста.");
		}

		// Используем Stream API для поиска максимума
		Optional<Worker> maxWorkerOpt = collectionManager.getCollection().stream()
				.max(Comparator.comparing(worker -> worker.getStartDate())); // Сравниваем по дате начала

		if (maxWorkerOpt.isPresent()) {
			Worker maxWorker = maxWorkerOpt.get();
			commandLogger.info("Найден работник с максимальной датой начала: ID {}", maxWorker.getId());
			return new ExecutionResponse("Работник с максимальной датой начала:\n" + maxWorker.toString());
		} else {
			commandLogger.info("Не найдено работников с установленной датой начала.");
			return new ExecutionResponse("Не найдено работников с установленной датой начала.");
		}
	}
}