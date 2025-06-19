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

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public Info(CollectionManager collectionManager) {
		super("info", "вывести информацию о коллекции");
		this.collectionManager = collectionManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Executing info command...");

		LocalDateTime lastInitTime = collectionManager.getLastInitTime();
		String lastInitTimeString = (lastInitTime == null) ? "not yet initialized in this session" :
				lastInitTime.format(formatter);

		StringBuilder sb = new StringBuilder();
		sb.append("Collection Information:\n");
		sb.append("  Type: ").append(collectionManager.getCollectionType()).append("\n");
		sb.append("  Number of elements: ").append(collectionManager.getCollectionSize()).append("\n");
		sb.append("  Persistence Method: ").append("PostgreSQL Database (Live)").append("\n"); // New relevant info
		sb.append("  Last Initialization (from DB): ").append(lastInitTimeString);

		commandLogger.info("Information about the collection has been generated.");
		return new ExecutionResponse(sb.toString());
	}
}