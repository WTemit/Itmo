package client.commands;

import client.managers.UserManager;
import client.network.ClientNetworkIO;
import client.util.StandardConsole;
import common.dto.Request;
import common.dto.Response;
import common.dto.User;
import common.interfaces.Describable;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Abstract base class for all client-side commands.
 * It holds references to necessary managers and handles request creation.
 */
public abstract class Command implements Describable {
	protected static final Logger logger = LogManager.getLogger(Command.class);
	private final String name;
	private final String description;
	protected final ClientNetworkIO networkIO;
	protected final StandardConsole console;
	protected final UserManager userManager;

	public Command(String name, String description, ClientNetworkIO networkIO, StandardConsole console, UserManager userManager) {
		this.name = name;
		this.description = description;
		this.networkIO = networkIO;
		this.console = console;
		this.userManager = userManager;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Executes the command logic.
	 * @param arguments Command-line arguments.
	 * @return The result of the command execution.
	 */
	public abstract ExecutionResponse apply(String[] arguments);

	/**
	 * A helper method to create a Request with the current user's credentials.
	 * This should be used by all commands that need to communicate with the server.
	 * @param commandName Name of the command.
	 * @param stringArg String argument for the command.
	 * @param objectArg Serializable object argument for the command.
	 * @return A new Request object, populated with user credentials.
	 */
	protected Request createRequest(String commandName, String stringArg, java.io.Serializable objectArg) {
		User currentUser = userManager.getCurrentUser();
		return new Request(commandName, stringArg, objectArg, currentUser);
	}

	/**
	 * Helper method to send a request and handle the response.
	 * @param request The request to send.
	 * @return The execution response from the server.
	 */
	protected ExecutionResponse sendRequestAndGetResponse(Request request) {
		Response serverResponse = networkIO.sendRequestAndReceiveResponse(request);
		if (serverResponse == null) {
			return new ExecutionResponse(false, "Failed to receive a response from the server. The server might be down or unreachable.");
		}
		return serverResponse.getExecutionResponse();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Command command = (Command) o;
		return Objects.equals(name, command.name) && Objects.equals(description, command.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description);
	}
}