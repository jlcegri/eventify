package Model.Entities;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class EventTest {

    private Event event;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        date = LocalDate.of(2025, 5, 10);
        startTime = LocalTime.of(18, 30);
        endTime = LocalTime.of(22, 0);

        event = new Event(
                1,
                100,
                "Rock Concert",
                "A music event",
                date,
                startTime,
                endTime,
                "Street 123",
                "Madrid",
                "Music",
                List.of("Rock", "Live"),
                500,
                "https://example.com/image.jpg",
                10.0
        );
    }

    @Test
    void testFullConstructor() {
        assertEquals(1, event.getId());
        assertEquals(100, event.getCreatorID());
        assertEquals("Rock Concert", event.getTitle());
        assertEquals("A music event", event.getDescription());
        assertEquals(date, event.getDate());
        assertEquals(startTime, event.getStartTime());
        assertEquals(endTime, event.getEndTime());
        assertEquals("Street 123", event.getLocation());
        assertEquals("Madrid", event.getCity());
        assertEquals("Music", event.getEventType());
        assertEquals(List.of("Rock", "Live"), event.getInterests());
        assertEquals(500, event.getMaxAttendees());
        assertEquals("https://example.com/image.jpg", event.getImageURL());
        assertEquals(10.0, event.getPrice());
    }

    @Test
    void testEmptyConstructorAndSetters() {
        Event emptyEvent = new Event();
        List<String> interests = new ArrayList<>();
        interests.add("Culture");

        emptyEvent.setId(2);
        emptyEvent.setCreatorID(200);
        emptyEvent.setTitle("Test Event");
        emptyEvent.setDescription("Description");
        emptyEvent.setDate(date);
        emptyEvent.setStartTime(startTime);
        emptyEvent.setEndTime(endTime);
        emptyEvent.setLocation("Venue");
        emptyEvent.setCity("Seville");
        emptyEvent.setEventType("Culture");
        emptyEvent.setInterests(interests);
        emptyEvent.setMaxAttendees(1000);
        emptyEvent.setImageURL("img.png");
        emptyEvent.setPrice(25.5);

        assertEquals(2, emptyEvent.getId());
        assertEquals(200, emptyEvent.getCreatorID());
        assertEquals("Test Event", emptyEvent.getTitle());
        assertEquals("Description", emptyEvent.getDescription());
        assertEquals(date, emptyEvent.getDate());
        assertEquals(startTime, emptyEvent.getStartTime());
        assertEquals(endTime, emptyEvent.getEndTime());
        assertEquals("Venue", emptyEvent.getLocation());
        assertEquals("Seville", emptyEvent.getCity());
        assertEquals("Culture", emptyEvent.getEventType());
        assertEquals(interests, emptyEvent.getInterests());
        assertEquals(1000, emptyEvent.getMaxAttendees());
        assertEquals("img.png", emptyEvent.getImageURL());
        assertEquals(25.5, emptyEvent.getPrice());
    }

    @Test
    void testAddAndRemoveUser() {
        User user1 = new User();
        user1.setId(1);
        User user2 = new User();
        user2.setId(2);

        event.addUser(user1);
        assertTrue(event.getUsers().contains(user1));

        event.addUser(user2);
        assertTrue(event.getUsers().contains(user2));

        event.removeUser(user1);
        assertFalse(event.getUsers().contains(user1));
    }

    @Test
    void testSettersAllowNull() {
        event.setTitle(null);
        event.setDescription(null);
        event.setDate(null);
        event.setStartTime(null);
        event.setEndTime(null);
        event.setLocation(null);
        event.setCity(null);
        event.setEventType(null);
        event.setInterests(null);
        event.setMaxAttendees(null);
        event.setImageURL(null);
        event.setUsers(null);

        assertNull(event.getTitle());
        assertNull(event.getDescription());
        assertNull(event.getDate());
        assertNull(event.getStartTime());
        assertNull(event.getEndTime());
        assertNull(event.getLocation());
        assertNull(event.getCity());
        assertNull(event.getEventType());
        assertNull(event.getInterests());
        assertNull(event.getMaxAttendees());
        assertNull(event.getImageURL());
        assertNull(event.getUsers());
    }

    @Test
    void testToStringContainsFields() {
        String result = event.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("creatorId=100"));
        assertTrue(result.contains("title='Rock Concert'"));
        assertTrue(result.contains("location='Street 123'"));
        assertTrue(result.contains("date=" + date.toString()));
        assertTrue(result.contains("startTime=" + startTime.toString()));
        assertTrue(result.contains("endTime=" + endTime.toString()));
        assertTrue(result.contains("price=10.0"));
    }
}
