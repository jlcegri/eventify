package Persistence;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventDAO;
import Model.Services.UserDAO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcEventDAO implements EventDAO {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserDAO userDAO;

    public JdbcEventDAO() {
        // We use the Service layer to access the User DAO to avoid dependency cycles and ensure consistency
        this.userDAO = new JdbcUserDAO();
    }

    // --- CRUD EVENTS ---

    @Override
    public void save(Event event) {
        String interestsCSV = convertListToCSV(event.getInterests());

        String sql = "INSERT INTO EVENTS (" +
                "creatorID, title, description, date, startTime, endTime, location, city, " +
                "eventType, interests_csv, maxAttendees, imageURL, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            stmt.setInt(i++, event.getCreatorID());
            stmt.setString(i++, event.getTitle());
            stmt.setString(i++, event.getDescription());
            stmt.setDate(i++, Date.valueOf(event.getDate()));

            // Handle nullable LocalTime for startTime
            if (event.getStartTime() != null) {
                stmt.setString(i++, event.getStartTime().format(TIME_FORMATTER));
            } else {
                stmt.setNull(i++, java.sql.Types.TIME);
            }

            // Handle nullable LocalTime for endTime
            if (event.getEndTime() != null) {
                stmt.setString(i++, event.getEndTime().format(TIME_FORMATTER));
            } else {
                stmt.setNull(i++, java.sql.Types.TIME);
            }

            stmt.setString(i++, event.getLocation());
            stmt.setString(i++, event.getCity());
            stmt.setString(i++, event.getEventType());
            stmt.setString(i++, interestsCSV);
            // Handling nullable Integer
            if (event.getMaxAttendees() != null) {
                stmt.setInt(i++, event.getMaxAttendees());
            } else {
                stmt.setNull(i++, java.sql.Types.INTEGER);
            }
            stmt.setString(i++, event.getImageURL());
            stmt.setDouble(i++, event.getPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating event failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating event failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving event: " + event.getTitle(), e);
        }
    }

    @Override
    public Event findById(int id) {
        String sql = "SELECT * FROM EVENTS WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Event event = mapResultSetToEvent(rs);
                    loadAttendees(event);
                    return event;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding event by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event event = mapResultSetToEvent(rs);
                loadAttendees(event);
                events.add(event);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all events.", e);
        }
        return events;
    }

    @Override
    public List<Event> findByCreator(int userId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM EVENTS WHERE creatorID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Event event = mapResultSetToEvent(rs);
                    loadAttendees(event);
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving events by creator ID: " + userId, e);
        }
        return events;
    }

    @Override
    public boolean update(Event event) {
        String interestsCSV = convertListToCSV(event.getInterests());

        String sql = "UPDATE EVENTS SET creatorID = ?, title = ?, description = ?, date = ?, startTime = ?, endTime = ?, " +
                "location = ?, city = ?, eventType = ?, interests_csv = ?, maxAttendees = ?, imageURL = ?, price = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int i = 1;
            stmt.setInt(i++, event.getCreatorID());
            stmt.setString(i++, event.getTitle());
            stmt.setString(i++, event.getDescription());
            stmt.setDate(i++, Date.valueOf(event.getDate()));
            stmt.setString(i++, event.getStartTime().format(TIME_FORMATTER));
            stmt.setString(i++, event.getEndTime().format(TIME_FORMATTER));
            stmt.setString(i++, event.getLocation());
            stmt.setString(i++, event.getCity());
            stmt.setString(i++, event.getEventType());
            stmt.setString(i++, interestsCSV);
            if (event.getMaxAttendees() != null) {
                stmt.setInt(i++, event.getMaxAttendees());
            } else {
                stmt.setNull(i++, java.sql.Types.INTEGER);
            }
            stmt.setString(i++, event.getImageURL());
            stmt.setDouble(i++, event.getPrice());
            stmt.setInt(i++, event.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating event: " + event.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM EVENTS WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting event with ID: " + id, e);
        }
    }

    // --- ATTENDEE MANAGEMENT ---

    /**
     * @brief Loads all attendees (User objects) for a given Event and sets them in the Event entity.
     * @param event The Event to load attendees for.
     */
    @Override
    public void loadAttendees(Event event) {
        event.setUsers(findAttendeesByEventId(event.getId()));
    }

    @Override
    public List<User> findAttendeesByEventId(int eventId) {
        List<User> attendees = new ArrayList<>();
        // Query to fetch User IDs from the join table
        String sql = "SELECT userID FROM EVENT_ATTENDEES WHERE eventID = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("userID");
                    User user = userDAO.findById(userId);
                    if (user != null) {
                        attendees.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding attendees for event ID: " + eventId, e);
        }
        return attendees;
    }

    @Override
    public boolean addUserToEvent(int eventId, int userId) {
        String sql = "INSERT INTO EVENT_ATTENDEES (eventID, userID) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);

            // ExecuteUpdate > 0 means the insertion was successful
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Check for duplicate entry (user already attending)
            if (e.getSQLState().startsWith("23")) { // SQLState for integrity constraint violation
                return false;
            }
            throw new RuntimeException("Error adding user " + userId + " to event " + eventId, e);
        }
    }

    @Override
    public boolean removeUserFromEvent(int eventId, int userId) {
        String sql = "DELETE FROM EVENT_ATTENDEES WHERE eventID = ? AND userID = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing user " + userId + " from event " + eventId, e);
        }
    }

    // --- HELPER METHODS ---

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int creatorID = rs.getInt("creatorID");
        String title = rs.getString("title");
        String description = rs.getString("description");

        // Mapeo de SQL Date a LocalDate
        LocalDate date = rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null;

        // Mapeo de SQL Time a LocalTime (asumiendo que se guardaron como String HH:mm:ss o Time)
        LocalTime startTime = rs.getString("startTime") != null ? LocalTime.parse(rs.getString("startTime")) : null;
        LocalTime endTime = rs.getString("endTime") != null ? LocalTime.parse(rs.getString("endTime")) : null;

        String location = rs.getString("location");
        String city = rs.getString("city");
        String eventType = rs.getString("eventType");

        List<String> interests = convertCSVToList(rs.getString("interests_csv"));

        // Handle nullable Integer
        Integer maxAttendees = rs.getObject("maxAttendees", Integer.class);

        String imageURL = rs.getString("imageURL");
        double price = rs.getDouble("price");

        return new Event(id, creatorID, title, description, date, startTime, endTime, location, city,
                eventType, interests, maxAttendees, imageURL, price);
    }

    private List<String> convertCSVToList(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String convertListToCSV(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(", ", list);
    }
}