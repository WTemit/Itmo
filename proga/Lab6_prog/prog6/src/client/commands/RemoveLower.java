package client.commands;

import client.network.ClientNetworkIO;
import client.util.Ask;
import client.util.StandardConsole;
import common.dto.Request;
import common.models.Worker;
import common.util.ExecutionResponse;

public class RemoveLower extends Command {

    public RemoveLower(ClientNetworkIO networkIO, StandardConsole console) {
        super("remove_lower", "удалить из коллекции все элементы, меньшие, чем заданный", networkIO, console);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length > 1 && !arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        try {
            console.println("* Создание эталонного Рабочего для сравнения:");
            Worker worker = Ask.askWorker();

            if (worker == null) {
                return new ExecutionResponse(false,"Создание Рабочего прервано или не удалось.");
            }

            logger.debug("Создание Request для команды '{}' с объектом Worker", getName());
            Request request = new Request(getName(), "", worker);
            return sendRequestAndGetResponse(request);

        } catch (Ask.AskBreak e) {
            logger.warn("Операция 'remove_lower' прервана пользователем.");
            return new ExecutionResponse(false, "Отмена операции удаления...");
        }
    }
}