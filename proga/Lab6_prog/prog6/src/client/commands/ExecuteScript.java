package client.commands;

import client.network.ClientNetworkIO;
import client.util.Runner; // Нужна ссылка на Runner
import client.util.StandardConsole;
import common.util.ExecutionResponse;

public class ExecuteScript extends Command {
	private final Runner runner; // Ссылка на Runner для запуска режима скрипта

	public ExecuteScript(ClientNetworkIO networkIO, StandardConsole console, Runner runner) {
		super("execute_script ", "исполнить скрипт из указанного файла", networkIO, console);
		this.runner = runner;
	}

	@Override
	public ExecutionResponse apply(String[] arguments) {
		if (arguments.length < 2 || arguments[1].isEmpty()) {
			return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + " file_name'");
		}
		String filename = arguments[1].trim();
		logger.info("Команда ExecuteScript вызвана для файла: {}", filename);

		// Делегируем выполнение скрипта Runner'у
		// scriptMode в Runner'е обработает чтение файла, парсинг команд,
		// переключение сканера Ask и сетевые вызовы.
		return runner.scriptMode(filename);
	}
}