package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.util.ExecutionResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class CountByStartDate extends Command {
    public CountByStartDate(ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
        super("count_by_start_date", "вывести количество элементов с заданной датой начала", networkIO, console, userManager);
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (!userManager.isUserLoggedIn()) {
            return new ExecutionResponse(false, "You must be logged in to use this command.");
        }
        if (arguments.length < 2 || arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Usage: " + getName() + " {startDate_in_ISO_format}");
        }
        String dateString = arguments[1].trim();
        try {
            ZonedDateTime.parse(dateString); // Validate format on client side
        } catch (DateTimeParseException e) {
            return new ExecutionResponse(false, "Invalid date format! Use full ZonedDateTime format (e.g., 2023-01-01T12:00:00+03:00[Europe/Moscow]).");
        }
        Request request = createRequest(getName(), dateString, null);
        return sendRequestAndGetResponse(request);
    }
}