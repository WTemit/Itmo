package models;

import utility.Element;
import utility.Validatable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class Worker extends Element implements Validatable {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double salary; //Поле может быть null, Значение поля должно быть больше 0
    private Date startDate; //Поле не может быть null
    private LocalDateTime endDate; //Поле может быть null
    private Position position; //Поле может быть null
    private Organization organization; //Поле может быть null

    public Worker(Long id, String name, Coordinates coordinates, Date creationDate, Double salary, Date startDate, LocalDateTime endDate, Position position, Organization organization){
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.salary = salary;
        this.startDate = startDate;
        this.endDate = endDate;
        this.position = position;
        this.organization = organization;
    }

    public Worker(Long id, String name, Coordinates coordinates, Double salary, Position position, Organization organization){
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = new Date();
        this.salary = salary;
        this.startDate = new Date();
        this.endDate = LocalDateTime.now();
        this.position = position;
        this.organization = organization;
    }

    public Worker() {
        this.creationDate = new Date();
        this.startDate = new Date();
    }

    @Override
    public boolean validate() {
        if (id == null || id <= 0) return false;
        if (name == null || name.isEmpty()) return false;
        if (coordinates == null) return false;
        if (creationDate == null) return false;
        if (salary != null && salary <= 0) return false;
        if (startDate == null) return false;
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime zonedDateTime) {
        this.creationDate = Date.from(zonedDateTime.toInstant());
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                this.salary = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                this.salary = null;
            }
        } else {
            this.salary = null;
        }
    }

    public Date getStartData() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = Date.from(startDate.toInstant());
    }

    public LocalDateTime getEndData() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(int annualTurnover, OrganizationType type, Address officialAdress) {
        if (this.organization == null) {
            this.organization = new Organization(annualTurnover, type, officialAdress);
        } else {
            this.organization.setAnnualTurnover(annualTurnover);
        }
    }

    @Override
    public int compareTo(Element element) {
        return (int)(this.id - element.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker that = (Worker) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, salary, startDate, endDate, position, organization);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Worker #").append(id).append("\n");
        sb.append("  Name: ").append(name).append("\n");
        sb.append("  Coordinates: ").append(coordinates).append("\n");
        sb.append("  Creation Date: ").append(creationDate).append("\n");
        if (salary != null) sb.append("  Salary: ").append(salary).append("\n");
        sb.append("  Start Date: ").append(startDate).append("\n");
        if (endDate != null) sb.append("  End Date: ").append(endDate).append("\n");
        if (position != null) sb.append("  Position: ").append(position).append("\n");
        if (organization != null) sb.append("  Organization: ").append(organization);
        return sb.toString();
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;

    }
}