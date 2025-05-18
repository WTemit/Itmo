package client.commands;

import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class AddIfMax extends Command {

    public AddIfMax(ClientNetworkIO networkIO, StandardConsole console) {
        super("add_if_max", "добавить новый элемент, если он превышает максимальный", networkIO, console);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length > 1 && !arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        try {
            console.println("* Создание нового Рабочего для сравнения:");
            Worker worker = Ask.askWorker();

            if (worker == null) {
                return new ExecutionResponse(false,"Создание Рабочего прервано или не удалось.");
            }

            logger.debug("Создание Request для команды '{}' с объектом Worker", getName());
            Request request = new Request(getName(), "", worker);
            return sendRequestAndGetResponse(request);

        } catch (Ask.AskBreak e) {
            logger.warn("Операция 'add_if_max' прервана пользователем.");
            return new ExecutionResponse(false, "Отмена операции добавления...");
        }
    }
}