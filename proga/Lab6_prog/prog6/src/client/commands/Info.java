package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class Info extends Command {

	public Info(ClientNetworkIO networkIO, StandardConsole console) {
		super("info", "вывести информацию о коллекции", networkIO, console);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		}
		logger.debug("Создание Request для команды '{}'", getName());
		Request request = new Request(getName());
		return sendRequestAndGetResponse(request);
	}
}