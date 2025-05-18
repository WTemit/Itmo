package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class RemoveById extends Command {

	public RemoveById(ClientNetworkIO networkIO, StandardConsole console) {
		super("remove_by_id", "удалить элемент из коллекции по ID", networkIO, console);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length < 2 || arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + " ID'");
		}

		long id;
		try {
			id = Long.parseLong(arguments[1].trim());
			if (id <= 0) throw new NumberFormatException("ID должен быть положительным");
		} catch (NumberFormatException e) {
			return new ExecutionResponse(false, "ID должен быть положительным числом.");
		}

		logger.debug("Создание Request для команды '{}', ID: {}", getName(), id);
		Request request = new Request(getName(), String.valueOf(id)); // name, stringArg (ID)
		return sendRequestAndGetResponse(request);
	}
}