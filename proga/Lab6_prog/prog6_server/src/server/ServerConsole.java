package server; // или server.util

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.Save;
import server.managers.CommandManager; // Серверный CommandManager

import java.util.Scanner;

public class ServerConsole implements Runnable { // Делаем Runnable для запуска в отдельном потоке
    private static final Logger consoleLogger = LogManager.getLogger(ServerConsole.class);
    private final CommandManager serverCommandManager;
    private final ServerMain serverInstance; // Ссылка на главный класс сервера для остановки
    private volatile boolean consoleIsRunning = true;

    public ServerConsole(CommandManager serverCommandManager, ServerMain serverInstance) {
        this.serverCommandManager = serverCommandManager;
        this.serverInstance = serverInstance;
    }

    @Override
    public void run() {
        consoleLogger.info("Серверная консоль запущена. Доступные команды: 'save', 'stop'.");
        Scanner scanner = new Scanner(System.in);

        while (consoleIsRunning && serverInstance.isRunning()) { // Проверяем оба флага
            System.out.print("server-console> "); // Приглашение для серверной консоли
            if (scanner.hasNextLine()) {
                String commandLine = scanner.nextLine().trim();
                if (commandLine.isEmpty()) {
                    continue;
                }

                // Обработка команд
                if ("save".equalsIgnoreCase(commandLine)) {
                    handleSaveCommand();
                } else if ("stop".equalsIgnoreCase(commandLine)) {
                    handleStopCommand();
                    // consoleIsRunning установится в false внутри handleStopCommand
                } else {
                    consoleLogger.warn("Неизвестная серверная команда: '{}'. Доступные: 'save', 'stop'.", commandLine);
                }
            } else {
                // Scanner закрыт (например, Ctrl+D в System.in)
                consoleLogger.info("Ввод для серверной консоли завершен.");
                // Если сервер еще работает, но консольный ввод закончился,
                // можно либо остановить сервер, либо просто завершить этот поток.
                // Для простоты, завершим этот поток.
                consoleIsRunning = false;
            }
        }
        scanner.close(); // Закрываем сканер при выходе из цикла
        consoleLogger.info("Серверная консоль завершила свою работу.");
    }

    private void handleSaveCommand() {
        consoleLogger.debug("Обработка серверной команды 'save'.");
        // Получаем команду Save из CommandManager'а
        server.commands.Command cmd = serverCommandManager.getCommands().get("save");
        if (cmd instanceof Save) {
            Save saveCommand = (Save) cmd;
            String result = saveCommand.executeSaveForServerConsole();
            System.out.println("Результат 'save': " + result); // Выводим результат в консоль сервера
        } else {
            String errorMessage = "Команда 'save' не найдена или имеет неверный тип в CommandManager'е.";
            consoleLogger.error(errorMessage);
            System.err.println(errorMessage);
        }
    }

    private void handleStopCommand() {
        consoleLogger.info("Серверная команда 'stop' получена. Инициирование остановки сервера...");
        consoleIsRunning = false; // Останавливаем цикл консоли
        serverInstance.stopServer(); // Вызываем метод остановки в ServerMain
    }

    // Метод для внешней остановки консоли, если потребуется
    public void shutdown() {
        consoleIsRunning = false;
        // Можно попытаться прервать поток, если он заблокирован на scanner.hasNextLine(),
        // но закрытие System.in - плохая идея. Лучше дать ему завершиться естественно.
    }
}