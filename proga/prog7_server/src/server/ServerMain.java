package server;

import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.commands.*;
import server.db.DatabaseManager;
import server.handlers.CommandHandler;
import server.managers.CollectionManager;
import server.managers.CommandManager;
import server.util.SerializationUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMain {
    private static final int BUFFER_SIZE = 8192;
    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    private final int port;
    private DatagramChannel channel;
    private Selector selector;
    private CollectionManager collectionManager;
    private CommandManager commandManager;
    private CommandHandler commandHandler;
    private DatabaseManager databaseManager;
    private volatile boolean isRunning = true;

    private ExecutorService requestReaderPool;

    public ServerMain(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port;
        try {
            port = (args.length > 0) ? Integer.parseInt(args[0]) : 14881;
            if (port <= 1024 || port > 65535) throw new NumberFormatException("Порт должен быть между 1025 и 65535");
        } catch (NumberFormatException e) {
            logger.error("Неверный формат порта или порт не указан. Используется порт по умолчанию 14881.", e);
            port = 14881;
        }

        ServerMain server = new ServerMain(port);
        server.initialize();
        server.run();
    }

    private void initialize() {
        logger.info("Инициализация сервера...");
        try {
            int coreCount = Runtime.getRuntime().availableProcessors();
            requestReaderPool = Executors.newFixedThreadPool(coreCount);
            logger.info("Пул потоков для чтения запросов (FixedThreadPool) инициализирован с {} потоками.", coreCount);

            databaseManager = new DatabaseManager(logger);
            databaseManager.connect();
            databaseManager.initializeTables();
            logger.info("Менеджер базы данных инициализирован, таблицы готовы.");

            collectionManager = new CollectionManager(databaseManager);
            commandManager = new CommandManager();
            commandHandler = new CommandHandler(commandManager, databaseManager);

            collectionManager.loadCollection();

            registerCommands();
            logger.info("Команды сервера зарегистрированы.");

            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_READ);
            logger.info("Сервер слушает порт: {}", port);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));

        } catch (Exception e) {
            logger.fatal("Не удалось инициализировать сервер: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private void registerCommands() {
        commandManager.register("register", new Register(databaseManager));

        commandManager.register("add", new Add(collectionManager));
        commandManager.register("update", new Update(collectionManager));
        commandManager.register("remove_by_id", new RemoveById(collectionManager));
        commandManager.register("clear", new Clear(collectionManager));
        commandManager.register("add_if_max", new AddIfMax(collectionManager));
        commandManager.register("remove_lower", new RemoveLower(collectionManager));

        commandManager.register("help", new Help(commandManager));
        commandManager.register("info", new Info(collectionManager));
        commandManager.register("show", new Show(collectionManager));
        commandManager.register("history", new History(commandManager));
        commandManager.register("min_by_id", new MinById(collectionManager));
        commandManager.register("count_by_start_date", new CountByStartDate(collectionManager));
        commandManager.register("max_by_start_date", new MaxByStartDate(collectionManager));

    }

    public void run() {
        logger.info("Сервер запущен и готов принимать запросы...");
        while (isRunning) {
            try {
                int readyChannels = selector.select();
                if (!isRunning) break;
                if (readyChannels == 0) continue;

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid() && key.isReadable()) {
                        handleRead(key);
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                if (isRunning) logger.error("Ошибка ввода-вывода в главном цикле: {}", e.getMessage(), e);
            } catch (Exception e) {
                if (isRunning) logger.error("Непредвиденная ошибка в главном цикле: {}", e.getMessage(), e);
            }
        }
        logger.info("Главный цикл сервера завершен.");
    }

    public synchronized void stopServer() {
        if (!isRunning) return;
        logger.info("Начата последовательность завершения работы...");
        isRunning = false;

        if(selector != null) selector.wakeup();

        requestReaderPool.shutdown();
        try {
            if (!requestReaderPool.awaitTermination(5, TimeUnit.SECONDS)) {
                requestReaderPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            requestReaderPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            if (selector != null && selector.isOpen()) selector.close();
            if (channel != null && channel.isOpen()) channel.close();
        } catch (IOException e) {
            logger.error("Ошибка при закрытии сетевых ресурсов: {}", e.getMessage());
        }

    }

    private void handleRead(SelectionKey key) {
        DatagramChannel currentChannel = (DatagramChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        try {
            SocketAddress clientAddress = currentChannel.receive(buffer);
            if (clientAddress != null) {
                buffer.flip();
                requestReaderPool.submit(new RequestReaderTask(buffer, clientAddress));
            }
        } catch (IOException e) {
            logger.error("Ошибка чтения из канала: {}", e.getMessage(), e);
        }
    }

    /**
     * Этап 1: Десериализация запроса. Выполняется в FixedThreadPool.
     */
    private class RequestReaderTask implements Runnable {
        private final ByteBuffer buffer;
        private final SocketAddress clientAddress;

        public RequestReaderTask(ByteBuffer buffer, SocketAddress clientAddress) {
            this.buffer = buffer;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            try {
                Request request = SerializationUtil.deserializeRequest(buffer);
                logger.info("Десериализован запрос '{}' от {}", request, clientAddress);

                // Задача 2: Создаем НОВЫЙ поток для обработки
                Thread processingThread = new Thread(new RequestProcessorTask(request, clientAddress));
                processingThread.setName("Processor-" + clientAddress.toString());
                processingThread.start();

            } catch (IOException | ClassNotFoundException e) {
                logger.error("Не удалось десериализовать запрос от {}: {}", clientAddress, e.getMessage());
            }
        }
    }

    /**
     * Этап 2: Обработка команды. Выполняется в собственном НОВОМ потоке.
     */
    private class RequestProcessorTask implements Runnable {
        private final Request request;
        private final SocketAddress clientAddress;

        public RequestProcessorTask(Request request, SocketAddress clientAddress) {
            this.request = request;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            Response response = commandHandler.handle(request);

            // Задача 3: Создаем НОВЫЙ поток для отправки ответа
            Thread sendingThread = new Thread(new ResponseSenderTask(response, clientAddress));
            sendingThread.setName("Sender-" + clientAddress.toString());
            sendingThread.start();
        }
    }

    /**
     * Этап 3: Сериализация и отправка ответа. Выполняется в собственном НОВОМ потоке.
     */
    private class ResponseSenderTask implements Runnable {
        private final Response response;
        private final SocketAddress clientAddress;

        public ResponseSenderTask(Response response, SocketAddress clientAddress) {
            this.response = response;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            try {
                ByteBuffer responseBuffer = SerializationUtil.serializeResponse(response);
                int bytesSent = channel.send(responseBuffer, clientAddress);
                logger.info("Отправлен ответ ({} байт) на {}: {}", bytesSent, clientAddress, response);
            } catch (IOException e) {
                if(isRunning) logger.error("Не удалось отправить ответ на {}: {}", clientAddress, e.getMessage(), e);
            }
        }
    }
}