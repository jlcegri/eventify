package Model.Assistant;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventLlmOutputTest {
    @Test
    void testPOJO() {
        EventLlmOutput e = new EventLlmOutput();
        e.setTitle("T"); e.setDescription("D"); e.setDate("2025-01-01");
        e.setStartTime("10:00"); e.setEndTime("11:00"); e.setLocation("L");
        e.setCity("C"); e.setEventType("E"); e.setInterests(List.of("I"));
        e.setMaxAttendees(10); e.setPrice(5.0); e.setImageURL("url");

        assertEquals("T", e.getTitle());
        assertEquals("D", e.getDescription());
        assertEquals("2025-01-01", e.getDate());
        assertEquals("10:00", e.getStartTime());
        assertEquals("11:00", e.getEndTime());
        assertEquals("L", e.getLocation());
        assertEquals("C", e.getCity());
        assertEquals("E", e.getEventType());
        assertEquals("I", e.getInterests().get(0));
        assertEquals(10, e.getMaxAttendees());
        assertEquals(5.0, e.getPrice());
        assertEquals("url", e.getImageURL());
    }
}