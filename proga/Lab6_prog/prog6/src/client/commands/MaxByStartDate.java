package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;

public class MaxByStartDate extends Command {

	public MaxByStartDate(ClientNetworkIO networkIO, StandardConsole console) {
		super("max_by_start_date", "вывести элемент с максимальной датой начала", networkIO, console);
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