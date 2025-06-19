package client.util;

import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;

public class SerializationUtil {
    private static final Logger logger = LogManager.getLogger(SerializationUtil.class);

    // Клиент сериализует запросы (Request)
    public static ByteBuffer serializeRequest(Request request) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
            oos.flush();
            logger.debug("Serialized Request: {} ({} bytes)", request.getCommandName(), baos.size());
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (NotSerializableException e) {
            logger.error("Object not serializable in request {}: {}", request.getCommandName(), e.getMessage(), e);
            throw e; // Повторно выбросить конкретное исключение
        }
    }

    // Клиент десериализует ответы (Response)
    public static Response deserializeResponse(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        if (!buffer.hasRemaining()) {
            logger.warn("Attempted to deserialize empty buffer.");
            throw new IOException("Received empty buffer, cannot deserialize response.");
        }
        // Создать BAIS с правильным смещением и лимитом
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.remaining());
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (obj instanceof Response) {
                Response response = (Response) obj;
                logger.debug("Deserialized Response. Success: {}, Message length: {}",
                        response.getExecutionResponse().getExitCode(),
                        response.getExecutionResponse().getMessage().length());
                return response;
            } else {
                logger.error("Received object is not of type Response: {}", (obj == null ? "null" : obj.getClass().getName()));
                throw new ClassCastException("Expected Response, received " + (obj == null ? "null" : obj.getClass().getName()));
            }
        } catch (EOFException e) {
            logger.error("EOFException during deserialization. Incomplete data received? Buffer remaining: {}", buffer.remaining(), e);
            throw e;
        } catch(IOException | ClassNotFoundException | ClassCastException e) {
            logger.error("Deserialization failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}