package client.util;

import client.commands.Command;
import client.managers.CommandManager;
import client.managers.UserManager;
import client.network.ClientNetworkIO;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Runner {
	private static final Logger logger = LogManager.getLogger(Runner.class);
	private final StandardConsole console;
	private final CommandManager commandManager;
	private final ClientNetworkIO networkIO;
	private final UserManager userManager; // <-- ADDED THIS FIELD
	private final List<String> scriptStack = new ArrayList<>();

	// UPDATED CONSTRUCTOR
	public Runner(StandardConsole console, CommandManager commandManager, ClientNetworkIO networkIO, UserManager userManager) {
		this.console = console;
		this.commandManager = commandManager;
		this.networkIO = networkIO;
		this.userManager = userManager; // <-- ADDED THIS INITIALIZATION
	}

	/**
	 * Interactive mode: Reads commands from the console.
	 */
	public void interactiveMode() {
		logger.info("Entering interactive mode...");
		// Set Ask to use the console scanner
		Ask.setScanner(new Scanner(System.in), false);
		try {
			String[] userCommand = {"", ""};

			while (true) {
				console.prompt();
				try {
					String line = console.readln();
					userCommand = (line.trim() + " ").split(" ", 2);
					userCommand[1] = userCommand[1].trim();
				} catch (NoSuchElementException e) {
					logger.warn("Input stream closed (Ctrl+D?). Exiting.");
					break;
				} catch (IllegalStateException e) {
					logger.error("Console reader is in an invalid state. Exiting.", e);
					break;
				}

				if (userCommand[0].isEmpty()) {
					continue;
				}

				commandManager.addToHistory(userCommand[0]);
				ExecutionResponse commandStatus = launchCommand(userCommand);

				if (commandStatus != null && "exit".equals(commandStatus.getMessage()) && commandStatus.getExitCode()) {
					logger.info("Exit command successful. Terminating client.");
					break;
				}

				if (commandStatus != null) {
					if (commandStatus.getExitCode()){
						console.println(commandStatus.getMessage());
					} else {
						console.printError(commandStatus.getMessage());
					}
				} else {
					console.printError("Received null response, command execution may have failed with an internal error.");
				}
			}
		} catch (Exception e) {
			logger.fatal("An unhandled exception occurred in interactive mode: {}", e.getMessage(), e);
			console.printError("A critical error occurred. Please check the logs. Exiting.");
		} finally {
			logger.info("Exiting interactive mode.");
		}
	}

	/**
	 * Script mode: Executes commands from a file.
	 * @param filename Path to the script file.
	 * @return ExecutionResponse indicating the result of the script execution.
	 */
	public ExecutionResponse scriptMode(String filename) {
		File scriptFile = new File(filename);
		if (!scriptFile.exists()) {
			logger.error("Script file not found: {}", filename);
			return new ExecutionResponse(false, "Script file does not exist: " + filename);
		}
		if (!scriptFile.canRead()) {
			logger.error("Script file cannot be read (permissions error): {}", filename);
			return new ExecutionResponse(false, "No read permissions for script file: " + filename);
		}

		String absolutePath = scriptFile.getAbsolutePath();
		if (scriptStack.contains(absolutePath)) {
			logger.error("Recursion detected! Script '{}' is already running.", absolutePath);
			return new ExecutionResponse(false, "Recursion detected! Script '" + filename + "' is already running.");
		}

		scriptStack.add(absolutePath);
		logger.info("Entering script mode: {}", filename);
		StringBuilder scriptOutput = new StringBuilder();
		boolean scriptOk = true;

		Object savedAskState = Ask.saveState();

		try (Scanner fileScanner = new Scanner(scriptFile)) {
			Ask.setScanner(fileScanner, true);

			while (fileScanner.hasNextLine()) {
				String line = "";
				try {
					line = fileScanner.nextLine().trim();
					scriptOutput.append(console.getPrompt()).append(line).append("\n");

					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}

					String[] scriptCommand = (line + " ").split(" ", 2);
					scriptCommand[1] = scriptCommand[1].trim();

					logger.debug("Executing script command: [{}] with args [{}]", scriptCommand[0], scriptCommand[1]);
					commandManager.addToHistory(scriptCommand[0]);
					ExecutionResponse response = launchCommand(scriptCommand);


					if (response == null) {
						scriptOutput.append("Error: Command execution resulted in an internal error (null response).\n");
						logger.error("Command [{}] in script [{}] resulted in a null response.", scriptCommand[0], filename);
						scriptOk = false;
						break;
					} else if ("exit".equals(response.getMessage()) && response.getExitCode()) {
						scriptOutput.append("Exit command encountered in script. Terminating client.\n");
						logger.info("Exit command in script '{}'. Terminating.", filename);
						Ask.restoreState(savedAskState);
						scriptStack.remove(absolutePath);
						return response;
					} else if (!response.getExitCode()) {
						scriptOutput.append("Error: ").append(response.getMessage()).append("\n");
						logger.warn("Command [{}] in script [{}] failed: {}", scriptCommand[0], filename, response.getMessage());
						scriptOk = false;
					} else {
						scriptOutput.append(response.getMessage()).append("\n");
					}

				} catch (NoSuchElementException e){
					logger.debug("End of script file {} reached.", filename);
					break;
				} catch (Exception e) {
					logger.error("Error processing line '{}' in script {}: {}", line, filename, e.getMessage(), e);
					scriptOk = false;
					scriptOutput.append("Critical error during script execution: ").append(e.getMessage()).append("\n");
					break;
				}
			}

		} catch (FileNotFoundException e) {
			logger.error("Script file not found during execution: {}", filename, e);
			scriptOk = false;
			scriptOutput.append("Error: Script file not found: ").append(filename).append("\n");
		} catch (Exception e) {
			logger.error("Error executing script {}: {}", filename, e.getMessage(), e);
			scriptOk = false;
			scriptOutput.append("Critical error during script execution: ").append(e.getMessage()).append("\n");
		} finally {
			Ask.restoreState(savedAskState);
			scriptStack.remove(absolutePath);
			logger.info("Exiting script mode: {}", filename);
		}

		return new ExecutionResponse(scriptOk, scriptOutput.toString().trim());
	}


	/**
	 * Finds and launches a command.
	 * @param userCommand Array with command name and arguments.
	 * @return ExecutionResponse from the command's apply method or an error response.
	 */
	private ExecutionResponse launchCommand(String[] userCommand) {
		String commandName = userCommand[0];
		if (commandName.isEmpty()) {
			return new ExecutionResponse(true, "");
		}

		Command command = commandManager.getCommands().get(commandName);

		if (command == null) {
			logger.warn("Command '{}' not found.", commandName);
			return new ExecutionResponse(false, "Command '" + commandName + "' not found. Type 'help' for a list of commands.");
		}

		logger.debug("Launching command: {}", commandName);
		try {
			return command.apply(userCommand);
		} catch (Exception e) {
			logger.error("Exception during execution of command '{}': {}", commandName, e.getMessage(), e);
			return new ExecutionResponse(false, "Error while executing command '" + commandName + "': " + e.getMessage());
		}
	}
}