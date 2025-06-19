package client.commands;

import common.util.ExecutionResponse;

public class Exit extends Command {
	public Exit() {
		super("exit", "завершить программу клиента", null, null, null);
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		return new ExecutionResponse(true, "exit");
	}
}