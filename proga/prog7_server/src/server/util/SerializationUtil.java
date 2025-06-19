package server.util;

import common.dto.Request;
import common.dto.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;

public class SerializationUtil {
    private static final Logger logger = LogManager.getLogger(SerializationUtil.class);

    public static Request deserializeRequest(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.limit());
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (obj instanceof Request) {
                return (Request) obj;
            } else {
                throw new ClassCastException("Ожидался Request, получен " + (obj == null ? "null" : obj.getClass().getName()));
            }
        }
    }

    public static ByteBuffer serializeResponse(Response response) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            oos.flush();
            return ByteBuffer.wrap(baos.toByteArray());
        }
    }
}