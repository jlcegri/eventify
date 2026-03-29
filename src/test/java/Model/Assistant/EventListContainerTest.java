package Model.Assistant;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventListContainerTest {

    @Test
    void testGettersAndSetters() {
        EventListContainer container = new EventListContainer();
        List<EventLlmOutput> list = new ArrayList<>();
        EventLlmOutput event = new EventLlmOutput();
        event.setTitle("Test");
        list.add(event);

        container.setEvents(list);

        assertNotNull(container.getEvents());
        assertEquals(1, container.getEvents().size());
        assertEquals("Test", container.getEvents().get(0).getTitle());
    }

    @Test
    void testContainer() {
        EventListContainer c = new EventListContainer();
        c.setEvents(new ArrayList<>());
        assertNotNull(c.getEvents());
    }
}