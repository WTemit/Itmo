package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class Help extends Command {

	public Help(ClientNetworkIO networkIO, StandardConsole console) {
		super("help", "вывести справку по доступным командам", networkIO, console);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
		}
		logger.debug("Создание Request для команды '{}'", getName());
		Request request = new Request(getName()); // Команда только с именем
		return sendRequestAndGetResponse(request);
	}
}