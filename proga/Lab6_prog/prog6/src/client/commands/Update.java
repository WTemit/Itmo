package client.commands;

import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class Update extends Command {

	public Update(ClientNetworkIO networkIO, StandardConsole console) {
		super("updateadd_if_max", "обновить значение элемента коллекции по ID", networkIO, console);
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

		try {
			console.println("* Введите новые данные для Рабочего с ID=" + id + ":");
			Worker worker = Ask.askWorker();

			if (worker == null) { // Ask вернул null
				return new ExecutionResponse(false,"Обновление Рабочего прервано или не удалось.");
			}

			// Примечание: Сервер установит ID и creationDate на основе существующего элемента.
			// Мы отправляем ID в основном в строковом аргументе.
			logger.debug("Создание Request для команды '{}', ID: {}, с объектом Worker", getName(), id);
			Request request = new Request(getName(), String.valueOf(id), worker); // name, stringArg (ID), objectArg
			return sendRequestAndGetResponse(request);

		} catch (Ask.AskBreak e) {
			logger.warn("Операция 'update' для ID {} прервана пользователем.", id);
			return new ExecutionResponse(false, "Отмена операции обновления...");
		}
	}
}