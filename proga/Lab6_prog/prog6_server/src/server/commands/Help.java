package server.commands;

import common.dto.Request;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.managers.CommandManager; // Нужен CommandManager сервера

import java.util.stream.Collectors;

public class Help extends Command {
	private static final Logger commandLogger = LogManager.getLogger(Help.class);
	private final CommandManager commandManager; // CommandManager сервера

	public Help(CommandManager commandManager) {
		super("help", "вывести справку по доступным командам сервера");
		this.commandManager = commandManager;
	}

	@Override
	public ExecutionResponse apply(Request request) {
		commandLogger.debug("Выполнение команды help...");
		// Получаем список команд, зарегистрированных на СЕРВЕРЕ
		String helpText = commandManager.getCommands().values().stream()
				.map(cmd -> String.format(" %-35s : %s", cmd.getName(), cmd.getDescription()))
				.collect(Collectors.joining("\n"));

		commandLogger.info("Сформирована справка по командам.");
		return new ExecutionResponse("Доступные команды сервера:\n" + helpText);
	}
}