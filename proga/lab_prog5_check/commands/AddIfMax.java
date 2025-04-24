package commands;

import managers.CollectionManager;
import models.*;
import utility.Console;
import utility.ExecutionResponse;
import models.Ask;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Команда 'add_if_max'. Добавить новый элемент, если он превышает максимальный по всем значимым полям.
 */
public class AddIfMax extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public AddIfMax(Console console, CollectionManager collectionManager) {
        super("add_if_max", "добавить новый элемент, если он превышает максимальный по ВСЕМ значимым полям");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String[] arguments) {
        if (arguments.length > 1 && !arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Команда не требует аргументов!\nИспользование: '" + getName() + "'");
        }

        try {
            console.println("* Создание нового работника для сравнения:");
            Worker newWorker = Ask.askWorker(console, collectionManager.getFreeId());

            if (newWorker == null || !newWorker.validate()) {
                return new ExecutionResponse(false, "Некорректные данные работника!");
            }

            // Проверяем, что новый работник максимальный по всем полям
            boolean isMax = true;
            StringBuilder comparisonResults = new StringBuilder();

            // Сравнение по имени (лексикографический порядок)
            Worker maxByName = getMaxWorkerByField("name");
            if (maxByName != null && newWorker.getName().compareTo(maxByName.getName()) <= 0) {
                isMax = false;
                comparisonResults.append("× Имя не максимальное (макс: ").append(maxByName.getName()).append(")\n");
            }

            // Сравнение по координатам
            Worker maxByCoordinates = getMaxWorkerByField("coordinates");
            if (maxByCoordinates != null && !isCoordinatesGreater(newWorker.getCoordinates(), maxByCoordinates.getCoordinates())) {
                isMax = false;
                comparisonResults.append("× Координаты не максимальные\n");
            }

            // Сравнение по зарплате
            Worker maxBySalary = getMaxWorkerByField("salary");
            if (maxBySalary != null && newWorker.getSalary() != null &&
                    (maxBySalary.getSalary() == null || newWorker.getSalary() <= maxBySalary.getSalary())) {
                isMax = false;
                comparisonResults.append("× Зарплата не максимальная (макс: ")
                        .append(maxBySalary.getSalary()).append(")\n");
            }

            // Сравнение по дате начала
            Worker maxByStartDate = getMaxWorkerByField("startDate");
            if (maxByStartDate != null && !newWorker.getStartData().after(maxByStartDate.getStartData())) {
                isMax = false;
                comparisonResults.append("× Дата начала не максимальная\n");
            }

            // Сравнение по дате окончания
            Worker maxByEndDate = getMaxWorkerByField("endDate");
            if (maxByEndDate != null && newWorker.getEndData() != null &&
                    (maxByEndDate.getEndData() == null || !newWorker.getEndData().isAfter(maxByEndDate.getEndData()))) {
                isMax = false;
                comparisonResults.append("× Дата окончания не максимальная\n");
            }

            // Сравнение по позиции
            Worker maxByPosition = getMaxWorkerByField("position");
            if (maxByPosition != null && newWorker.getPosition() != null &&
                    (maxByPosition.getPosition() == null ||
                            newWorker.getPosition().compareTo(maxByPosition.getPosition()) <= 0)) {
                isMax = false;
                comparisonResults.append("× Должность не максимальная\n");
            }

            // Сравнение по организации
            Worker maxByOrganization = getMaxWorkerByField("organization");
            if (maxByOrganization != null && !isOrganizationGreater(newWorker.getOrganization(), maxByOrganization.getOrganization())) {
                isMax = false;
                comparisonResults.append("× Организация не максимальная\n");
            }

            if (isMax) {
                collectionManager.add(newWorker);
                return new ExecutionResponse("Работник добавлен - он превышает максимальные значения по ВСЕМ полям!");
            } else {
                return new ExecutionResponse(false,
                        "Работник НЕ добавлен - не все значения максимальные:\n" + comparisonResults.toString());
            }
        } catch (Ask.AskBreak e) {
            return new ExecutionResponse(false, "Создание работника прервано");
        }
    }

    private Worker getMaxWorkerByField(String fieldName) {
        if (collectionManager.getCollection().isEmpty()) {
            return null;
        }

        return collectionManager.getCollection().stream()
                .filter(Objects::nonNull)
                .reduce((w1, w2) -> {
                    switch (fieldName) {
                        case "name":
                            return w1.getName().compareTo(w2.getName()) > 0 ? w1 : w2;
                        case "coordinates":
                            return isCoordinatesGreater(w1.getCoordinates(), w2.getCoordinates()) ? w1 : w2;
                        case "salary":
                            return compareSalaries(w1.getSalary(), w2.getSalary()) > 0 ? w1 : w2;
                        case "startDate":
                            return w1.getStartData().after(w2.getStartData()) ? w1 : w2;
                        case "endDate":
                            return compareEndDates(w1.getEndData(), w2.getEndData()) > 0 ? w1 : w2;
                        case "position":
                            return comparePositions(w1.getPosition(), w2.getPosition()) > 0 ? w1 : w2;
                        case "organization":
                            return isOrganizationGreater(w1.getOrganization(), w2.getOrganization()) ? w1 : w2;
                        default:
                            return w1;
                    }
                })
                .orElse(null);
    }

    // Вспомогательные методы сравнения
    private boolean isCoordinatesGreater(Coordinates c1, Coordinates c2) {
        if (c1 == null) return false;
        if (c2 == null) return true;
        return c1.getX() > c2.getX() ||
                (c1.getX() == c2.getX() && c1.getY() > c2.getY());
    }

    private int compareSalaries(Double s1, Double s2) {
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return Double.compare(s1, s2);
    }

    private int compareEndDates(LocalDateTime d1, LocalDateTime d2) {
        if (d1 == null) return -1;
        if (d2 == null) return 1;
        return d1.compareTo(d2);
    }

    private int comparePositions(Position p1, Position p2) {
        if (p1 == null) return -1;
        if (p2 == null) return 1;
        return p1.compareTo(p2);
    }

    private boolean isOrganizationGreater(Organization o1, Organization o2) {
        if (o1 == null) return false;
        if (o2 == null) return true;
        return o1.getAnnualTurnover() != null &&
                (o2.getAnnualTurnover() == null ||
                        o1.getAnnualTurnover() > o2.getAnnualTurnover());
    }
}