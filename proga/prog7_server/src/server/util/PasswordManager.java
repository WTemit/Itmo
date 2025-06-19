package server.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordManager {
    private static final Logger logger = LogManager.getLogger(PasswordManager.class);
    private static final String HASH_ALGORITHM = "SHA-384";
    private static final int SALT_LENGTH = 16; // 16 байт = 128 бит

    /**
     * Генерирует случайную соль.
     * @return "Соль" в виде шестнадцатеричной строки.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return new BigInteger(1, salt).toString(16);
    }

    /**
     * Хеширует пароль, используя SHA-384.
     * @param password Пароль в виде обычного текста.
     * @param salt "Соль", используемая для хеширования.
     * @return Хешированный пароль в виде шестнадцатеричной строки или null в случае ошибки.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            // Добавляем "соль" к паролю перед хешированием
            byte[] saltedPassword = (password + salt).getBytes(StandardCharsets.UTF_8);
            byte[] digest = md.digest(saltedPassword);
            // Преобразуем массив байтов в шестнадцатеричную строку
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            logger.fatal("Не удалось найти алгоритм хеширования: " + HASH_ALGORITHM, e);
            System.exit(1);
            return null;
        }
    }
}