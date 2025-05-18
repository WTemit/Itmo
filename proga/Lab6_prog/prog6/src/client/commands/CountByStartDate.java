package client.commands;

import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class CountByStartDate extends Command {

    public CountByStartDate(ClientNetworkIO networkIO, StandardConsole console) {
        super("count_by_start_date ", "вывести количество элементов с заданной датой начала (формат ISO ZonedDateTime)", networkIO, console);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length < 2 || arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + " yyyy-MM-ddTHH:mm:ss+Zone[Region]'");
        }
        String dateString = arguments[1].trim();

        // Простая проверка формата на клиенте (сервер все равно проверит)
        try {
            ZonedDateTime.parse(dateString); // Попытка парсинга
        } catch (DateTimeParseException e) {
            logger.warn("Неверный формат даты, введенный пользователем: {}", dateString);
            return new ExecutionResponse(false, "Неверный формат даты! Требуется полный формат ISO ZonedDateTime (например, 2023-11-20T10:15:30+01:00[Europe/Paris]).");
        }

        logger.debug("Создание Request для команды '{}', Дата: {}", getName(), dateString);
        Request request = new Request(getName(), dateString); // name, stringArg (дата)
        return sendRequestAndGetResponse(request);
    }
}