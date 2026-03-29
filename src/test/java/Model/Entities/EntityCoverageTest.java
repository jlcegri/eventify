package Model.Entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityCoverageTest {

    @Test
    void testUserEntityAdditionalMethods() {
        User user = new User();
        Event event = new Event();
        event.setId(1);

        // addEvent / removeEvent
        user.addEvent(event);
        assertTrue(user.getEvents().contains(event));
        assertTrue(user.removeEvent(event));
        assertFalse(user.getEvents().contains(event));

        // addCreatedEvent / removeCreatedEvent
        user.addCreatedEvent(event);
        assertTrue(user.getCreatedEvents().contains(event));
        assertTrue(user.removeCreatedEvent(event));
        assertFalse(user.getCreatedEvents().contains(event));

        // hashCode
        user.setId(123);
        assertEquals(Integer.hashCode(123), user.hashCode());
    }

    @Test
    void testEventEntityAdditionalMethods() {
        // Constructor without ID
        Event event = new Event(
                1,
                "Title",
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
        assertEquals(0, event.getId()); // Should default to 0

        // addUser return false
        User user = new User();
        user.setId(1);
        
        assertTrue(event.addUser(user));
        assertFalse(event.addUser(user)); // Should return false because user is already added
    }
}
