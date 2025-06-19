package server.db;

import common.dto.User;
import common.models.*;
import org.apache.logging.log4j.Logger;
import server.util.PasswordManager;

import java.sql.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Управляет всеми операциями с базой данных.
 * Не использует постоянное соединение.
 * Вместо этого она создает новое соединение для каждого вызова метода и немедленно закрывает его,
 * обеспечивая потокобезопасность и предотвращая утечки ресурсов с помощью try-with-resources.
 */
public class DatabaseManager {
    private final Logger logger;

    // Данные для подключения хранятся как final строки.
    private final String DB_URL = "jdbc:postgresql://localhost:5432/studs";
    private final String DB_USER = "s466385";
    private final String DB_PASS = "";

    public DatabaseManager(Logger logger) {
        this.logger = logger;
    }

    public void initializeTables() {
        logger.info("Предполагается, что таблицы базы данных инициализированы.");
    }

    /**
     * Проверяет, можно ли установить соединение с базой данных.
     * Этот метод вызывается один раз при запуске сервера.
     */
    public void connect() {
        try (Connection conn = getConnection()) {
            logger.info("Драйвер базы данных проверен, соединение доступно.");
        } catch (SQLException e) {
            logger.fatal("Не удалось установить начальное соединение с базой данных. Проверьте URL, имя пользователя, пароль и драйвер.", e);
            System.exit(1);
        }
    }

    /**
     * Приватный вспомогательный метод для создания нового соединения с базой данных.
     * @return Новый объект Connection.
     * @throws SQLException если возникает ошибка доступа к базе данных.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }


    /**
     * Аутентифицирует пользователя, сравнивая хешированную версию предоставленного пароля
     * с той, что хранится в базе данных.
     * @param username Имя пользователя.
     * @param password Пароль пользователя в виде обычного текста.
     * @return true, если аутентификация прошла успешно, иначе false.
     */
    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    String providedHash = PasswordManager.hashPassword(password, salt);
                    return storedHash.equals(providedHash);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL при аутентификации пользователя '{}': {}", username, e.getMessage());
        }
        return false;
    }

    /**
     * Регистрирует нового пользователя в базе данных.
     * @param username Желаемое имя пользователя.
     * @param password Желаемый пароль.
     * @return true, если регистрация прошла успешно, false, если имя пользователя занято или произошла ошибка БД.
     */
    public boolean registerUser(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        String salt = PasswordManager.generateSalt();
        String hashedPassword = PasswordManager.hashPassword(password, salt);
        String sql = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // 23505 - это SQLSTATE для unique_violation (нарушение уникальности)
                logger.warn("Попытка зарегистрировать существующее имя пользователя: {}", username);
            } else {
                logger.error("Ошибка SQL при регистрации пользователя '{}': {}", username, e.getMessage());
            }
            return false;
        }
    }

    /**
     * Загружает всех работников из базы данных в Set.
     * @return Set всех объектов Worker.
     */
    public Set<Worker> loadWorkers() {
        Set<Worker> workers = new HashSet<>();
        String sql = "SELECT * FROM workers";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                workers.add(mapRowToWorker(rs));
            }
        } catch (SQLException e) {
            logger.error("Не удалось загрузить работников из базы данных: {}", e.getMessage(), e);
        }
        return workers;
    }

    /**
     * Добавляет нового работника в базу данных.
     * @param worker Объект Worker для добавления.
     * @param user Пользователь, который владеет этим работником.
     * @return Сгенерированный ID нового работника, или -1 в случае неудачи.
     */
    public long addWorker(Worker worker, User user) {
        String sql = "INSERT INTO workers (name, coordinates_x, coordinates_y, creation_date, salary, start_date, end_date, position, org_annual_turnover, org_type, org_address_street, org_address_town_x, org_address_town_y, org_address_town_name, owner_username) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setWorkerStatement(stmt, worker, user);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL при добавлении работника для пользователя '{}': {}", user.getUsername(), e.getMessage());
        }
        return -1;
    }

    /**
     * Обновляет существующего работника в базе данных.
     * @param workerId ID работника для обновления.
     * @param worker Объект Worker с новыми данными.
     * @param user Пользователь, выполняющий обновление (для проверки владения).
     * @return true, если обновление прошло успешно.
     */
    public boolean updateWorker(long workerId, Worker worker, User user) {
        String sql = "UPDATE workers SET name=?, coordinates_x=?, coordinates_y=?, salary=?, start_date=?, end_date=?, position=?, org_annual_turnover=?, org_type=?, org_address_street=?, org_address_town_x=?, org_address_town_y=?, org_address_town_name=? " +
                "WHERE id = ? AND owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setWorkerStatementForUpdate(stmt, worker);
            stmt.setLong(14, workerId);
            stmt.setString(15, user.getUsername());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Ошибка SQL при обновлении работника {} для пользователя '{}': {}", workerId, user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Удаляет работника из базы данных.
     * @param workerId ID работника для удаления.
     * @param user Пользователь, выполняющий удаление (для проверки владения).
     * @return true, если удаление прошло успешно.
     */
    public boolean removeWorker(long workerId, User user) {
        String sql = "DELETE FROM workers WHERE id = ? AND owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, workerId);
            stmt.setString(2, user.getUsername());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Ошибка SQL при удалении работника {} для пользователя '{}': {}", workerId, user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Удаляет всех работников, принадлежащих определенному пользователю.
     * @param user Пользователь, чьих работников нужно удалить.
     * @return Количество удаленных строк, или -1 в случае неудачи.
     */
    public int clearWorkers(User user) {
        String sql = "DELETE FROM workers WHERE owner_username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка SQL при очистке работников для пользователя '{}': {}", user.getUsername(), e.getMessage());
            return -1;
        }
    }

    /**
     * Преобразует строку из ResultSet в объект Worker.
     * @param rs ResultSet для преобразования.
     * @return Новый объект Worker.
     * @throws SQLException если столбец не найден.
     */
    private Worker mapRowToWorker(ResultSet rs) throws SQLException {
        Worker worker = new Worker();
        worker.setId(rs.getLong("id"));
        worker.setName(rs.getString("name"));
        worker.setCoordinates(new Coordinates(rs.getLong("coordinates_x"), rs.getInt("coordinates_y")));
        worker.setCreationDate(ZonedDateTime.ofInstant(rs.getTimestamp("creation_date").toInstant(), ZoneOffset.UTC));
        worker.setSalary(String.valueOf(rs.getDouble("salary")));
        if(rs.wasNull()) worker.setSalary(null);
        worker.setStartDate(ZonedDateTime.ofInstant(rs.getTimestamp("start_date").toInstant(), ZoneOffset.UTC));
        Timestamp endDate = rs.getTimestamp("end_date");
        if (endDate != null) worker.setEndDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC));
        String positionStr = rs.getString("position");
        if(positionStr != null) worker.setPosition(Position.valueOf(positionStr));

        Organization org = new Organization();
        org.setAnnualTurnover(rs.getInt("org_annual_turnover"));
        if(rs.wasNull()) org.setAnnualTurnover(null);
        String orgTypeStr = rs.getString("org_type");
        if(orgTypeStr != null) org.setType(OrganizationType.valueOf(orgTypeStr));

        Address address = new Address(rs.getString("org_address_street"), null);
        if (address.getStreet() != null) {
            Location town = new Location(
                    rs.getDouble("org_address_town_x"),
                    rs.getInt("org_address_town_y"),
                    rs.getString("org_address_town_name")
            );
            if (rs.wasNull()) town.setX(null);
            address.setTown(town.getX() != null ? town : null);
        }

        org.setOfficialAddress(address.getStreet() != null ? address : null);
        worker.setOrganization(org.getAnnualTurnover() != null || org.getType() != null || org.getOfficialAddress() != null ? org : null);

        worker.setOwnerUsername(rs.getString("owner_username"));
        return worker;
    }

    /**
     * Вспомогательный метод для установки параметров для инструкции INSERT.
     */
    private void setWorkerStatement(PreparedStatement stmt, Worker worker, User user) throws SQLException {
        setWorkerStatementForUpdate(stmt, worker);
        stmt.setTimestamp(4, Timestamp.from(worker.getCreationDate().toInstant()));
        stmt.setString(15, user.getUsername());
    }

    /**
     * Вспомогательный метод для установки общих параметров для INSERT и UPDATE.
     */
    private void setWorkerStatementForUpdate(PreparedStatement stmt, Worker worker) throws SQLException {
        stmt.setString(1, worker.getName());
        stmt.setLong(2, worker.getCoordinates().getX());
        stmt.setInt(3, worker.getCoordinates().getY());

        if (worker.getSalary() != null) stmt.setDouble(5, worker.getSalary()); else stmt.setNull(5, Types.DOUBLE);

        stmt.setTimestamp(6, Timestamp.from(worker.getStartDate().toInstant()));

        if (worker.getEndDate() != null) stmt.setTimestamp(7, Timestamp.from(((ZonedDateTime) worker.getEndDate()).toInstant())); else stmt.setNull(7, Types.TIMESTAMP_WITH_TIMEZONE);

        if (worker.getPosition() != null) stmt.setString(8, worker.getPosition().name()); else stmt.setNull(8, Types.VARCHAR);

        Organization org = worker.getOrganization();
        if (org != null) {
            if (org.getAnnualTurnover() != null) stmt.setInt(9, org.getAnnualTurnover()); else stmt.setNull(9, Types.INTEGER);
            if (org.getType() != null) stmt.setString(10, org.getType().name()); else stmt.setNull(10, Types.VARCHAR);

            Address address = org.getOfficialAddress();
            if (address != null) {
                stmt.setString(11, address.getStreet());
                Location town = address.getTown();
                if (town != null) {
                    stmt.setDouble(12, town.getX());
                    stmt.setInt(13, town.getY());
                    stmt.setString(14, town.getName());
                } else {
                    stmt.setNull(12, Types.DOUBLE);
                    stmt.setNull(13, Types.INTEGER);
                    stmt.setNull(14, Types.VARCHAR);
                }
            } else {
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.DOUBLE);
                stmt.setNull(13, Types.INTEGER);
                stmt.setNull(14, Types.VARCHAR);
            }
        } else {
            stmt.setNull(9, Types.INTEGER);
            stmt.setNull(10, Types.VARCHAR);
            stmt.setNull(11, Types.VARCHAR);
            stmt.setNull(12, Types.DOUBLE);
            stmt.setNull(13, Types.INTEGER);
            stmt.setNull(14, Types.VARCHAR);
        }
    }
}