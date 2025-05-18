package server.managers;

import common.models.*; // Импорт всех моделей из common
import common.models.Location;
import org.apache.logging.log4j.Logger; // Передаем логгер

import javax.xml.stream.*;
import java.io.*;
import java.time.ZonedDateTime; // Используем ZonedDateTime
import java.time.format.DateTimeParseException; // Для отлова ошибок парсинга ZonedDateTime
import java.util.Collection;
import java.util.HashSet;

public class DumpManager {
    private final String fileName;
    private final Logger logger;

    public DumpManager(String fileName, Logger logger) {
        this.fileName = fileName;
        this.logger = logger;
        logger.debug("DumpManager инициализирован для файла: {}", fileName);
    }

    public void writeCollection(Collection<Worker> collection) {
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Имя файла для сохранения не установлено. Сохранение отменено.");
            return;
        }

        // Проверяем, что коллекция не null. Если null, логируем и НЕ записываем пустой файл.
        // Если требуется записать пустой файл для null коллекции, раскомментируйте строку ниже.
        if (collection == null) {
            logger.warn("Попытка сохранения null коллекции. Сохранение отменено, чтобы не перезаписать файл.");
            // collection = new HashSet<>(); // Раскомментируйте, если нужно записывать пустой <Workers/> для null коллекции
            return; // Важно: выходим, если не хотим перезаписывать файл пустой коллекцией
        }

        // Если коллекция пуста, но не null, мы запишем <Workers></Workers>
        // Это нормальное поведение, если коллекция действительно была очищена.
        if (collection.isEmpty()) {
            logger.info("Коллекция пуста. Будет записан пустой XML-файл (<Workers></Workers>) в {}", fileName);
        } else {
            logger.info("Начало записи {} элементов коллекции в XML файл: {}", collection.size(), fileName);
        }

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null; // Объявляем здесь, чтобы был доступен в finally

        // Используем try-with-resources для FileOutputStream и BufferedOutputStream
        // XMLStreamWriter нужно закрывать отдельно в finally, так как он не AutoCloseable в некоторых реализациях
        // или его закрытие должно быть явным для корректной записи футера документа.
        try (FileOutputStream fos = new FileOutputStream(fileName); // Открывает файл для ЗАПИСИ (перезаписывает)
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            writer = outputFactory.createXMLStreamWriter(bos, "UTF-8");

            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");
            writer.writeStartElement("Workers");
            writer.writeCharacters("\n");

            for (Worker worker : collection) {
                if (worker == null) {
                    logger.warn("Пропущен null элемент при записи коллекции.");
                    continue;
                }
                writeWorkerToXML(writer, worker); // Вспомогательный метод
                writer.writeCharacters("\n");
            }

            writer.writeEndElement(); // </Workers>
            writer.writeCharacters("\n");
            writer.writeEndDocument();

            writer.flush(); // Принудительно сбрасываем буфер XMLStreamWriter

            logger.info("Коллекция успешно сохранена в файл {}", fileName);

        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода при сохранении файла {}: {}", fileName, e.getMessage(), e);
        } catch (XMLStreamException e) {
            logger.error("Ошибка записи XML в файл {}: {}", fileName, e.getMessage(), e);
        } catch (Exception e) { // Ловим другие непредвиденные ошибки
            logger.error("Непредвиденная ошибка при сохранении коллекции в {}: {}", fileName, e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close(); // Очень важно закрыть XMLStreamWriter!
                    // Это завершает XML-документ и освобождает ресурсы.
                } catch (XMLStreamException e) {
                    logger.error("Ошибка при закрытии XMLStreamWriter для файла {}: {}", fileName, e.getMessage(), e);
                }
            }
            // fos и bos закроются автоматически благодаря try-with-resources
        }
    }

    // Вспомогательный метод writeWorkerToXML (без изменений, как в вашем предыдущем коде)
    private void writeWorkerToXML(XMLStreamWriter writer, Worker worker) throws XMLStreamException {
        writer.writeCharacters("  "); // Отступ
        writer.writeStartElement("Worker");
        writer.writeCharacters("\n    ");

        writeElement(writer, "id", String.valueOf(worker.getId()));
        writeElement(writer, "name", worker.getName());

        // Coordinates
        if (worker.getCoordinates() != null) {
            writer.writeCharacters("    ");
            writer.writeStartElement("coordinates"); writer.writeCharacters("\n      ");
            writeElement(writer, "x", String.valueOf(worker.getCoordinates().getX())); writer.writeCharacters("      ");
            writeElement(writer, "y", String.valueOf(worker.getCoordinates().getY())); writer.writeCharacters("    ");
            writer.writeEndElement(); writer.writeCharacters("\n");
        } else {
            logger.warn("Worker с ID {} имеет null coordinates.", worker.getId());
        }

        // Creation Date (ZonedDateTime)
        if (worker.getCreationDate() != null) {
            writeElement(writer, "creationDate", worker.getCreationDate().toString());
        } else {
            logger.warn("Worker с ID {} имеет null creationDate.", worker.getId());
        }

        // Salary (Double)
        if (worker.getSalary() != null) {
            writeElement(writer, "salary", String.valueOf(worker.getSalary()));
        }

        // Start Date (ZonedDateTime)
        if (worker.getStartDate() != null) {
            writeElement(writer, "startDate", worker.getStartDate().toString());
        } else {
            logger.warn("Worker с ID {} имеет null startDate.", worker.getId());
        }

        // End Date (ZonedDateTime)
        if (worker.getEndDate() != null) { // getEndDate() не статический
            writeElement(writer, "endDate", worker.getEndDate().toString());
        }

        // Position (Enum)
        if (worker.getPosition() != null) {
            writeElement(writer, "position", worker.getPosition().name());
        }

        // Organization
        if (worker.getOrganization() != null) {
            Organization org = worker.getOrganization();
            writer.writeCharacters("    ");
            writer.writeStartElement("organization"); writer.writeCharacters("\n");

            if (org.getAnnualTurnover() != null) {
                writer.writeCharacters("      ");
                writeElement(writer, "annualTurnover", String.valueOf(org.getAnnualTurnover()));
            }
            if (org.getType() != null) {
                writer.writeCharacters("      ");
                writeElement(writer, "type", org.getType().name());
            }
            if (org.getOfficialAddress() != null) {
                Address address = org.getOfficialAddress();
                writer.writeCharacters("      ");
                writer.writeStartElement("officialAddress"); writer.writeCharacters("\n");

                if (address.getStreet() != null) {
                    writer.writeCharacters("        ");
                    writeElement(writer, "street", address.getStreet());
                } else {
                    logger.warn("Worker ID {} -> Organization -> Address имеет null street.", worker.getId());
                }

                if (address.getTown() != null) {
                    Location town = address.getTown();
                    writer.writeCharacters("        ");
                    writer.writeStartElement("town"); writer.writeCharacters("\n          ");
                    writeElement(writer, "x", String.valueOf(town.getX())); writer.writeCharacters("          ");
                    writeElement(writer, "y", String.valueOf(town.getY())); writer.writeCharacters("          ");
                    // ИСПРАВЛЕНИЕ: Имя поля в Location у вас nameT, метод getName()
                    writeElement(writer, "nameT", town.getName()); writer.writeCharacters("        ");
                    writer.writeEndElement(); writer.writeCharacters("\n"); // </town>
                }
                writer.writeCharacters("      ");
                writer.writeEndElement(); writer.writeCharacters("\n"); // </officialAddress>
            }
            writer.writeCharacters("    ");
            writer.writeEndElement(); writer.writeCharacters("\n"); // </organization>
        }
        writer.writeCharacters("  ");
        writer.writeEndElement(); // </Worker>
    }


    private void writeElement(XMLStreamWriter writer, String tagName, String value) throws XMLStreamException {
        if (value != null) {
            writer.writeStartElement(tagName);
            writer.writeCharacters(value);
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
    }

    public HashSet<Worker> readCollection() {
        if (fileName == null || fileName.isEmpty()) {
            logger.error("Имя файла для загрузки не установлено.");
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            logger.warn("Файл {} не найден. Возвращена пустая коллекция.", fileName);
            return new HashSet<>(); // Возвращаем пустую, а не null
        }
        if (!file.canRead()) {
            logger.error("Нет прав на чтение файла: {}", fileName);
            return null; // Ошибка прав доступа - критично
        }
        if (file.length() == 0) {
            logger.warn("Файл {} пуст. Возвращена пустая коллекция.", fileName);
            return new HashSet<>();
        }


        logger.info("Начало чтения коллекции из XML файла: {}", fileName);
        HashSet<Worker> collection = new HashSet<>();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        XMLStreamReader reader = null;
        try (InputStream fileStream = new FileInputStream(fileName);
             BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {

            reader = factory.createXMLStreamReader(bufferedStream);

            Worker currentWorker = null;
            Coordinates currentCoordinates = null;
            Organization currentOrganization = null;
            Address currentAddress = null;
            Location currentTown = null;
            String currentElement = null;
            StringBuilder textContent = new StringBuilder();

            int loadedCount = 0;
            int skippedCount = 0;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        textContent.setLength(0); // Очищаем здесь
                        currentElement = reader.getLocalName();
                        switch (currentElement) {
                            case "Worker":
                                currentWorker = new Worker();
                                currentCoordinates = new Coordinates();
                                currentOrganization = new Organization();
                                currentAddress = new Address(null, null);
                                currentTown = new Location(null, null, null); // Имя поля nameT
                                currentWorker.setCoordinates(currentCoordinates);
                                currentWorker.setOrganization(currentOrganization);
                                currentOrganization.setOfficialAddress(currentAddress);
                                break;
                            case "town":
                                if (currentAddress != null) {
                                    currentAddress.setTown(currentTown);
                                } else {
                                    logger.warn("Обнаружен тег 'town' без родительского 'officialAddress'");
                                }
                                break;
                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        if (!reader.isWhiteSpace()) {
                            textContent.append(reader.getText());
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        String endName = reader.getLocalName();
                        String value = textContent.toString().trim();

                        if ("Worker".equals(endName)) {
                            if (currentWorker != null && currentWorker.validate() && currentWorker.getId() != null && currentWorker.getId() > 0) { // Добавил проверку ID > 0
                                collection.add(currentWorker);
                                loadedCount++;
                            } else {
                                logger.warn("Пропущен невалидный Worker (или null/невалидный ID): ID={}",
                                        (currentWorker != null ? currentWorker.getId() : "N/A"));
                                skippedCount++;
                            }
                            currentWorker = null;
                        } else if (!value.isEmpty() && currentWorker != null) {
                            try {
                                parseWorkerField(currentWorker, currentCoordinates, currentOrganization,
                                        currentAddress, currentTown, endName, value);
                            } catch (IllegalArgumentException | DateTimeParseException | NullPointerException e) {
                                logger.warn("Ошибка парсинга поля '{}' со значением '{}' для Worker ID {}: {}",
                                        endName, value, currentWorker.getId(), e.getMessage());
                                currentWorker.setId(-1L); // Инвалидируем
                            }
                        }
                        break;
                }
            }
            logger.info("Чтение XML завершено. Загружено: {}, Пропущено: {}", loadedCount, skippedCount);
            return collection;

        } catch (FileNotFoundException e) {
            logger.error("Файл не найден (ошибка потока): {}", fileName, e);
            return null;
        } catch (XMLStreamException e) {
            logger.error("Ошибка парсинга XML файла {}: {}", fileName, e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода при чтении файла {}: {}", fileName, e.getMessage(), e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    logger.error("Ошибка при закрытии XMLStreamReader: {}", e.getMessage(), e);
                }
            }
        }
    }

    private void parseWorkerField(Worker w, Coordinates coords, Organization org, Address addr, Location town,
                                  String fieldName, String value) {
        if (w == null) return;

        try {
            switch (fieldName) {
                case "id": w.setId(Long.parseLong(value)); break;
                case "name": w.setName(value); break;
                case "creationDate": w.setCreationDate(ZonedDateTime.parse(value)); break;
                case "salary": w.setSalary(value); break; // Worker.setSalary(String)
                case "startDate": w.setStartDate(ZonedDateTime.parse(value)); break;
                case "endDate": w.setEndDate(ZonedDateTime.parse(value)); break;
                case "position": w.setPosition(Position.valueOf(value.toUpperCase())); break;

                case "x":
                    if (coords != null && w.getCoordinates() == coords) {
                        try {
                            double doubleValue = Double.parseDouble(value);
                            coords.setX((long) doubleValue); // Преобразование double в long
                        } catch (NumberFormatException e) {
                            logger.warn("Не удалось преобразовать значение '{}' для Coordinates.x в Long.", value, e);
                            throw new IllegalArgumentException("Неверный числовой формат для Coordinates.x", e);
                        }
                    } else if (town != null && addr != null && addr.getTown() == town) {
                        town.setX(Double.parseDouble(value)); // Здесь ожидается Double
                    }
                    break;
                case "y": // Может быть и для Coordinates, и для Location
                    if (coords != null && w.getCoordinates() == coords) coords.setY(Integer.parseInt(value));
                    else if (town != null && addr != null && addr.getTown() == town) town.setY(Integer.parseInt(value));
                    break;

                case "annualTurnover": if (org != null) org.setAnnualTurnover(Integer.parseInt(value)); break;
                case "type": if (org != null) org.setType(OrganizationType.valueOf(value.toUpperCase())); break;

                case "street": if (addr != null) addr.setStreet(value); break;

                case "nameT": // Поле nameT в Location
                    if (town != null && addr != null && addr.getTown() == town) {
                        town.setName(value); // setName из Location
                    }
                    break;
            }
        } catch (NumberFormatException e) {
            logger.warn("Ошибка преобразования числа для поля '{}', значение '{}': {}", fieldName, value, e.getMessage());
            throw new IllegalArgumentException("Неверный числовой формат для поля " + fieldName, e);
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное значение enum для поля '{}', значение '{}': {}", fieldName, value, e.getMessage());
            throw e;
        } catch (DateTimeParseException e) {
            logger.warn("Ошибка парсинга даты/времени для поля '{}', значение '{}': {}", fieldName, value, e.getMessage());
            throw e;
        }
    }

}