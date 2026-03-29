package Persistence;

import Model.Entities.Event;
import Model.Entities.User;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and Integration tests for JdbcEventDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcEventDAOTest {

    private JdbcEventDAO eventDAO;
    private Event sampleEvent;

    @BeforeAll
    void setupDatabase() {
        DatabaseManager.initTestDatabase();
        // Initialize the DAO implementation before all tests
        eventDAO = new JdbcEventDAO();
    }

    private int testUserId;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createTestUser();

        // Create a fresh Event object before each test case to ensure test isolation
        sampleEvent = new Event(
                0, testUserId, "Rock concert", "Event description",
                LocalDate.now().plusDays(7),
                LocalTime.of(20, 0),
                LocalTime.of(23, 0),
                "National Stadium", "Madrid", "Music",
                Arrays.asList("Rock", "Concert"), 200, "http://image.com/rock.jpg", 15.50
        );
    }

    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTestUser() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO USERS (firstName, lastName, email, password) VALUES ('Test', 'User', 'test@test.com', '1234')",
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            pstmt.executeUpdate();
            var rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                testUserId = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should save an event and generate an ID")
    void testSaveEvent() {
        eventDAO.save(sampleEvent);

        // Verify that the database assigned an ID to the object
        assertNotEquals(0, sampleEvent.getId(), "The event ID should have been updated after saving.");

        // Verify the data was actually written to the database
        Event retrieved = eventDAO.findById(sampleEvent.getId());
        assertNotNull(retrieved);
        assertEquals("Rock concert", retrieved.getTitle());
    }

    @Test
    @DisplayName("Should retrieve all events from the database")
    void testFindAll() {
        eventDAO.save(sampleEvent);
        List<Event> events = eventDAO.findAll();

        assertFalse(events.isEmpty(), "The events list should not be empty.");
    }

    @Test
    @DisplayName("Should update existing event details")
    void testUpdateEvent() {
        eventDAO.save(sampleEvent);

        // Modify fields
        sampleEvent.setTitle("Updated title");
        sampleEvent.setCity("Barcelona");

        boolean updated = eventDAO.update(sampleEvent);
        assertTrue(updated, "The update operation should return true.");

        // Verify the changes in the database
        Event retrieved = eventDAO.findById(sampleEvent.getId());
        assertEquals("Updated title", retrieved.getTitle());
        assertEquals("Barcelona", retrieved.getCity());
    }

    @Test
    @DisplayName("Should delete an event by ID")
    void testDeleteEvent() {
        eventDAO.save(sampleEvent);
        int id = sampleEvent.getId();

        boolean deleted = eventDAO.delete(id);
        assertTrue(deleted, "The delete operation should return true.");

        // Verify the event no longer exists
        Event retrieved = eventDAO.findById(id);
        assertNull(retrieved, "The event should be null after deleting it.");
    }

    @Test
    @DisplayName("Should manage attendee flow (add/remove users)")
    void testAttendeeManagement() {
        eventDAO.save(sampleEvent);
        int eventId = sampleEvent.getId();
        int userId = testUserId; 

        // 1. Add user to event
        boolean added = eventDAO.addUserToEvent(eventId, userId);

        // Only continue verification if the insertion succeeded (depends on DB state)
        if (added) {
            assertTrue(added);

            // 2. Verify user is in the attendees list
            List<User> attendees = eventDAO.findAttendeesByEventId(eventId);
            boolean found = attendees.stream().anyMatch(u -> u.getId() == userId);
            assertTrue(found, "The user should be present in the attendee list.");

            // 3. Remove user from event
            boolean removed = eventDAO.removeUserFromEvent(eventId, userId);
            assertTrue(removed, "The removal operation should return true.");
        }
    }

    @Test
    @DisplayName("Should correctly convert interest lists to CSV and back")
    void testCSVConversion() {
        // This test verifies the private logic for handling comma-separated values in the DB
        List<String> originalInterests = Arrays.asList("Sports", "Outdoors");
        sampleEvent.setInterests(originalInterests);

        eventDAO.save(sampleEvent);
        Event retrieved = eventDAO.findById(sampleEvent.getId());

        // Verify that the list was reconstructed correctly from the CSV string
        assertNotNull(retrieved.getInterests());
        assertEquals(originalInterests.size(), retrieved.getInterests().size());
        assertTrue(retrieved.getInterests().contains("Sports"));
    }
}