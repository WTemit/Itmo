package client.util;

import common.models.*;
import common.models.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.time.ZonedDateTime;

public class Ask {
    private static final Logger logger = LogManager.getLogger(Ask.class);

    // --- Внутреннее состояние ---
    private static boolean scriptMode = false;
    private static Scanner currentScanner; // Теперь один Scanner для текущего режима
    private static final Scanner systemInScanner = new Scanner(System.in); // Всегда держим ссылку на System.in

    // Инициализация currentScanner при загрузке класса
    static {
        currentScanner = systemInScanner;
    }
    // --- Конец внутреннего состояния ---


    public static class AskBreak extends Exception {}

    // --- Класс для сохранения состояния ---
    private static class AskState {
        final Scanner scanner;
        final boolean mode;

        AskState(Scanner scanner, boolean mode) {
            this.scanner = scanner;
            this.mode = mode;
        }
    }
    // --- Конец класса для сохранения состояния ---


    /**
     * Сохраняет текущее состояние Ask (сканер и режим).
     * @return Объект, содержащий сохраненное состояние.
     */
    public static Object saveState() {
        logger.trace("Сохранение состояния Ask: Mode={}, Scanner={}", scriptMode, currentScanner.getClass().getSimpleName());
        return new AskState(currentScanner, scriptMode);
    }

    /**
     * Восстанавливает ранее сохраненное состояние Ask.
     * @param savedState Объект состояния, полученный из saveState().
     */
    public static void restoreState(Object savedState) {
        if (savedState instanceof AskState) {
            AskState state = (AskState) savedState;
            currentScanner = state.scanner;
            scriptMode = state.mode;
            logger.trace("Восстановление состояния Ask: Mode={}, Scanner={}", scriptMode, currentScanner.getClass().getSimpleName());
        } else {
            logger.error("Не удалось восстановить состояние Ask: неверный тип объекта состояния.");
            // Возвращаемся к консольному режиму по умолчанию в случае ошибки
            currentScanner = systemInScanner;
            scriptMode = false;
        }
    }


    /**
     * Устанавливает Scanner для использования при вводе.
     * @param scanner Сканер (например, из System.in или файла).
     * @param isScript true, если сканер читает из файла скрипта.
     */
    public static void setScanner(Scanner scanner, boolean isScript) {
        // Не закрываем предыдущий сканер здесь, особенно если это System.in!
        currentScanner = (scanner != null) ? scanner : systemInScanner; // Безопасное присваивание
        scriptMode = isScript;
        logger.trace("Установка сканера Ask: Mode={}, Scanner={}", scriptMode, currentScanner.getClass().getSimpleName());
    }

    /**
     * Получает текущую строку ввода, обрабатывая эхо скрипта.
     * @return Обрезанная строка ввода.
     * @throws NoSuchElementException Если ввод недоступен.
     * @throws IllegalStateException Если Scanner закрыт.
     * @throws AskBreak Если пользователь вводит 'exit'.
     */
    private static String readLine() throws NoSuchElementException, IllegalStateException, AskBreak {
        if (currentScanner == null) {
            logger.error("Ошибка Ask: Текущий сканер null (ScriptMode: {})", scriptMode);
            throw new IllegalStateException("Сканер ввода не настроен.");
        }
        if (!currentScanner.hasNextLine()){
            logger.warn("Нет следующей строки для чтения (ScriptMode: {}).", scriptMode);
            // Если это скрипт, значит он закончился. Если консоль - ждем или Ctrl+D.
            throw new NoSuchElementException("Нет доступных строк для чтения.");
        }

        String line = currentScanner.nextLine().trim();
        if (scriptMode) {
            System.out.println(line); // Эхо ввода скрипта
        }
        if ("exit".equalsIgnoreCase(line)) {
            logger.info("Пользователь инициировал 'exit' во время ввода.");
            throw new AskBreak();
        }
        return line;
    }

    // Остальные методы askString, askLong, askWorker и т.д. остаются без изменений
    // ... (весь предыдущий код методов ask*) ...
    /**
     * Prompts the user and reads a non-empty string.
     * @param prompt The message to display to the user.
     * @return The non-empty string entered by the user.
     * @throws AskBreak If the user enters 'exit'.
     */
    private static String askString(String prompt) throws AskBreak {
        String value;
        while (true) {
            System.out.print(prompt);
            try {
                value = readLine();
                if (!value.isEmpty()) {
                    return value;
                }
                if (!scriptMode) System.err.println("Ввод не может быть пустым.");
            } catch (NoSuchElementException e) {
                logger.error("Поток ввода неожиданно завершился.", e);
                throw e; // Перебрасываем критическое исключение
            } catch (IllegalStateException e) {
                logger.error("Сканер закрыт или в недопустимом состоянии.", e);
                throw e; // Перебрасываем критическое исключение
            }
        }
    }

    /**
     * Prompts the user and reads a Long, validating the condition.
     * @param prompt The message to display.
     * @param condition A lambda function (Long -> boolean) for validation.
     * @param errorMsg Message to show on validation failure.
     * @return The valid Long entered.
     * @throws AskBreak If 'exit' is entered.
     */
    private static Long askLong(String prompt, java.util.function.Predicate<Long> condition, String errorMsg) throws AskBreak {
        Long value;
        while (true) {
            System.out.print(prompt);
            try {
                String line = readLine();
                value = Long.parseLong(line);
                if (condition.test(value)) {
                    return value;
                } else {
                    if (!scriptMode) System.err.println("Ошибка валидации: " + errorMsg);
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                throw e;
            } catch (NumberFormatException e) {
                if (!scriptMode) System.err.println("Неверный формат числа. Введите целое число.");
            }
        }
    }

    /**
     * Prompts the user and reads an Integer, validating the condition.
     * @param prompt The message to display.
     * @param condition A lambda function (Integer -> boolean) for validation.
     * @param errorMsg Message to show on validation failure.
     * @return The valid Integer entered.
     * @throws AskBreak If 'exit' is entered.
     */
    private static Integer askInteger(String prompt, java.util.function.Predicate<Integer> condition, String errorMsg) throws AskBreak {
        Integer value;
        while (true) {
            System.out.print(prompt);
            try {
                String line = readLine();
                value = Integer.parseInt(line);
                if (condition.test(value)) {
                    return value;
                } else {
                    if (!scriptMode) System.err.println("Ошибка валидации: " + errorMsg);
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                throw e;
            } catch (NumberFormatException e) {
                if (!scriptMode) System.err.println("Неверный формат числа. Введите целое число.");
            }
        }
    }

    /**
     * Prompts the user and reads a Double, validating the condition.
     * Allows null input by entering an empty string.
     * @param prompt The message to display.
     * @param condition A lambda function (Double -> boolean) for validation (only applied if not null).
     * @param errorMsg Message to show on validation failure.
     * @return The valid Double entered, or null if empty string entered.
     * @throws AskBreak If 'exit' is entered.
     */
    private static Double askDoubleNullable(String prompt, java.util.function.Predicate<Double> condition, String errorMsg) throws AskBreak {
        Double value;
        while (true) {
            System.out.print(prompt + " (или нажмите Enter для null): ");
            try {
                String line = readLine();
                if (line.isEmpty()) {
                    return null; // Разрешаем null ввод
                }
                value = Double.parseDouble(line);
                if (condition.test(value)) {
                    return value;
                } else {
                    if (!scriptMode) System.err.println("Ошибка валидации: " + errorMsg);
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                throw e;
            } catch (NumberFormatException e) {
                if (!scriptMode) System.err.println("Неверный формат числа. Введите число.");
            }
        }
    }

    /**
     * Prompts the user and reads an Enum value.
     * Allows null input by entering an empty string.
     * @param <T> The enum type.
     * @param prompt The message to display.
     * @param enumClass The class of the enum.
     * @param names List of valid enum names (e.g., from Enum.names()).
     * @return The selected enum constant, or null.
     * @throws AskBreak If 'exit' is entered.
     */
    private static <T extends Enum<T>> T askEnumNullable(String prompt, Class<T> enumClass, String names) throws AskBreak {
        T value;
        while (true) {
            System.out.print(prompt + " (" + names + ", или нажмите Enter для null): ");
            try {
                String line = readLine();
                if (line.isEmpty()) {
                    return null; // Разрешаем null
                }
                // Преобразуем в верхний регистр для сравнения без учета регистра
                value = Enum.valueOf(enumClass, line.toUpperCase());
                return value;
            } catch (NoSuchElementException | IllegalStateException e) {
                throw e;
            } catch (IllegalArgumentException e) {
                if (!scriptMode) System.err.println("Неверное значение. Выберите из списка или нажмите Enter.");
            }
        }
    }


    // --- Методы для запроса конкретных common моделей ---

    /** Запрашивает у пользователя данные Worker. ID устанавливается сервером. Даты устанавливаются сервером. */
    public static Worker askWorker() throws AskBreak {
        try {
            System.out.println("* Введите данные Worker (введите 'exit' в любое время для отмены):");
            String name = askString("  Имя: ");
            Coordinates coordinates = askCoordinates();
            Double salary = askDoubleNullable("  Зарплата (> 0): ", s -> s > 0, "Зарплата должна быть больше 0.");
            Position position = askEnumNullable("  Должность", Position.class, Position.names());
            Organization organization = askOrganization();

            // ID и даты (creationDate, startDate, endDate) устанавливаются сервером
            // Создаем объект Worker без них изначально
            Worker worker = new Worker(null, name, coordinates, salary, position, organization);
            // Устанавливаем фиктивные даты, если нужно локально, но сервер их перезапишет
            worker.setCreationDate(ZonedDateTime.now()); // Заполнитель
            worker.setStartDate(ZonedDateTime.now());   // Заполнитель

            return worker;
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Ошибка ввода при создании Worker.", e);
            System.err.println("\nОшибка чтения ввода. Прерывание создания Worker.");
            return null; // Обозначаем неудачу
        }
    }

    public static Coordinates askCoordinates() throws AskBreak {
        try {
            System.out.println("  Введите Координаты:");
            // Из common.models.Coordinates: x > -472, не null; y не null
            Long x = askLong("    x (> -472): ", val -> val > -472, "Значение должно быть больше -472.");
            Integer y = askInteger("    y: ", val -> true, ""); // Нет специфичной валидации для y, кроме того, что это целое число

            return new Coordinates(x, y);
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Ошибка ввода при создании Coordinates.", e);
            System.err.println("\nОшибка чтения ввода. Прерывание создания Coordinates.");
            return null;
        }
    }

    public static Position askPosition() throws AskBreak {
        return askEnumNullable("  Должность", Position.class, Position.names());
    }

    public static Organization askOrganization() throws AskBreak {
        try {
            System.out.println("  Введите данные Организации (опционально, нажмите Enter в первом запросе для пропуска):");
            // Сначала спрашиваем годовой оборот, разрешая пропуск
            Integer annualTurnover;
            while (true) {
                System.out.print("    Годовой оборот (> 0, или нажмите Enter для пропуска Организации): ");
                String line = readLine();
                if (line.isEmpty()) return null; // Пропускаем организацию
                try {
                    annualTurnover = Integer.parseInt(line);
                    if (annualTurnover > 0) break;
                    else if (!scriptMode) System.err.println("Ошибка валидации: Годовой оборот должен быть > 0.");
                } catch (NumberFormatException e) {
                    if (!scriptMode) System.err.println("Неверный формат числа.");
                }
            }

            OrganizationType type = askEnumNullable("    Тип", OrganizationType.class, OrganizationType.names());
            Address officialAddress = askAddress(); // Запрашиваем адрес (также можно пропустить внутри askAddress)

            return new Organization(annualTurnover, type, officialAddress);
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Ошибка ввода при создании Organization.", e);
            System.err.println("\nОшибка чтения ввода. Прерывание создания Organization.");
            return null;
        }
    }

    public static Address askAddress() throws AskBreak{
        try {
            System.out.println("    Введите Официальный адрес (опционально, нажмите Enter в запросе улицы для пропуска):");
            String street;
            while (true) {
                System.out.print("      Улица (или нажмите Enter для пропуска Адреса): ");
                street = readLine();
                if (street.isEmpty()) return null; // Пропускаем адрес
                // Базовая валидация, если нужна (например, не null, но пользователь это вводит)
                break;
            }

            Location town = askTown(); // Город обязателен, если указан адрес (согласно common models)

            if (town == null) { // Если askTown не удался или был отменен
                return null; // Не может быть адреса без города
            }

            return new Address(street, town);
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Ошибка ввода при создании Address.", e);
            System.err.println("\nОшибка чтения ввода. Прерывание создания Address.");
            return null;
        }
    }

    private static Location askTown() throws AskBreak {
        try {
            System.out.println("      Введите Местоположение города:");
            // common.models.Location: x, y, name не могут быть null
            Double x = askDoubleNullable("        Город X (число): ", val -> true, ""); // Нет специфичной валидации?
            // Переспрашиваем, если null введен в консоли
            while (x == null && !scriptMode) {
                System.err.println("Город X не может быть null.");
                x = askDoubleNullable("        Город X (число): ", val -> true, "");
            }
            if(x == null && scriptMode) throw new NoSuchElementException("Город X не может быть null в скрипте");


            Integer y = askInteger("        Город Y (целое число): ", val -> true, "");
            // Переспрашиваем, если null введен в консоли
            while (y == null && !scriptMode) {
                System.err.println("Город Y не может быть null.");
                y = askInteger("        Город Y (целое число): ", val -> true, "");
            }
            if(y == null && scriptMode) throw new NoSuchElementException("Город Y не может быть null в скрипте");


            String name = askString("        Название города: ");
            // Переспрашиваем, если null введен в консоли (хотя askString уже это делает)
            while (name.isEmpty() && !scriptMode) {
                System.err.println("Название города не может быть пустым.");
                name = askString("        Название города: ");
            }
            if(name.isEmpty() && scriptMode) throw new NoSuchElementException("Название города не может быть пустым в скрипте");


            return new Location(x, y, name);
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Ошибка ввода при создании Location (Town).", e);
            System.err.println("\nОшибка чтения ввода. Прерывание создания Location.");
            return null;
        }
    }

} // Конец класса Ask