package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CollectionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Info extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Info.class);
	private final CollectionManager collectionManager;

	// Форматтер для вывода дат
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


	public Info(CollectionManager collectionManager) {
		super("info", "вывести информацию о коллекции");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды info...");

		LocalDateTime lastInitTime = collectionManager.getLastInitTime();
		String lastInitTimeString = (lastInitTime == null) ? "в данной сессии инициализации еще не происходило" :
				lastInitTime.format(formatter);

		LocalDateTime lastSaveTime = collectionManager.getLastSaveTime();
		String lastSaveTimeString = (lastSaveTime == null) ? "в данной сессии сохранения еще не происходило" :
				lastSaveTime.format(formatter);

		// Используем StringBuilder для формирования строки
		StringBuilder sb = new StringBuilder();
		sb.append("Сведения о коллекции:\n");
		// Используем .getSimpleName() для краткости
		sb.append(" Тип: ").append(collectionManager.getCollection().getClass().getSimpleName()).append("\n");
		sb.append(" Количество элементов: ").append(collectionManager.getCollection().size()).append("\n");
		sb.append(" Дата последнего сохранения: ").append(lastSaveTimeString).append("\n");
		sb.append(" Дата последней инициализации: ").append(lastInitTimeString);

		commandLogger.info("Сформирована информация о коллекции.");
		return new ExecutionResponse(sb.toString());
	}
}