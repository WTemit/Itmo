package client;

import client.commands.*; // Import all command classes
import client.managers.CommandManager;
import client.network.ClientNetworkIO;
import client.util.Runner;
import client.util.StandardConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientMain {
    private static final Logger logger = LogManager.getLogger(ClientMain.class);
    private static final String DEFAULT_HOST = "88.201.133.173"; // Or read from args/env
    private static final int DEFAULT_PORT = 14881;       // Or read from args/env

    public static void main(String[] args) {
        // Setup basic console logging if log4j2.xml is not found
        // PropertyConfigurator.configure(...); or BasicConfigurator.configure();

        logger.info("Starting Lab 6 Client...");

        // Determine server host and port (e.g., from args or defaults)
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                if (port <= 1024 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                logger.warn("Invalid port specified ('{}'). Using default port {}.", args[1], DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }
        logger.info("Attempting to connect to server at {}:{}", host, port);


        StandardConsole console = new StandardConsole();
        ClientNetworkIO networkIO = null;
        try {
            networkIO = new ClientNetworkIO(host, port);
        } catch (IOException e) {
            console.printError("Failed to initialize network client: " + e.getMessage());
            logger.fatal("Network initialization failed. Exiting.", e);
            System.exit(1);
        }

        CommandManager commandManager = new CommandManager();
        // Runner needs networkIO now only for passing to ExecuteScript perhaps,
        // or if commands need direct access (unlikely with current design)
        Runner runner = new Runner(console, commandManager, networkIO);

        // Register client-side commands
        // Pass networkIO and console to commands that need them
        commandManager.register("help", new Help(networkIO, console));
        commandManager.register("info", new Info(networkIO, console));
        commandManager.register("show", new Show(networkIO, console));
        commandManager.register("add", new Add(networkIO, console));
        commandManager.register("update", new Update(networkIO, console));
        commandManager.register("remove_by_id", new RemoveById(networkIO, console));
        commandManager.register("clear", new Clear(networkIO, console));
        // commandManager.register("save", ...); // SAVE IS SERVER-SIDE ONLY - DO NOT REGISTER ON CLIENT
        commandManager.register("execute_script", new ExecuteScript(networkIO, console, runner)); // Pass runner
        commandManager.register("exit", new Exit(networkIO, console)); // Exit is client-side
        commandManager.register("add_if_max", new AddIfMax(networkIO, console));
        commandManager.register("remove_lower", new RemoveLower(networkIO, console));
        commandManager.register("history", new History(networkIO, console, commandManager)); // History command asks server now
        commandManager.register("min_by_id", new MinById(networkIO, console));
        commandManager.register("max_by_start_date", new MaxByStartDate(networkIO, console));
        commandManager.register("count_by_start_date", new CountByStartDate(networkIO, console));

        logger.info("Client setup complete. Starting interactive mode.");
        runner.interactiveMode(); // Start the main client loop

        // Cleanup after interactive mode finishes (e.g., user types exit)
        logger.info("Client shutting down...");
        if (networkIO != null) {
            networkIO.close();
        }
        logger.info("Client finished.");
        System.exit(0);
    }
}