package client.commands;

import client.util.Runner;
import client.util.StandardConsole;
import common.util.ExecutionResponse;

public class ExecuteScript extends Command {
	private final Runner runner;

	public ExecuteScript(StandardConsole console, Runner runner) {
		super("execute_script", "исполнить скрипт из указанного файла", null, console, null);
		this.runner = runner;
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length < 2 || arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Usage: " + getName() + " {file_name}");
		}
		String filename = arguments[1].trim();
		return runner.scriptMode(filename);
	}
}