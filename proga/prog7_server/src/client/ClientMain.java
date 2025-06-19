package client;

import client.commands.*;
import client.managers.CommandManager;
import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.Runner;
import client.util.StandardConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientMain {
    private static final Logger logger = LogManager.getLogger(ClientMain.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 14881;

    public static void main(String[] args) {
        logger.info("Starting Client for Lab 7...");
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                if (port <= 1024 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                logger.warn("Invalid port '{}'. Using default port {}.", args[1], DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        logger.info("Attempting to connect to server at {}:{}", host, port);

        StandardConsole console = new StandardConsole();
        ClientNetworkIO networkIO;
        try {
            networkIO = new ClientNetworkIO(host, port);
        } catch (IOException e) {
            console.printError("Could not initialize network client: " + e.getMessage());
            logger.fatal("Network initialization error. Exiting.", e);
            System.exit(1);
            return;
        }

        UserManager userManager = new UserManager();
        CommandManager commandManager = new CommandManager();
        Runner runner = new Runner(console, commandManager, networkIO, userManager);

        // Register all commands
        registerCommands(commandManager, networkIO, console, userManager, runner);

        logger.info("Client setup complete. Starting interactive mode.");
        runner.interactiveMode();

        logger.info("Client is shutting down...");
        networkIO.close();
        logger.info("Client has shut down.");
        System.exit(0);
    }

    private static void registerCommands(CommandManager commandManager, ClientNetworkIO networkIO, StandardConsole console, UserManager userManager, Runner runner) {
        // Authentication commands
        commandManager.register("register", new Register(networkIO, console, userManager));
        commandManager.register("login", new Login(networkIO, console, userManager));
        commandManager.register("logout", new Logout(networkIO, console, userManager));

        // Local client commands
        commandManager.register("exit", new Exit());
        commandManager.register("history", new History(networkIO, console, userManager, commandManager));
        commandManager.register("execute_script", new ExecuteScript(console, runner));

        // Commands requiring authentication
        commandManager.register("help", new Help(networkIO, console, userManager));
        commandManager.register("info", new Info(networkIO, console, userManager));
        commandManager.register("show", new Show(networkIO, console, userManager));
        commandManager.register("add", new Add(networkIO, console, userManager));
        commandManager.register("update", new Update(networkIO, console, userManager));
        commandManager.register("remove_by_id", new RemoveById(networkIO, console, userManager));
        commandManager.register("clear", new Clear(networkIO, console, userManager));
        commandManager.register("add_if_max", new AddIfMax(networkIO, console, userManager));
        commandManager.register("remove_lower", new RemoveLower(networkIO, console, userManager));
        commandManager.register("min_by_id", new MinById(networkIO, console, userManager));
        commandManager.register("max_by_start_date", new MaxByStartDate(networkIO, console, userManager));
        commandManager.register("count_by_start_date", new CountByStartDate(networkIO, console, userManager));
    }
}