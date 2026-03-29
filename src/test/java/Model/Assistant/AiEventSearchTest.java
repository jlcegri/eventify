package Model.Assistant;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiEventSearchTest {

    @Test
    void testPerformSearch_returnsEvents_whenServiceFindsThem() {
        // 1. Mock the Service and the Container
        EventSearchService mockService = Mockito.mock(EventSearchService.class);
        EventListContainer mockContainer = new EventListContainer();

        // Create a fake event
        EventLlmOutput fakeEvent = new EventLlmOutput();
        fakeEvent.setTitle("Test Cooking Class");
        mockContainer.events = List.of(fakeEvent);

        // 2. Define behavior
        String query = "cooking";
        when(mockService.findEvents(query)).thenReturn(mockContainer);

        // 3. Run test
        AiEventSearch searchApp = new AiEventSearch();
        List<EventLlmOutput> results = searchApp.performSearch(mockService, query);

        // 4. Assert
        assertEquals(1, results.size());
        assertEquals("Test Cooking Class", results.get(0).getTitle());
    }

    @Test
    void testPerformSearch_handlesEmptyResults() {
        EventSearchService mockService = Mockito.mock(EventSearchService.class);
        // Service returns null
        when(mockService.findEvents(anyString())).thenReturn(null);

        AiEventSearch searchApp = new AiEventSearch();
        List<EventLlmOutput> results = searchApp.performSearch(mockService, "anything");

        assertTrue(results.isEmpty());
    }
}