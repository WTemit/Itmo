package client.commands;

import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class Add extends Command {

	public Add(ClientNetworkIO networkIO, StandardConsole console) {
		super("add", "добавить новый элемент в коллекцию", networkIO, console);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length > 1 && !arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "' (без аргументов в строке)");
		}

		try {
			console.println("* Создание нового Рабочего (для отправки на сервер):");
			// Используем client.util.Ask для получения данных Worker от пользователя
			Worker worker = Ask.askWorker();

			if (worker == null) { // Ask вернул null (например, ошибка ввода)
				return new ExecutionResponse(false,"Создание Рабочего прервано или не удалось.");
			}

			// Валидация на клиенте не обязательна, если сервер ее выполняет,
			// но можно добавить базовую проверку.
			// if (!worker.validate()) { return new ExecutionResponse(false,"Поля Рабочего не валидны! Рабочий не создан!"); }

			logger.debug("Создание Request для команды '{}' с объектом Worker", getName());
			Request request = new Request(getName(), "", worker); // name, stringArg (пустой), objectArg
			return sendRequestAndGetResponse(request);

		} catch (Ask.AskBreak e) {
			logger.warn("Операция 'add' прервана пользователем.");
			return new ExecutionResponse(false, "Отмена операции добавления...");
		}
	}
}