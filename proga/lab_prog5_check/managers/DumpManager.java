package managers;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.stream.*;

import models.Location;
import utility.Console;
import models.*;

import static models.OrganizationType.TRUST;

/**
 * Использует файл для сохранения и загрузки коллекции в формате XML.
 * Реализация с использованием BufferedReader для чтения и FileWriter для записи.
 */
public class DumpManager {
    private final String fileName;
    private final Console console;
    private HashSet<Worker> collection;

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

        if (fileName != null && !fileName.isEmpty()) {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    console.printError("Загрузочный файл не найден!");
                    return collection;
                }

                // Используем BufferedReader для чтения файла
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder xmlContent = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        xmlContent.append(line).append("\n");
                    }

                    // Используем XMLStreamReader для парсинга XML
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    XMLStreamReader xmlReader = factory.createXMLStreamReader(new StringReader(xmlContent.toString()));

                    Worker currentWorker = null;
                    String currentElement = "";
                    String parentElement = "";
                    Organization currentOrganization = null;
                    Address currentAddress = null;
                    Location currentLocation = null;

                    while (xmlReader.hasNext()) {
                        int event = xmlReader.next();

                        switch (event) {
                            case XMLStreamConstants.START_ELEMENT:
                                String localName = xmlReader.getLocalName();

                                if ("Worker".equals(localName)) {
                                    currentWorker = new Worker();
                                } else if ("organization".equals(localName)) {
                                    currentOrganization = new Organization();
                                    if (currentWorker != null) {
                                        currentWorker.setOrganization(currentOrganization);
                                    }
                                } else if ("officialAddress".equals(localName)) {
                                    currentAddress = new Address("", null);
                                    if (currentOrganization != null) {
                                        currentOrganization.setOfficialAddress(currentAddress);
                                    }
                                } else if ("town".equals(localName)) {
                                    currentLocation = new Location(0.0, 0, "");
                                    if (currentAddress != null) {
                                        currentAddress.setTown(currentLocation);
                                    }
                                }

                                parentElement = currentElement;
                                currentElement = localName;
                                break;

                            case XMLStreamConstants.CHARACTERS:
                                if (!xmlReader.isWhiteSpace()) {
                                    String text = xmlReader.getText();
                                    parseWorkerField(currentWorker, currentElement, text, parentElement, currentOrganization, currentAddress, currentLocation);
                                }
                                break;

                            case XMLStreamConstants.END_ELEMENT:
                                String endName = xmlReader.getLocalName();

                                if ("Worker".equals(endName)) {
                                    if (currentWorker != null && currentWorker.validate()) {
                                        collection.add(currentWorker);
                                    } else if (currentWorker != null) {
                                        console.printError("Файл с коллекцией содержит недействительные данные для рабочего: " + currentWorker);
                                    }
                                    currentWorker = null;
                                } else if ("organization".equals(endName)) {
                                    currentOrganization = null;
                                } else if ("officialAddress".equals(endName)) {
                                    currentAddress = null;
                                } else if ("town".equals(endName)) {
                                    currentLocation = null;
                                }

                                if (currentElement.equals(endName)) {
                                    currentElement = parentElement;
                                }
                                break;
                        }
                    }

                    xmlReader.close();
                    console.println("Коллекция успешно загружена из XML файла!");
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
     */
    private void parseWorkerField(Worker worker, String fieldName, String value, String parentElement,
                                  Organization organization, Address address, Location location) {
        try {
            if ("town".equals(parentElement) && location != null) {
                // Обработка данных Location для town
                switch (fieldName) {
                    case "x" -> location.setX(Double.parseDouble(value));
                    case "y" -> location.setY(Integer.parseInt(value));
                    case "name" -> location.setName(value);
                }
            } else if ("officialAddress".equals(parentElement) && address != null) {
                // Обработка данных Address
                if ("street".equals(fieldName)) {
                    address.setStreet(value);
                }
            } else if ("organization".equals(parentElement) && organization != null) {
                // Обработка данных Organization
                switch (fieldName) {
                    case "annualTurnover" -> organization.setAnnualTurnover(Integer.parseInt(value));
                    case "type" -> {
                        try {
                            organization.setType(OrganizationType.valueOf(value));
                        } catch (IllegalArgumentException e) {
                            console.printError("Неверный тип организации: " + value);
                        }
                    }
                }
            } else if (worker != null) {
                // Обработка основных полей рабочего
                switch (fieldName) {
                    case "id" -> worker.setId(Long.parseLong(value));
                    case "name" -> worker.setName(value);
                    case "coordinates" -> worker.setCoordinates(new Coordinates(value));
                    case "creationDate" -> worker.setCreationDate(ZonedDateTime.parse(value));
                    case "startDate" -> worker.setStartDate(ZonedDateTime.parse(value));
                    case "endDate" -> worker.setEndDate(LocalDateTime.parse(value));
                    case "salary" -> worker.setSalary(value);
                    case "position" -> {
                        try {
                            worker.setPosition(Position.valueOf(value));
                        } catch (IllegalArgumentException e) {
                            console.printError("Неверная позиция: " + value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            console.printError("Ошибка парсинга поля " + fieldName + ": " + e.getMessage());
        }
    }
}