package server.managers;

import common.models.*;
import common.models.Location; // Явный импорт, если есть неоднозначность
import org.apache.logging.log4j.Logger;

import javax.xml.stream.*;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects; // Для Objects.hash, если будете использовать

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
        if (collection == null) {
            logger.warn("Попытка сохранения null коллекции. Сохранение отменено.");
            return;
        }

        if (collection.isEmpty()) {
            logger.info("Коллекция пуста. Будет записан пустой XML-файл (<Workers></Workers>) в {}", fileName);
        } else {
            logger.info("Начало записи {} элементов коллекции в XML файл: {}", collection.size(), fileName);
        }

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try (FileOutputStream fos = new FileOutputStream(fileName);
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
                writeWorkerToXML(writer, worker);
                writer.writeCharacters("\n");
            }

            writer.writeEndElement(); // </Workers>
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();
            logger.info("Коллекция успешно сохранена в файл {}", fileName);

        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода при сохранении файла {}: {}", fileName, e.getMessage(), e);
        } catch (XMLStreamException e) {
            logger.error("Ошибка записи XML в файл {}: {}", fileName, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при сохранении коллекции в {}: {}", fileName, e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (XMLStreamException e) {
                    logger.error("Ошибка при закрытии XMLStreamWriter для файла {}: {}", fileName, e.getMessage(), e);
                }
            }
        }
    }

    private void writeWorkerToXML(XMLStreamWriter writer, Worker worker) throws XMLStreamException {
        writer.writeCharacters("  ");
        writer.writeStartElement("Worker");
        writer.writeCharacters("\n"); // Перенос строки после <Worker>

        writeElement(writer, "id", String.valueOf(worker.getId()), "    ");
        writeElement(writer, "name", worker.getName(), "    ");

        if (worker.getCoordinates() != null) {
            writer.writeCharacters("    "); // Отступ для <coordinates>
            writer.writeStartElement("coordinates");
            writer.writeCharacters("\n");
            writeElement(writer, "x", String.valueOf(worker.getCoordinates().getX()), "      ");
            writeElement(writer, "y", String.valueOf(worker.getCoordinates().getY()), "      ");
            writer.writeCharacters("    "); // Отступ для </coordinates>
            writer.writeEndElement(); // </coordinates>
            writer.writeCharacters("\n");
        } else {
            logger.warn("Worker с ID {} имеет null coordinates, тег не будет записан.", worker.getId());
        }

        if (worker.getCreationDate() != null) writeElement(writer, "creationDate", worker.getCreationDate().toString(), "    ");
        if (worker.getSalary() != null) writeElement(writer, "salary", String.valueOf(worker.getSalary()), "    ");
        if (worker.getStartDate() != null) writeElement(writer, "startDate", worker.getStartDate().toString(), "    ");
        if (worker.getEndDate() != null) writeElement(writer, "endDate", worker.getEndDate().toString(), "    ");
        if (worker.getPosition() != null) writeElement(writer, "position", worker.getPosition().name(), "    ");

        if (worker.getOrganization() != null) {
            Organization org = worker.getOrganization();
            writer.writeCharacters("    "); // Отступ для <organization>
            writer.writeStartElement("organization");
            writer.writeCharacters("\n");

            if (org.getAnnualTurnover() != null) writeElement(writer, "annualTurnover", String.valueOf(org.getAnnualTurnover()), "      ");
            if (org.getType() != null) writeElement(writer, "type", org.getType().name(), "      ");

            if (org.getOfficialAddress() != null) {
                Address address = org.getOfficialAddress();
                writer.writeCharacters("      "); // Отступ для <officialAddress>
                writer.writeStartElement("officialAddress");
                writer.writeCharacters("\n");

                if (address.getStreet() != null) writeElement(writer, "street", address.getStreet(), "        ");

                if (address.getTown() != null) {
                    Location town = address.getTown();
                    writer.writeCharacters("        "); // Отступ для <town>
                    writer.writeStartElement("town");
                    writer.writeCharacters("\n");
                    if (town.getX() != null) writeElement(writer, "x", String.valueOf(town.getX()), "          ");
                    if (town.getY() != null) writeElement(writer, "y", String.valueOf(town.getY()), "          ");
                    if (town.getName() != null) writeElement(writer, "nameT", town.getName(), "          "); // Имя тега nameT
                    writer.writeCharacters("        "); // Отступ для </town>
                    writer.writeEndElement(); // </town>
                    writer.writeCharacters("\n");
                }
                writer.writeCharacters("      "); // Отступ для </officialAddress>
                writer.writeEndElement(); // </officialAddress>
                writer.writeCharacters("\n");
            }
            writer.writeCharacters("    "); // Отступ для </organization>
            writer.writeEndElement(); // </organization>
            writer.writeCharacters("\n");
        }
        writer.writeCharacters("  "); // Отступ для </Worker>
        writer.writeEndElement(); // </Worker>
    }

    private void writeElement(XMLStreamWriter writer, String tagName, String value, String indent) throws XMLStreamException {
        if (value != null) {
            writer.writeCharacters(indent);
            writer.writeStartElement(tagName);
            writer.writeCharacters(value);
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
    }

    public HashSet<Worker> readCollection() {
        if (fileName == null || fileName.isEmpty()) { logger.error("Имя файла для загрузки не установлено."); return null; }
        File file = new File(fileName);
        if (!file.exists()) { logger.warn("Файл {} не найден. Возвращена пустая коллекция.", fileName); return new HashSet<>(); }
        if (!file.canRead()) { logger.error("Нет прав на чтение файла: {}", fileName); return null; }
        if (file.length() == 0) { logger.warn("Файл {} пуст. Возвращена пустая коллекция.", fileName); return new HashSet<>(); }

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
            StringBuilder textContent = new StringBuilder();

            // Флаги для отслеживания контекста
            boolean inCoordinatesTag = false;
            boolean inTownTag = false;

            int loadedCount = 0;
            int skippedCount = 0;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        textContent.setLength(0);
                        String elementName = reader.getLocalName();
                        switch (elementName) {
                            case "Worker":
                                currentWorker = new Worker();
                                // Инициализируем вложенные объекты, которые всегда должны быть у Worker
                                currentCoordinates = new Coordinates(); // Поля x, y будут null по умолчанию
                                currentWorker.setCoordinates(currentCoordinates);
                                // Organization и его части могут быть null, создаем их только при наличии тегов
                                currentOrganization = null;
                                currentAddress = null;
                                currentTown = null;
                                break;
                            case "coordinates":
                                inCoordinatesTag = true;
                                // currentCoordinates уже должен быть создан при "Worker"
                                break;
                            case "organization":
                                if (currentWorker != null) {
                                    currentOrganization = new Organization();
                                    currentWorker.setOrganization(currentOrganization);
                                }
                                break;
                            case "officialAddress":
                                if (currentOrganization != null) {
                                    currentAddress = new Address(null, null);
                                    currentOrganization.setOfficialAddress(currentAddress);
                                }
                                break;
                            case "town":
                                if (currentAddress != null) {
                                    currentTown = new Location(null, null, null);
                                    currentAddress.setTown(currentTown);
                                    inTownTag = true;
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
                            if (currentWorker != null && currentWorker.validate() &&
                                    currentWorker.getId() != null && currentWorker.getId() > 0 &&
                                    currentWorker.getCoordinates() != null && // Проверка на null Coordinates
                                    currentWorker.getCoordinates().getX() != null && // Проверка на null X
                                    currentWorker.getCoordinates().getY() != null) { // Проверка на null Y
                                collection.add(currentWorker);
                                loadedCount++;
                                logger.debug("Добавлен Worker: ID={}", currentWorker.getId());
                            } else {
                                logger.warn("Пропущен невалидный Worker или Worker с неполными координатами: ID={}",
                                        (currentWorker != null ? currentWorker.getId() : "N/A"));
                                skippedCount++;
                            }
                            currentWorker = null; // Сбрасываем для следующего
                        } else if ("coordinates".equals(endName)) {
                            inCoordinatesTag = false;
                        } else if ("town".equals(endName)) {
                            inTownTag = false;
                        } else if (!value.isEmpty() && currentWorker != null) {
                            try {
                                parseWorkerField(currentWorker, currentCoordinates, currentOrganization,
                                        currentAddress, currentTown, endName, value, inCoordinatesTag, inTownTag);
                            } catch (IllegalArgumentException | DateTimeParseException e) { // NullPointerException теперь маловероятен здесь
                                logger.warn("Ошибка парсинга поля '{}' со значением '{}' для Worker (ID {}): {}",
                                        endName, value, (currentWorker.getId() == null ? "нового" : currentWorker.getId()), e.getMessage());
                            }
                        }
                        break;
                }
            }
            logger.info("Чтение XML завершено. Загружено: {}, Пропущено: {}", loadedCount, skippedCount);
            return collection;

        } catch (FileNotFoundException e) { logger.error("Файл не найден (ошибка потока): {}", fileName, e); return null;
        } catch (XMLStreamException e) { logger.error("Ошибка парсинга XML файла {}: {}", fileName, e.getMessage(), e); return null;
        } catch (IOException e) { logger.error("Ошибка ввода/вывода при чтении файла {}: {}", fileName, e.getMessage(), e); return null;
        } finally {
            if (reader != null) { try { reader.close(); } catch (XMLStreamException e) { logger.error("Ошибка при закрытии XMLStreamReader: {}", e.getMessage(), e); } }
        }
    }

    private void parseWorkerField(Worker w, Coordinates workerCoords, Organization org, Address addr, Location townLoc,
                                  String fieldName, String value, boolean isInCoords, boolean isInTown) {
        try {
            switch (fieldName) {
                // Поля Worker
                case "id": w.setId(Long.parseLong(value)); break;
                case "name": w.setName(value); break;
                case "creationDate": w.setCreationDate(ZonedDateTime.parse(value)); break;
                case "salary": w.setSalary(value); break;
                case "startDate": w.setStartDate(ZonedDateTime.parse(value)); break;
                case "endDate": w.setEndDate(ZonedDateTime.parse(value)); break;
                case "position": w.setPosition(Position.valueOf(value.toUpperCase())); break;

                // Поля <x> и <y>
                case "x":
                    if (isInCoords && workerCoords != null) {
                        logger.trace("Парсинг X для Worker Coordinates: {}", value);
                        workerCoords.setX(Long.parseLong(value));
                    } else if (isInTown && townLoc != null) {
                        logger.trace("Парсинг X для Town Location: {}", value);
                        townLoc.setX(Double.parseDouble(value));
                    } else {
                        logger.warn("Тег <x> со значением '{}' найден вне ожидаемого контекста (coordinates/town).", value);
                    }
                    break;
                case "y":
                    if (isInCoords && workerCoords != null) {
                        logger.trace("Парсинг Y для Worker Coordinates: {}", value);
                        workerCoords.setY(Integer.parseInt(value));
                    } else if (isInTown && townLoc != null) {
                        logger.trace("Парсинг Y для Town Location: {}", value);
                        townLoc.setY(Integer.parseInt(value));
                    } else {
                        logger.warn("Тег <y> со значением '{}' найден вне ожидаемого контекста (coordinates/town).", value);
                    }
                    break;

                // Поля Organization
                case "annualTurnover": if (org != null) org.setAnnualTurnover(Integer.parseInt(value)); break;
                case "type": if (org != null) org.setType(OrganizationType.valueOf(value.toUpperCase())); break;

                // Поля Address
                case "street": if (addr != null) addr.setStreet(value); break;

                // Поля Location (города) - имя
                case "nameT": // Тег из вашего XML
                    if (isInTown && townLoc != null) {
                        logger.trace("Парсинг nameT для Town Location: {}", value);
                        townLoc.setName(value); // common.models.Location.setName(String)
                    } else {
                        logger.warn("Тег <nameT> со значением '{}' найден вне ожидаемого контекста <town>.", value);
                    }
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException e) {
            logger.warn("Ошибка преобразования числа для поля '{}', значение '{}': {}", fieldName, value, e.getMessage());
            throw new IllegalArgumentException("Неверный числовой формат для поля " + fieldName, e);
        } catch (IllegalArgumentException e) {
            logger.warn("Неверное значение для поля '{}', значение '{}': {}", fieldName, value, e.getMessage());
            throw e;
        }
    }
}