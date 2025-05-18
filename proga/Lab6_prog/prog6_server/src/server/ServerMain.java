package server;

import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.*;
import server.handlers.CommandHandler;
import server.managers.CollectionManager;
import server.managers.CommandManager;
import server.managers.DumpManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class ServerMain {
    private static final int BUFFER_SIZE = 8192;
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    private ServerConsole serverConsole;
    private Thread serverConsoleThread;

    private final int port;
    private final String filePath;
    private DatagramChannel channel;
    private Selector selector;
    private CollectionManager collectionManager;
    private CommandManager commandManager;
    private CommandHandler commandHandler;
    private DumpManager dumpManager;
    private volatile boolean isRunning = true; // Для возможности остановки

    public ServerMain(int port, String filePath) {
        this.port = port;
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        int port;
        String filePath = System.getenv("FileLab6");

        if (filePath == null || filePath.isEmpty()) {
            logger.error("Переменная окружения 'FileLab6' не установлена или пуста.");
            System.exit(1);
        }

        File dataFile = new File(filePath);
        if (!dataFile.exists()){
            logger.info("Файл данных {} не найден. Попытка создать.", filePath);
            try {
                dataFile.createNewFile();
                logger.info("Файл данных {} успешно создан.", filePath);
            } catch (IOException e){
                logger.error("Не удалось создать файл данных {}: {}", filePath, e.getMessage());
                System.exit(1);
            }
        }
        if (!dataFile.canRead() || !dataFile.canWrite()) {
            logger.error("Нет прав на чтение/запись для файла: {}", filePath);
            System.exit(1);
        }


        try {
            port = (args.length > 0) ? Integer.parseInt(args[0]) : 14881; // Пример порта
            if (port <= 1024 || port > 65535) throw new NumberFormatException("Порт должен быть между 1025 и 65535");
        } catch (NumberFormatException e) {
            logger.error("Неверный формат порта или порт не указан. Используется порт по умолчанию 54321.", e);
            port = 54321; // Убедитесь, что это значение корректно
        }


        ServerMain server = new ServerMain(port, filePath);
        server.initialize();
        server.run();
    }

    private void initialize() {
        logger.info("Инициализация сервера...");
        try {
            // 2. Настройка сети
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_READ);
            logger.info("Сервер слушает порт: {}", port);

            // 3. Настройка менеджеров
            dumpManager = new DumpManager(filePath, logger);
            collectionManager = new CollectionManager(dumpManager, logger);
            commandManager = new CommandManager();
            commandHandler = new CommandHandler(commandManager);

            if (!collectionManager.loadCollection()) {
                logger.error("Не удалось загрузить коллекцию. Завершение работы.");
                System.exit(1);
            } else {
                logger.info("Коллекция успешно загружена. Количество элементов: {}", collectionManager.getCollection().size());
            }

            registerCommands();
            logger.info("Команды сервера зарегистрированы.");

            serverConsole = new ServerConsole(commandManager, this);
            serverConsoleThread = new Thread(serverConsole, "ServerConsoleThread");
            serverConsoleThread.start();
            logger.info("Серверная консоль запущена.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Получен сигнал завершения. Сохранение коллекции...");
                isRunning = false;
                try { Thread.sleep(100); } catch (InterruptedException ignored) { Thread.currentThread().interrupt();}
                collectionManager.saveCollection();
                logger.info("Коллекция сохранена. Завершение работы сервера.");
                closeResources();
            }));

        } catch (IOException e) {
            logger.fatal("Ошибка инициализации сервера: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void registerCommands() {
        commandManager.register("help", new Help(commandManager)); // Убрали Console
        commandManager.register("history", new History(commandManager)); // Убрали Console
        commandManager.register("info", new Info(collectionManager)); // Убрали Console
        commandManager.register("show", new Show(collectionManager)); // Убрали Console
        commandManager.register("add", new Add(collectionManager)); // Убрали Console
        commandManager.register("update", new Update(collectionManager)); // Убрали Console
        commandManager.register("remove_by_id", new RemoveById(collectionManager)); // Убрали Console
        commandManager.register("clear", new Clear(collectionManager)); // Убрали Console
        commandManager.register("save", new Save(collectionManager)); // Убрали Console
        commandManager.register("add_if_max", new AddIfMax(collectionManager)); // Убрали Console
        commandManager.register("remove_lower", new RemoveLower(collectionManager)); // Убрали Console
        commandManager.register("min_by_id", new MinById(collectionManager)); // Убрали Console
        commandManager.register("count_by_start_date", new CountByStartDate(collectionManager)); // Убрали Console
        commandManager.register("max_by_start_date", new MaxByStartDate(collectionManager)); // Убрали Console
    }

    public void run() {
        logger.info("Сервер запущен и готов к приему запросов...");
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        while (isRunning) {
            try {
                int readyChannels = selector.select(); // Блокируемся до события
                if (!isRunning) break; // Проверка после возможного пробуждения от shutdown hook

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isValid() && key.isReadable()) {
                        handleRead(key, buffer);
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                logger.error("Ошибка ввода/вывода в главном цикле: {}", e.getMessage(), e);
                // Можно добавить логику перезапуска или остановки
            } catch (Exception e) {
                logger.error("Непредвиденная ошибка в главном цикле: {}", e.getMessage(), e);
            }
        }
        logger.info("Основной цикл сервера завершен.");
    }

    public void stopServer() {
        logger.info("Инициирована остановка сервера...");
        isRunning = false;
        if (selector != null) {
            selector.wakeup(); // Разбудить селектор, если он заблокирован на select()
        }
    }

    private void handleRead(SelectionKey key, ByteBuffer buffer) {
        DatagramChannel currentChannel = (DatagramChannel) key.channel();
        SocketAddress clientAddress = null;
        try {
            buffer.clear();
            clientAddress = currentChannel.receive(buffer);

            if (clientAddress != null) {
                buffer.flip(); // Готовим буфер к чтению
                logger.info("Получен пакет размером {} байт от {}", buffer.limit(), clientAddress);

                // Десериализация запроса
                Request request = deserializeRequest(buffer);

                if (request != null) {
                    logger.info("Десериализован запрос: {} от {}", request, clientAddress);
                    // Обработка запроса
                    Response response = commandHandler.handle(request);

                    // Сериализация и отправка ответа
                    sendResponse(currentChannel, response, clientAddress);
                } else {
                    logger.warn("Не удалось десериализовать запрос от {}", clientAddress);
                    // Можно отправить ответ об ошибке, если необходимо
                }
            } else {
                logger.warn("Получен пустой пакет?");
            }
        } catch (IOException e) {
            logger.error("Ошибка при обработке чтения от {}: {}", clientAddress, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при обработке чтения от {}: {}", clientAddress, e.getMessage(), e);
        } finally {
            buffer.clear(); // Очищаем буфер для следующего чтения
        }
    }

    private Request deserializeRequest(ByteBuffer buffer) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit());
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Request) ois.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            logger.error("Ошибка десериализации запроса: {}", e.getMessage());
            return null;
        }
    }

    private void sendResponse(DatagramChannel channel, Response response, SocketAddress clientAddress) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(response);
            oos.flush(); // Убедимся, что все записано в baos

            ByteBuffer responseBuffer = ByteBuffer.wrap(baos.toByteArray());

            int bytesSent = channel.send(responseBuffer, clientAddress);
            logger.info("Отправлен ответ размером {} байт клиенту {}: {}", bytesSent, clientAddress, response);

        } catch (IOException e) {
            logger.error("Ошибка сериализации или отправки ответа клиенту {}: {}", clientAddress, e.getMessage(), e);
        }
    }

    private void closeResources() {
        logger.info("Закрытие ресурсов сервера...");
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
                logger.debug("Селектор закрыт.");
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
                logger.debug("Канал датаграмм закрыт.");
            }
        } catch (IOException e) {
            logger.error("Ошибка при закрытии сетевых ресурсов: {}", e.getMessage(), e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}