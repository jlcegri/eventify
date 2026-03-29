package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import Persistence.DatabaseManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for the "Join Event" functionality.
 */
class JoinEventTest {

    private UserService userService;
    private EventService eventService;
    private User testUser;
    private Event testEvent;

    @BeforeAll
    static void initTestDB() {
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();
        
        userService = UserService.getInstance();
        eventService = EventService.getInstance();
        
        // Insert user to be the creator and the joiner (or separate them if needed, here we use one user)
        // Creator
        createUser(9001, "Creator", "creator@test.com");
        // User to join
        createUser(999, "Test User", "test@test.com");
        
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 9002");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // We create a test user object mirroring the one in DB
        testUser = new User(
                999,
                "Test User",
                "test@test.com",
                "pass",
                "City",
                List.of("Tech", "Art"),
                List.of("Workshops"),
                "Bio");

        // We create a test event
        testEvent = new Event(
                101,
                9001, // Valid creator ID
                "Test Event Title",
                "A test description for joining.",
                LocalDate.now().plusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                "Main Street 123",
                "Test City",
                "Networking",
                List.of("Tech", "Business"),
                100,
                "default/url.png",
                15.50);
        
        // This adds to DB, so ID might change to auto-increment unless we force it or H2 respects it.
        // H2 usually respects provided ID if specified in INSERT, but `eventDAO.save` (called by addEvent)
        // constructs an INSERT statement. If it doesn't include ID, it autogenerates.
        // Let's assume addEvent generates ID.
        testEvent = eventService.addEvent(testEvent); 
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
    
    private void createUser(int id, String name, String email) {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (" + id + ", '" + name + "', 'Last', '" + email + "', 'pass')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddUser() {
        boolean result = userService.joinEvent(testEvent, testUser);

        // Refresh event from service to get updated attendee list
        testEvent = eventService.getEventById(testEvent.getId());

        assertTrue(result, "The joinEvent method should return true if the user was added.");
        assertTrue(testEvent.getUsers().stream().anyMatch(u -> u.getId() == testUser.getId()), "The user must be present on the event attendee list.");
        assertEquals(1, testEvent.getUsers().size(), "The size of the attendee list must be 1.");
    }

    @Test
    void testInvalidDuplicate() {
        userService.joinEvent(testEvent, testUser);

        boolean result = userService.joinEvent(testEvent, testUser);
        
        // Refresh event
        testEvent = eventService.getEventById(testEvent.getId());

        assertFalse(result, "The second attempt at joinEvent should return false.");
        assertEquals(1, testEvent.getUsers().size(), "The size of the attendee list must remain at 1 (not duplicated).");
    }

    @Test
    void testNullInputs() {
        assertFalse(userService.joinEvent(null, testUser), "It should fail if the event is null.");
        assertFalse(userService.joinEvent(testEvent, null), "It should fail if the user is null..");
    }

    @Test
    void testUpdateAttendees() {
        User activeUser = testUser;

        assertEquals(0, testEvent.getUsers().size());

        boolean joined = userService.joinEvent(testEvent, activeUser);
        
        // Refresh event
        testEvent = eventService.getEventById(testEvent.getId());

        assertTrue(joined);
        assertEquals(1, testEvent.getUsers().size(), "After joining, the count should be 1.");
    }
}