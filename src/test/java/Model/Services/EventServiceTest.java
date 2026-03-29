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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    private EventService service;
    private UserService userService;

    @BeforeAll
    static void initTestDB() {
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        // Reset singletons manually using reflection
        // Ensure UserService is initialized before EventService to avoid events referencing stale User instances
        resetSingleton(UserService.class);
        resetSingleton(EventService.class);
        
        cleanDatabase();
        populateData();

        service = EventService.getInstance();
        userService = UserService.getInstance();
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
             
             // Insert User 1
             stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (1, 'Test', 'User', 'test@test.com', '1234')");
             
             // Insert 4 Events (IDs 1-4 automatically if auto-increment is reset, but specifying ID to be safe if H2 allows)
             // Using simple values. Assuming H2 handles date/time string conversion or using standard format.
             String date = LocalDate.now().toString();
             String time = LocalTime.now().toString();
             
             for (int i = 1; i <= 4; i++) {
                 stmt.executeUpdate("INSERT INTO EVENTS (id, creatorID, title, description, date, startTime, endTime, location, city, eventType, maxAttendees, price) " +
                         "VALUES (" + i + ", 1, 'Event " + i + "', 'Desc', '" + date + "', '" + time + "', '" + time + "', 'Loc', 'City', 'Type', 100, 10.0)");
             }

             // Add User 1 to Event 1
             stmt.executeUpdate("INSERT INTO EVENT_ATTENDEES (eventID, userID) VALUES (1, 1)");
             
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> T resetSingleton(Class<T> singletonClass) throws Exception {
        Field instanceField = singletonClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        
        // Re-initialize the singleton instance
        if (singletonClass.equals(EventService.class)) {
            return (T) EventService.getInstance();
        } else {
            return (T) UserService.getInstance();
        }
    }

    @Test
    void testSingletonInstance() {
        EventService service1 = EventService.getInstance();
        EventService service2 = EventService.getInstance();
        assertSame(service1, service2, "EventService should be a singleton");
    }

    @Test
    void testInitialMockEventsLoaded() {
        List<Event> events = service.getAllEvents();
        assertEquals(4, events.size(), "Service should start with 4 mock events");
    }
//TODO: modify tests
//    @Test
//    void testAddEventAssignsId() {
//        Event newEvent = new Event();
//        newEvent.setTitle("Test Event");
//
//        Event saved = service.addEvent(newEvent);
//
//        assertTrue(saved.getId() > 0, "Event ID should be a positive integer");
//        assertEquals(5, service.getAllEvents().size());
//    }

    @Test
    void testGetEventByIdFound() {
        Event event = service.getEventById(1);
        assertNotNull(event, "Event with ID 1 should exist");
        assertEquals(1, event.getId());
    }

    @Test
    void testGetEventByIdNotFound() {
        Event event = service.getEventById(999);
        assertNull(event, "No event should be found for invalid ID");
    }

    @Test
    void testGetAllEventsReturnsCopy() {
        List<Event> list1 = service.getAllEvents();
        List<Event> list2 = service.getAllEvents();

        assertNotSame(list1, list2, "List returned must be a copy, not the internal list");
    }

    @Test
    void testUpdateEventSuccess() {
        Event updated = new Event(
                1,  // ID of existing event
                1,
                "Updated Title",
                "Updated Description",
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now().plusHours(2),
                "New Location",
                "New City",
                "Updated Event Type",
                List.of("Technology", "Music"),
                123,
                "newImage.jpg",
                99.99
        );

        Event result = service.updateEvent(updated);
        assertNotNull(result);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("New Location", result.getLocation());
        assertEquals("newImage.jpg", result.getImageURL());
        assertEquals(99.99, result.getPrice());
    }
//TODO: modify tests
//    @Test
//    void testUpdateEventNotFound() {
//        Event fakeEvent = new Event();
//        fakeEvent.setId(999); // non-existing ID
//        Event result = service.updateEvent(fakeEvent);
//        assertNull(result, "Update should return empty if ID not found");
//    }

    @Test
    void testDeleteEventSuccess() {
        boolean deleted = service.deleteEvent(1);
        assertTrue(deleted, "Event with ID 1 should be deleted");
        assertEquals(3, service.getAllEvents().size());
    }

    @Test
    void testDeleteEventNotFound() {
        boolean deleted = service.deleteEvent(999);
        assertFalse(deleted, "Should return false if no event was deleted");
    }
//TODO: modify tests
//    @Test
//    void testGetEventsByCity() {
//        List<Event> madridEvents = service.getEventsByCity("Madrid");
//        assertEquals(1, madridEvents.size());
//        assertEquals("Madrid", madridEvents.get(0).getCity());
//
//        List<Event> coimbraEvents = service.getEventsByCity("coimbra"); // Case-insensitive
//        assertEquals(1, coimbraEvents.size());
//        assertEquals("Coimbra", coimbraEvents.get(0).getCity());
//
//        List<Event> nonExistentCityEvents = service.getEventsByCity("NonExistentCity");
//        assertTrue(nonExistentCityEvents.isEmpty());
//    }
//
//    @Test
//    void testGetProfileSuggestedEvents_byInterests() {
//        List<String> interests = List.of("Sports");
//        List<Event> suggested = service.getProfileSuggestedEvents(interests, Collections.emptyList());
//        assertEquals(1, suggested.size());
//        assertTrue(suggested.get(0).getInterests().contains("Sports"));
//    }
//
//    @Test
//    void testGetProfileSuggestedEvents_byEventType() {
//        List<String> eventTypes = List.of("Technology");
//        List<Event> suggested = service.getProfileSuggestedEvents(Collections.emptyList(), eventTypes);
//        assertEquals(1, suggested.size());
//        assertEquals("Technology", suggested.get(0).getEventType());
//    }
//
//    @Test
//    void testGetProfileSuggestedEvents_byBoth() {
//        List<String> interests = List.of("Music");
//        List<String> eventTypes = List.of("Technology");
//        List<Event> suggested = service.getProfileSuggestedEvents(interests, eventTypes);
//        assertEquals(3, suggested.size()); // Tech conference + 2 music events
//    }
//
//    @Test
//    void testGetProfileSuggestedEvents_noPreferences() {
//        List<Event> suggested = service.getProfileSuggestedEvents(Collections.emptyList(), Collections.emptyList());
//        assertTrue(suggested.isEmpty());
//    }
//
//    @Test
//    void testSmartSuggestedEvents_isDeprecated() {
//        List<String> interests = List.of("Culture");
//        List<String> eventTypes = List.of("Music");
//
//        // Should return the same as getProfileSuggestedEvents
//        List<Event> smartSuggested = service.getSmartSuggestedEvents("AnyCity", interests, eventTypes);
//        List<Event> profileSuggested = service.getProfileSuggestedEvents(interests, eventTypes);
//
//        assertEquals(profileSuggested.size(), smartSuggested.size());
//    }

    @Test
    void testRemoveUserFromEvent_success() {
        User user = userService.getUserById(1);
        Event event = service.getEventById(1);
        
        // Ensure user 1 is in event 1 (set up in populateData)
        assertTrue(event.getUsers().stream().anyMatch(u -> u.getId() == 1));
        
        boolean removed = service.removeUserFromEvent(1, user);
        assertTrue(removed);
        
        // Refresh event
        event = service.getEventById(1);
        assertFalse(event.getUsers().stream().anyMatch(u -> u.getId() == 1));
    }

    @Test
    void testRemoveUserFromEvent_eventNotFound() {
        User user = userService.getUserById(1);
        boolean removed = service.removeUserFromEvent(999, user);
        assertFalse(removed);
    }

    @Test
    void testRemoveUserFromEvent_userNotAttending() {
        User user = new User(); // A user not in any event
        user.setId(99);
        
        boolean removed = service.removeUserFromEvent(1, user);
        assertFalse(removed);
    }
}
