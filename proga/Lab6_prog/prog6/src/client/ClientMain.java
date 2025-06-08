package client;

import client.commands.*;
import client.managers.CommandManager;
import client.network.ClientNetworkIO;
import client.util.Runner;
import client.util.StandardConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientMain {
    private static final Logger logger = LogManager.getLogger(ClientMain.class);
    private static final String DEFAULT_HOST = "88.201.133.173"; // Или читать из аргументов/переменных окружения
    private static final int DEFAULT_PORT = 14881;       // Или читать из аргументов/переменных окружения

    public static void main(String[] args) {

        logger.info("Запуск клиента Лабораторной работы №6...");

        // Определяем хост и порт сервера (например, из аргументов или значений по умолчанию)
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                if (port <= 1024 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                logger.warn("Указан неверный порт ('{}'). Используется порт по умолчанию {}.", args[1], DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        logger.info("Попытка подключения к серверу по адресу {}:{}", host, port);


        StandardConsole console = new StandardConsole();
        ClientNetworkIO networkIO = null;
        try {
            networkIO = new ClientNetworkIO(host, port);
        } catch (IOException e) {
            console.printError("Не удалось инициализировать сетевой клиент: " + e.getMessage());
            logger.fatal("Ошибка инициализации сети. Завершение работы.", e);
            System.exit(1);
        }

        CommandManager commandManager = new CommandManager();
        Runner runner = new Runner(console, commandManager, networkIO);

        // Регистрация клиентских команд
        // Передаем networkIO и console командам, которые в них нуждаются
        commandManager.register("help", new Help(networkIO, console));
        commandManager.register("info", new Info(networkIO, console));
        commandManager.register("show", new Show(networkIO, console));
        commandManager.register("add", new Add(networkIO, console));
        commandManager.register("update", new Update(networkIO, console));
        commandManager.register("remove_by_id", new RemoveById(networkIO, console));
        commandManager.register("clear", new Clear(networkIO, console));
        commandManager.register("execute_script", new ExecuteScript(networkIO, console, runner)); // Передаем runner
        commandManager.register("exit", new Exit(networkIO, console)); // Команда Exit - клиентская
        commandManager.register("add_if_max", new AddIfMax(networkIO, console));
        commandManager.register("remove_lower", new RemoveLower(networkIO, console));
        commandManager.register("history", new History(networkIO, console, commandManager)); // Команда History теперь обращается к серверу
        commandManager.register("min_by_id", new MinById(networkIO, console));
        commandManager.register("max_by_start_date", new MaxByStartDate(networkIO, console));
        commandManager.register("count_by_start_date", new CountByStartDate(networkIO, console));

        logger.info("Настройка клиента завершена. Запуск интерактивного режима.");
        runner.interactiveMode(); // Запуск основного клиентского цикла

        // Очистка после завершения интерактивного режима (например, когда пользователь вводит exit)
        logger.info("Клиент завершает работу...");
        if (networkIO != null) {
            networkIO.close();
        }
        logger.info("Клиент завершил работу.");
        System.exit(0);
    }
}