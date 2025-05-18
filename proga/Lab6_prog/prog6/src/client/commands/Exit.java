package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.util.ExecutionResponse;

public class Exit extends Command {

	public Exit(ClientNetworkIO networkIO, StandardConsole console) {
		// NetworkIO и Console здесь строго не нужны, но передаются для единообразия
		super("exit", "завершить программу клиента", networkIO, console);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		}
		// Эта команда обрабатывается локально Runner'ом
		logger.info("Команда Exit инициирована пользователем.");
		// Возвращаем специальный ответ, который проверит Runner
		return new ExecutionResponse(true, "exit"); // Сообщение "exit" сигнализирует о завершении
	}
}