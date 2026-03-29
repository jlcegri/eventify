package Persistence;

import Model.Entities.Event;
import Model.Services.EventDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaoCoverageTest {

    private EventDAO eventDAO;
    private int testUserId;

    @BeforeEach
    void setUp() {
        DatabaseManager.initTestDatabase();
        eventDAO = new JdbcEventDAO();
        cleanDatabase();
        // Create a user for foreign key constraint
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO USERS (firstName, lastName, email, password, location, bio, interests_csv, eventTypes_csv, firstEntry) VALUES ('Test', 'User', 'test@test.com', 'pass', 'Loc', 'Bio', '', '', false)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                testUserId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanDatabase() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.getInstance().close();
    }

    @Test
    void testFindByCreator() {
        Event event = new Event(
                testUserId, // creatorID (matches the user created in setUp)
                "CreatorEvent",
                "Desc",
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(1),
                "Loc",
                "City",
                "Type",
                new ArrayList<>(),
                100,
                "url",
                10.0
        );
        eventDAO.save(event);

        List<Event> results = eventDAO.findByCreator(testUserId);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("CreatorEvent", results.get(0).getTitle());
        
        List<Event> emptyResults = eventDAO.findByCreator(999);
        assertTrue(emptyResults.isEmpty());
    }
}
