package server.handlers;

import common.dto.Request;
import common.dto.Response;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.Command;
import server.managers.CommandManager;

public class CommandHandler {
    private static final Logger logger = LogManager.getLogger(CommandHandler.class);
    private final CommandManager commandManager;

    public CommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public Response handle(Request request) {
        String commandName = request.getCommandName();
        logger.info("Обработка команды '{}'", commandName);
        Command command = commandManager.getCommands().get(commandName);

        if (command == null) {
            logger.warn("Команда '{}' не найдена.", commandName);
            return new Response(new ExecutionResponse(false, "Команда '" + commandName + "' не найдена на сервере."));
        }

        try {
            // Передаем весь объект Request в команду
            // Команда сама разберет, что ей нужно
            ExecutionResponse result = command.apply(request);
            commandManager.addToHistory(commandName); // Добавляем в историю сервера
            logger.info("Команда '{}' выполнена с результатом: {}", commandName, result.getExitCode());
            return new Response(result);
        } catch (Exception e) {
            logger.error("Ошибка выполнения команды '{}': {}", commandName, e.getMessage(), e);
            return new Response(new ExecutionResponse(false, "Внутренняя ошибка сервера при выполнении команды '" + commandName + "'."));
        }
    }
}