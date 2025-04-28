import commands.*;

import managers.CollectionManager;
import managers.CommandManager;

import managers.DumpManager;
import java.io.File;
import models.Worker;

import utility.StandardConsole;
import utility.Runner;

public class Main {
	public static void main(String[] args) {
		var console = new StandardConsole();

		String filePath = System.getenv("FileLab5");

		if (filePath == null || filePath.isEmpty()) {
			console.printError("Ошибка: Переменная окружения 'FileLab5' не установлена.");
			console.printError("Перед запуском выполните:");
			console.printError("  Linux/macOS: export FileLab5=/путь/к/файлу.xml");
			console.printError("  Windows: set FileLab5=C:\\путь\\к\\файлу.xml");
			System.exit(1);
		}

		// 2. Проверяем существование файла
		if (!new File(filePath).exists()) {
			console.printError("Файл не найден: " + filePath);
			System.exit(1);
		}



		var dumpManager = new DumpManager(filePath, console);
		var collectionManager = new CollectionManager(dumpManager);
		if (!collectionManager.loadCollection()) {
			System.exit(1);
		}

		CommandManager commandManager = new CommandManager();
		Runner runner = new Runner(console, commandManager);
		commandManager.register("help", new Help(console, commandManager));
		commandManager.register("history", new History(console, commandManager));
		commandManager.register("info", new Info(console, collectionManager));
		commandManager.register("show", new Show(console, collectionManager));
		commandManager.register("add", new Add(console, collectionManager));
		commandManager.register("update", new Update(console, collectionManager));
		commandManager.register("remove_by_id", new RemoveById(console, collectionManager));
		commandManager.register("clear", new Clear(console, collectionManager));
		commandManager.register("save", new Save(console, collectionManager));
		commandManager.register("execute_script", new ExecuteScript(console));
		commandManager.register("exit", new Exit(console));
		commandManager.register("add_if_max", new AddIfMax(console, collectionManager));
		commandManager.register("remove_lower", new RemoveLower(console, collectionManager));
		commandManager.register("min_by_id", new MinById(console, collectionManager));
		commandManager.register("count_by_start_date", new CountByStartDate(console, collectionManager));
		commandManager.register("max_by_start_date", new MaxByStartDate(console, collectionManager));
		runner.interactiveMode();



	}
}