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
		
		var commandManager = new CommandManager() {{
			register("help", new Help(console, this));
			register("history", new History(console, this));
			register("info", new Info(console, collectionManager));
			register("show", new Show(console, collectionManager));
			register("add", new Add(console, collectionManager));
			register("update", new Update(console, collectionManager));
			register("remove_by_id", new RemoveById(console, collectionManager));
			register("clear", new Clear(console, collectionManager));
			register("save", new Save(console, collectionManager));
			register("execute_script", new ExecuteScript(console));
			register("exit", new Exit(console));
			register("add_if_max", new AddIfMax(console, collectionManager));
			register("remove_lower", new RemoveLower(console, collectionManager));
			register("min_by_id", new MinById(console, collectionManager));
			register("count_by_start_date", new CountByStartDate(console, collectionManager));
			register("max_by_start_date", new MaxByStartDate(console, collectionManager));
		}};

		new Runner(console, commandManager).interactiveMode();
	}
}