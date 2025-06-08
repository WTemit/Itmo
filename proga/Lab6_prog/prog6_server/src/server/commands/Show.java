package server.commands;

import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.util.Comparator;
import java.util.stream.Collectors;

public class Show extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Show.class);
	private final CollectionManager collectionManager;

	public Show(CollectionManager collectionManager) {
		super("show", "вывести все элементы коллекции (отсортированные по имени)");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды show...");
		if (collectionManager.getCollection().isEmpty()) {
			commandLogger.info("Коллекция пуста для команды show.");
			return new ExecutionResponse("Коллекция пуста!");
		}

		try {
			String result = collectionManager.getCollection().stream()
					.sorted(Comparator.comparing(Worker::getName))
					.map(Worker::toString)
					.collect(Collectors.joining("\n\n"));

			commandLogger.debug("Команда show успешно сформировала результат.");
			return new ExecutionResponse(result);
		} catch (Exception e) {
			commandLogger.error("Ошибка при формировании вывода show: {}", e.getMessage(), e);
			return new ExecutionResponse(false, "Ошибка сервера при отображении коллекции.");
		}
	}
}