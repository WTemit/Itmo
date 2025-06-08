package client.network;

import client.util.SerializationUtil;
import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class ClientNetworkIO {
    private static final Logger logger = LogManager.getLogger(ClientNetworkIO.class);
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 1000;

    private final String serverHost;
    private final int serverPort;
    private DatagramChannel channel;
    private SocketAddress serverAddress;
    private Selector selector;

    public ClientNetworkIO(String serverHost, int serverPort) throws IOException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        setupConnection();
    }

    private void setupConnection() throws IOException {
        try {
            serverAddress = new InetSocketAddress(serverHost, serverPort);
            channel = DatagramChannel.open();
            channel.configureBlocking(false); // Неблокирующий режим
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            logger.info("DatagramChannel открыт и настроен для неблокирующего ввода-вывода. Целевой сервер: {}", serverAddress);
        } catch (IOException e) {
            logger.error("Не удалось настроить сетевой канал или селектор: {}", e.getMessage(), e);
            throw e; // Распространить исключение
        }
    }

    public Response sendRequestAndReceiveResponse(Request request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                // Отправить запрос
                ByteBuffer sendBuffer = SerializationUtil.serializeRequest(request);
                logger.debug("Попытка {}/{}: Отправка запроса {} на {}", attempt, MAX_RETRIES, request.getCommandName(), serverAddress);
                channel.send(sendBuffer, serverAddress); // Использовать send с адресом, если не подключено

                // Получить ответ с тайм-аутом
                ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                logger.debug("Ожидание ответа (макс. {} мс)...", RETRY_DELAY_MS * 2); // Тайм-аут немного дольше, чем задержка повтора

                // Использовать Selector для неблокирующего чтения с тайм-аутом
                int readyChannels = selector.select(RETRY_DELAY_MS * 2); // Тайм-аут для select

                if (readyChannels == 0) {
                    logger.warn("Попытка {}/{}: Ответ не получен в течение тайм-аута.", attempt, MAX_RETRIES);
                    continue; // Перейти к следующей попытке повтора
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        DatagramChannel readableChannel = (DatagramChannel) key.channel();
                        SocketAddress senderAddress = readableChannel.receive(receiveBuffer);

                        if (senderAddress == null) {
                            logger.warn("Попытка {}/{}: Получен нулевой адрес отправителя (ложное пробуждение?). Повторная попытка.", attempt, MAX_RETRIES);
                            keyIterator.remove();
                            continue; // Не должно происходить часто
                        }

                        receiveBuffer.flip(); // Подготовить буфер для чтения
                        logger.debug("Попытка {}/{}: Получено {} байт от {}", attempt, MAX_RETRIES, receiveBuffer.limit(), senderAddress);

                        try {
                            Response response = SerializationUtil.deserializeResponse(receiveBuffer);
                            keyIterator.remove(); // Обработали ключ
                            return response; // Успех!
                        } catch (IOException | ClassNotFoundException | ClassCastException e) {
                            logger.error("Попытка {}/{}: Не удалось десериализовать ответ: {}", attempt, MAX_RETRIES, e.getMessage(), e);
                            keyIterator.remove(); // Обработали ключ
                            return null; // Указать ошибку
                        }
                    }
                    keyIterator.remove();
                }
                logger.warn("Цикл селектора завершился без обработки читаемого ключа (Попытка {}).", attempt);

            } catch (SocketTimeoutException e) {
                logger.warn("Попытка {}/{}: Время ожидания сокета истекло при ожидании ответа.", attempt, MAX_RETRIES);
            } catch (IOException e) {
                logger.error("Попытка {}/{}: Ошибка сетевого ввода-вывода: {}", attempt, MAX_RETRIES, e.getMessage(), e);
                return null;
            }

            // Подождать перед повторной попыткой
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Задержка повтора прервана.");
                    return null; // Остановиться, если прервано
                }
            }
        }

        logger.error("Не удалось получить ответ от сервера после {} попыток.", MAX_RETRIES);
        return null; // Не удалось после всех повторных попыток
    }

    public void close() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
                logger.info("Селектор закрыт.");
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
                logger.info("DatagramChannel закрыт.");
            }
        } catch (IOException e) {
            logger.error("Ошибка при закрытии сетевых ресурсов: {}", e.getMessage(), e);
        }
    }
}