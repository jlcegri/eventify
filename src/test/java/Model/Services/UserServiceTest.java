package Model.Services;

import Model.Entities.Event;
import Model.Entities.User;
import Persistence.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private EventService eventService;

    @BeforeAll
    static void initTestDB() {
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Reset Singleton instances
        resetSingleton(UserService.class);
        resetSingleton(EventService.class);

        // 2. Clean and Populate DB
        cleanDatabase();
        populateData();

        // 3. Get fresh instances
        userService = UserService.getInstance();
        eventService = EventService.getInstance();
    }
    
    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
            stmt.executeUpdate("ALTER TABLE EVENTS ALTER COLUMN ID RESTART WITH 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void populateData() {
         try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
             
             // Insert User 1: Joao Felix
             stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (1, 'Joao', 'Felix', 'joao@app.com', 'pass')");
             // Insert User 2: Rui Fonseca
             stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (2, 'Rui', 'Fonseca', 'rui@app.com', 'pass')");
             
             // Reset sequence to avoid collision
             stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 3");

             // Insert Events
             String date = LocalDate.now().toString();
             String time = LocalTime.now().toString();
             // Event 1
             stmt.executeUpdate("INSERT INTO EVENTS (id, creatorID, title, description, date, startTime, endTime, location, city, eventType, maxAttendees, price) " +
                     "VALUES (1, 1, 'Event 1', 'Desc 1', '" + date + "', '" + time + "', '" + time + "', 'Loc', 'City', 'Type', 100, 10.0)");
             // Event 2
             stmt.executeUpdate("INSERT INTO EVENTS (id, creatorID, title, description, date, startTime, endTime, location, city, eventType, maxAttendees, price) " +
                     "VALUES (2, 1, 'Acoustic Night', 'Desc 2', '" + date + "', '" + time + "', '" + time + "', 'Loc', 'City', 'Music', 100, 10.0)");
             
             // Reset sequence
             stmt.executeUpdate("ALTER TABLE EVENTS ALTER COLUMN ID RESTART WITH 3");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetSingleton(Class<?> singletonClass) throws Exception {
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void testSingletonInstanceCreated() {
        UserService instance1 = UserService.getInstance();
        UserService instance2 = UserService.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2); // verifies same object (singleton)
    }

    @Test
    void testGetAllUsers_initialMockUsers() {
        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size()); // 2 populated users
    }

    @Test
    void testAddUser_assignsIdAndStoresUser() {
        User newUser = new User(
                0,
                "Alice Smith", 
                "alice@app.com",
                "mypassword",
                "Paris",
                List.of("Art", "Fashion"),
                List.of("Art Exhibitions", "Fashion Shows"),
                "A designer living in Paris."
        );
        User savedUser = userService.addUser(newUser);

        assertTrue(savedUser.getId() > 0); 
        assertTrue(userService.getAllUsers().stream().anyMatch(u -> u.getId() == savedUser.getId()));
    }

    @Test
    void testGetUserById_found() {
        User user = userService.getUserById(1);
        assertNotNull(user);
        assertEquals("Joao Felix", user.getName());
    }

    @Test
    void testGetUserById_notFound() {
        User user = userService.getUserById(999);
        assertNull(user);
    }

    @Test
    void testUpdateUser_success() {
        User updated = new User(
                1,
                "Updated Name",
                "updated@app.com",
                "newpass",
                "Madrid",
                List.of("Food & Drink"),
                List.of("Food Festivals"),
                "A chef from Madrid."
        );
        User result = userService.updateUser(updated);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("newpass", result.getPassword());
        assertEquals("Madrid", result.getLocation());
    }

    @Test
    void testUpdateUser_notFound() {
        User nonExistent = new User(
                999, "Ghost User",
                "ghost@app.com",
                "nopass",
                "Nowhere",
                List.of(),
                List.of(),
                ""
        );
        User result = userService.updateUser(nonExistent);

        assertNull(result);
    }

    @Test
    void testDeleteUser_success() {
        boolean deleted = userService.deleteUser(1); // mock user exists
        assertTrue(deleted);
        assertNull(userService.getUserById(1));
    }

    @Test
    void testDeleteUser_notFound() {
        boolean deleted = userService.deleteUser(999);
        assertFalse(deleted);
    }

    @Test
    void testGetAllUsers_returnsCopy() {
        List<User> users1 = userService.getAllUsers();
        List<User> users2 = userService.getAllUsers();
        
        assertNotSame(users1, users2);
        assertEquals(users1.size(), users2.size());
    }

    @Test
    void testGetCurrentUser_initial() {
        User currentUser = userService.getCurrentUser();
        assertNull(currentUser, "Initially no user should be logged in");
    }

    @Test
    void testAddUserCurrent_and_GetCurrentUser() {
        User newUser = new User(0, "Current User", "current@app.com", "pass", "City", List.of(), List.of(), "");

        // Add user and set as current
        userService.addUserCurrent(newUser);

        User currentUser = userService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals(newUser.getId(), currentUser.getId());
        assertEquals("Current User", currentUser.getName());
    }

    @Test
    void testJoinEvent_success() {
        Event event = eventService.getEventById(1);
        User user = userService.getUserById(1);

        assertTrue(userService.joinEvent(event, user));
        
        // Refresh event
        event = eventService.getEventById(1);
        assertTrue(event.getUsers().stream().anyMatch(u -> u.getId() == 1));
    }

    @Test
    void testJoinEvent_alreadyJoined() {
        Event event = eventService.getEventById(1);
        User user = userService.getUserById(1);

        userService.joinEvent(event, user); // First join

        assertFalse(userService.joinEvent(event, user)); // Second join should fail
    }

    @Test
    void testJoinEvent_nullParameters() {
        Event event = new Event();
        User user = userService.getUserById(1);

        assertFalse(userService.joinEvent(null, user));
        assertFalse(userService.joinEvent(event, null));
        assertFalse(userService.joinEvent(null, null));
    }

    @Test
    void testJoinEvent_nullAttendeesList() {
        Event event = new Event();
        event.setUsers(null); // Manually set attendees list to null
        User user = userService.getUserById(1);

        // Should return false or throw? Implementation checks null, but here we pass a non-persisted event
        // Service calls getEventById via addUserToEvent, so passing a non-persisted event might fail earlier.
        // If we want to test resilience, we should likely mock or use a persisted event.
        // For now, let's assume valid event ID check inside addUserToEvent returns false if not found.
        assertFalse(userService.joinEvent(event, user));
    }

    @Test
    void testRemoveEventFromUser_Success() {
        User user = userService.getUserById(1);
        Event event = eventService.getEventById(1); 

        // Join first
        userService.joinEvent(event, user);

        // Action: Remove
        boolean result = userService.removeEventFromUser(event, user);

        // Assert
        assertTrue(result, "Should return true when user is successfully removed");
        event = eventService.getEventById(1);
        assertFalse(event.getUsers().stream().anyMatch(u -> u.getId() == 1), "User should no longer be in the event list");
    }

    @Test
    void testRemoveEventFromUser_Failure_Nulls() {
        User user = userService.getUserById(1);
        Event event = eventService.getEventById(1);

        assertFalse(userService.removeEventFromUser(null, user));
        assertFalse(userService.removeEventFromUser(event, null));
    }

    @Test
    void testRemoveEventFromUser_Failure_NotAttending() {
        User user = userService.getUserById(2); // Rui Fonseca
        Event event = eventService.getEventById(2); // Acoustic Night

        // Ensure user is not in event (should be clean from populateData)
        
        boolean result = userService.removeEventFromUser(event, user);

        assertFalse(result, "Should return false if user was not attending the event");
    }
}