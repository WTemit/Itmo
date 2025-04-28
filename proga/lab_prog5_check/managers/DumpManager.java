package managers;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.*;

import models.Location;
import utility.Console;
import models.*;

/**
 * Использует файл для сохранения и загрузки коллекции в формате XML.
 * Реализация с использованием BufferedReader для чтения и FileWriter для записи.
 */
public class DumpManager {
    private final String fileName;
    private final Console console;
    private HashSet<Worker> collection;
    private Set<Long> usedIds = new HashSet<>();
    private Set<String> usedOrgNames = new HashSet<>();

    public DumpManager(String fileName, Console console) {
        this.fileName = fileName;
        this.console = console;
        this.collection = new HashSet<>();
    }

    /**
     * Записывает коллекцию в XML файл с использованием FileWriter.
     * @param collection коллекция
     */
    public void writeCollection(Collection<Worker> collection) {
        if (collection == null || collection.isEmpty()) {
            console.printError("Коллекция пуста!");
            return;
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<Workers>\n");

            for (Worker worker : collection) {
                writeWorkerToXML(writer, worker);
            }

            writer.write("</Workers>");
            writer.flush();

            console.println("Коллекция успешно сохранена в файл в формате XML!");
        } catch (IOException e) {
            console.printError("Ошибка ввода/вывода при сохранении файла: " + e.getMessage());
        } catch (Exception e) {
            console.printError("Непредвиденная ошибка при сохранении: " + e.getMessage());
        }
    }

    /**
     * Записывает рабочего в XML формат с использованием FileWriter.
     * @param writer FileWriter
     * @param worker рабочий для записи
     */
    private void writeWorkerToXML(FileWriter writer, Worker worker) throws IOException {
        writer.write("  <Worker>\n");

        writer.write("    <id>" + worker.getId() + "</id>\n");
        writer.write("    <name>" + worker.getName() + "</name>\n");
        writer.write("    <coordinates>" + worker.getCoordinates() + "</coordinates>\n");
        writer.write("    <creationDate>" + worker.getCreationDate().toInstant().toString() + "</creationDate>\n");

        if (worker.getSalary() != null) {
            writer.write("    <salary>" + worker.getSalary() + "</salary>\n");
        }

        writer.write("    <startDate>" + worker.getStartData().toInstant().toString() + "</startDate>\n");

        if (worker.getEndData() != null) {
            writer.write("    <endDate>" + worker.getEndData() + "</endDate>\n");
        }

        if (worker.getPosition() != null) {
            writer.write("    <position>" + worker.getPosition() + "</position>\n");
        }

        if (worker.getOrganization() != null) {
            writer.write("    <organization>\n");

            if (worker.getOrganization().getAnnualTurnover() != null) {
                writer.write("      <annualTurnover>" + worker.getOrganization().getAnnualTurnover() + "</annualTurnover>\n");
            }

            if (worker.getOrganization().getType() != null) {
                writer.write("      <type>" + worker.getOrganization().getType() + "</type>\n");
            }

            if (worker.getOrganization().getOfficialAddress() != null) {
                Address address = worker.getOrganization().getOfficialAddress();
                writer.write("      <officialAddress>\n");

                writer.write("        <street>" + address.getStreet() + "</street>\n");

                if (address.getTown() != null) {
                    Location town = address.getTown();
                    writer.write("        <town>\n");
                    writer.write("          <x>" + town.getX() + "</x>\n");
                    writer.write("          <y>" + town.getY() + "</y>\n");
                    writer.write("          <name>" + town.getName() + "</name>\n");
                    writer.write("        </town>\n");
                }

                writer.write("      </officialAddress>\n");
            }

            writer.write("    </organization>\n");
        }

        writer.write("  </Worker>\n");
    }

    /**
     * Считывает коллекцию из XML файла с использованием BufferedReader.
     * @return коллекция работников
     */
    public HashSet<Worker> readCollection() {
        collection = new HashSet<>();
        usedIds.clear();
        usedOrgNames.clear();
        int totalWorkers = 0;
        int loadedWorkers = 0;
        int skippedWorkers = 0;

        if (fileName != null && !fileName.isEmpty()) {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    console.printError("Загрузочный файл не найден!");
                    return collection;
                }

                // Проверяем, можно ли прочитать файл
                if (!file.canRead()) {
                    console.printError("Отсутствуют права на чтение файла!");
                    return collection;
                }

                // Используем BufferedReader для чтения файла
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder xmlContent = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        xmlContent.append(line).append("\n");
                    }

                    // Проверка на пустой файл
                    if (xmlContent.length() == 0) {
                        console.printError("Файл пуст!");
                        return collection;
                    }

                    // Проверка на базовую структуру XML
                    if (!xmlContent.toString().trim().startsWith("<?xml") && !xmlContent.toString().contains("<Workers>")) {
                        console.printError("Файл не содержит корректную XML структуру!");
                        return collection;
                    }

                    // Используем XMLStreamReader для парсинга XML
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    // Отключаем обработку внешних сущностей для безопасности
                    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
                    factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

                    XMLStreamReader xmlReader = factory.createXMLStreamReader(new StringReader(xmlContent.toString()));

                    Worker currentWorker = null;
                    String currentElement = "";
                    String parentElement = "";
                    Organization currentOrganization = null;
                    Address currentAddress = null;
                    Location currentLocation = null;
                    Coordinates currentCoordinates = null;
                    boolean workerValid = true;

                    while (xmlReader.hasNext()) {
                        int event = xmlReader.next();

                        switch (event) {
                            case XMLStreamConstants.START_ELEMENT:
                                String localName = xmlReader.getLocalName();

                                if ("Worker".equals(localName)) {
                                    currentWorker = new Worker();
                                    workerValid = true;
                                    totalWorkers++;
                                } else if ("coordinates".equals(localName)) {
                                    currentCoordinates = new Coordinates();
                                    if (currentWorker != null) {
                                        currentWorker.setCoordinates(currentCoordinates);
                                    }
                                } else if ("organization".equals(localName)) {
                                    currentOrganization = new Organization();
                                    if (currentWorker != null) {
                                        currentWorker.setOrganization(currentOrganization);
                                    }
                                } else if ("officialAddress".equals(localName)) {
                                    currentAddress = new Address(null, null);
                                    if (currentOrganization != null) {
                                        currentOrganization.setOfficialAddress(currentAddress);
                                    }
                                } else if ("town".equals(localName)) {
                                    currentLocation = new Location(null, 0, null);
                                    if (currentAddress != null) {
                                        currentAddress.setTown(currentLocation);
                                    }
                                }

                                parentElement = currentElement;
                                currentElement = localName;
                                break;

                            case XMLStreamConstants.CHARACTERS:
                                if (!xmlReader.isWhiteSpace()) {
                                    String text = xmlReader.getText().trim();
                                    try {
                                        if (!parseWorkerField(currentWorker, currentElement, text, parentElement,
                                                currentOrganization, currentAddress, currentLocation,
                                                currentCoordinates)) {
                                            workerValid = false;
                                        }
                                    } catch (Exception e) {
                                        console.printError("Ошибка при обработке поля " + currentElement +
                                                " для работника: " + e.getMessage());
                                        workerValid = false;
                                    }
                                }
                                break;

                            case XMLStreamConstants.END_ELEMENT:
                                String endName = xmlReader.getLocalName();

                                if ("Worker".equals(endName)) {
                                    if (workerValid && validateWorker(currentWorker)) {
                                        if (!usedIds.contains(currentWorker.getId())) {
                                            collection.add(currentWorker);
                                            usedIds.add(currentWorker.getId());
                                            loadedWorkers++;
                                        } else {
                                            console.printError("Пропущен работник с дублирующимся ID " + currentWorker.getId());
                                            skippedWorkers++;
                                        }
                                    } else {
                                        console.printError("Пропущен работник с недопустимыми данными (ID: " +
                                                (currentWorker != null ? currentWorker.getId() : "NULL") + ")");
                                        skippedWorkers++;
                                    }
                                    currentWorker = null;
                                    workerValid = true;
                                } else if ("organization".equals(endName)) {
                                    currentOrganization = null;
                                } else if ("officialAddress".equals(endName)) {
                                    currentAddress = null;
                                } else if ("town".equals(endName)) {
                                    currentLocation = null;
                                } else if ("coordinates".equals(endName)) {
                                    currentCoordinates = null;
                                }

                                if (currentElement.equals(endName)) {
                                    currentElement = parentElement;
                                }
                                break;
                        }
                    }

                    xmlReader.close();
                    console.println("Загрузка завершена. Загружено " + loadedWorkers + " из " + totalWorkers +
                            " работников. Пропущено из-за ошибок: " + skippedWorkers);
                }

            } catch (IOException e) {
                console.printError("Ошибка ввода/вывода при чтении файла: " + e.getMessage());
            } catch (XMLStreamException e) {
                console.printError("Ошибка чтения XML: " + e.getMessage());
            } catch (Exception e) {
                console.printError("Непредвиденная ошибка при загрузке: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            console.printError("Аргумент командной строки с загрузочным файлом не найден!");
        }

        return collection;
    }

    /**
     * Парсит поля рабочего из XML.
     * @param worker рабочий для заполнения
     * @param fieldName имя поля
     * @param value значение поля
     * @param parentElement родительский элемент
     * @return true если поле успешно распарсено, false в случае ошибки
     */
    private boolean parseWorkerField(Worker worker, String fieldName, String value, String parentElement,
                                     Organization organization, Address address, Location location,
                                     Coordinates coordinates) {
        try {
            if ("town".equals(parentElement) && location != null) {
                // Обработка данных Location для town
                switch (fieldName) {
                    case "x":
                        try {
                            Double x = Double.parseDouble(value);
                            if (x == null) {
                                console.printError("X в Location не может быть null");
                                return false;
                            }
                            location.setX(x);
                        } catch (NumberFormatException e) {
                            console.printError("Некорректное значение для X в Location: " + value);
                            return false;
                        }
                        break;
                    case "y":
                        try {
                            double y = Double.parseDouble(value);
                            location.setY((int) y);
                        } catch (NumberFormatException e) {
                            console.printError("Некорректное значение для Y в Location: " + value);
                            return false;
                        }
                        break;
                }
            } else if ("organization".equals(parentElement) && organization != null) {
                // Обработка данных Organization
                if (fieldName.equals("annualTurnover")) {
                    try {
                        double annualTurnover = Double.parseDouble(value);
                        if (annualTurnover <= 0) {
                            console.printError("annualTurnover должен быть больше 0");
                            return false;
                        }
                        organization.setAnnualTurnover((int) annualTurnover);
                    } catch (NumberFormatException e) {
                        console.printError("Некорректное значение для annualTurnover: " + value);
                        return false;
                    }
                }
            } else if (worker != null) {
                // Обработка основных полей рабочего
                switch (fieldName) {
                    case "id":
                        try {
                            int id = Integer.parseInt(value);
                            if (id <= 0) {
                                console.printError("ID должен быть больше 0");
                                return false;
                            }
                            worker.setId(id);
                        } catch (NumberFormatException e) {
                            console.printError("Некорректное значение для ID: " + value);
                            return false;
                        }
                        break;
                    case "name":
                        if (value == null || value.isEmpty()) {
                            console.printError("Имя не может быть пустым");
                            return false;
                        }
                        worker.setName(value);
                        break;
                    case "coordinates":
                        try {
                            String[] parts = value.split(";");
                            Float x = Float.parseFloat(parts[0]);
                            if (x <= -274) {
                                console.printError("X в Coordinates должен быть больше -274");
                                return false;
                            }
                            coordinates = new Coordinates(value);
                            worker.setCoordinates(coordinates);
                        }catch (NumberFormatException e){
                            console.printError("Некорректное значение для X в Coordinates: " + value);
                            return false;
                        }
                        break;
                    case "creationDate":
                        try {
                            worker.setCreationDate(ZonedDateTime.parse(value));
                        } catch (DateTimeParseException e) {
                            console.printError("Некорректный формат даты создания: " + value);
                            return false;
                        }
                        break;
                    case "salary":
                        try {
                            double salary = Double.parseDouble(value);
                            if (salary <= 0) {
                                console.printError("Зарплата должна быть больше 0");
                                return false;
                            }
                            worker.setSalary(String.valueOf(salary));
                        } catch (NumberFormatException e) {
                            console.printError("Некорректное значение для зарплаты: " + value);
                            return false;
                        }
                        break;
                    case "startDate":
                        try {
                            worker.setStartDate(ZonedDateTime.parse(value));
                        } catch (Exception e) {
                            console.printError("Некорректный формат даты начала работы: " + value);
                            return false;
                        }
                        break;
                    case "endDate":
                        try {
                            worker.setEndDate(LocalDateTime.parse(value));
                        } catch (DateTimeParseException e) {
                            console.printError("Некорректный формат даты окончания работы: " + value);
                            return false;
                        }
                        break;
                    case "position":
                        try {
                            Position position = Position.valueOf(value.toUpperCase());
                            worker.setPosition(position);
                        } catch (IllegalArgumentException e) {
                            console.printError("Неверная позиция: " + value +
                                    ". Допустимые значения: " +
                                    String.join(", ", positionNames()));
                            return false;
                        }
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            console.printError("Общая ошибка парсинга поля " + fieldName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Возвращает имена всех позиций из перечисления
     */
    private String[] positionNames() {
        Position[] positions = Position.values();
        String[] names = new String[positions.length];
        for (int i = 0; i < positions.length; i++) {
            names[i] = positions[i].name();
        }
        return names;
    }

    /**
     * Полная проверка работника на соответствие всем ограничениям
     * @param worker работник для проверки
     * @return true если работник валиден, false в противном случае
     */
    private boolean validateWorker(Worker worker) {
        if (worker == null) {
            console.printError("Работник не может быть null");
            return false;
        }

        if (worker.getId() <= 0) {
            console.printError("ID работника должен быть больше 0");
            return false;
        }

        if (worker.getName() == null || worker.getName().isEmpty()) {
            console.printError("Имя работника не может быть пустым");
            return false;
        }

        if (worker.getCoordinates() == null) {
            console.printError("Координаты работника не могут быть null");
            return false;
        }
        try{
        if (worker.getCoordinates().getX() <= -274) {
            console.printError("X координата должна быть больше -274");
            return false;
        }} catch (NullPointerException e){
            console.printError("X координата должна быть не Null");
            return false;
        }

        if (worker.getCreationDate() == null) {
            console.printError("Дата создания не может быть null");
            return false;
        }

        if (worker.getSalary() <= 0) {
            console.printError("Зарплата должна быть больше 0");
            return false;
        }

        // Проверка Organization, если она есть
        if (worker.getOrganization() != null) {
            Organization org = worker.getOrganization();

            if (org.getAnnualTurnover() <= 0) {
                console.printError("Годовой оборот должен быть больше 0");
                return false;
            }

            // Проверка Address, если он есть
            if (org.getOfficialAddress() != null) {
                Address address = org.getOfficialAddress();

                if (address.getTown() == null) {
                    console.printError("Поле town в адресе не может быть null");
                    return false;
                }

                // Проверка Location
                Location town = address.getTown();
                if (town.getX() == null) {
                    console.printError("X в Location не может быть null");
                    return false;
                }

                if (town.getY() == null) {
                    console.printError("Y в Location не может быть null");
                    return false;
                }
            }
        }

        return true;
    }
}