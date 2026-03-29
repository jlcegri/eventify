package Model.Assistant;

import Model.Entities.Event;
import Model.Services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventSearchOrchestratorTest {

    private EventSearchOrchestrator orchestrator;

    @Mock
    private EventSearchService mockSearchService;

    @Mock
    private EventService mockEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orchestrator = new EventSearchOrchestrator(mockSearchService, mockEventService);
    }

    @Test
    void testSearchAndMapEvents_Successful() {
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("AI Event");
        llmEvent.setCity("Porto");
        llmEvent.setDate("2026-01-01");
        llmEvent.setStartTime("10:00");

        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(mockEventService.addEvent(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertEquals(1, results.size());
        assertEquals("AI Event", results.get(0).getTitle());
        verify(mockEventService, times(1)).addEvent(any(Event.class));
    }

    @Test
    void testSearch_HandlesDuplicates() {
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("Existing Event");
        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        Event existing = new Event();
        existing.setTitle("Existing Event");
        existing.setId(55);

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(List.of(existing));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertEquals(1, results.size());
        assertEquals(55, results.get(0).getId());
        verify(mockEventService, never()).addEvent(any(Event.class));
    }

    @Test
    void testSearch_HandlesNullService() {
        EventSearchOrchestrator nullOrch = new EventSearchOrchestrator(null, mockEventService);
        List<Event> res = nullOrch.searchAndMapEvents("q");
        assertTrue(res.isEmpty());
    }

    @Test
    void testSearch_HandlesExceptionInService() {
        // Simulate an exception thrown by the search service
        when(mockSearchService.findEvents(anyString())).thenThrow(new RuntimeException("API Error"));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertTrue(results.isEmpty(), "Should return empty list on service exception");
    }

    @Test
    void testMapAndCacheEvent_BadDateParsing() {
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("Bad Date Event");
        llmEvent.setDate("invalid-date-format"); // Irá lançar exceção
        llmEvent.setStartTime("10:00");

        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(mockEventService.addEvent(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertFalse(results.isEmpty());
        // Verifica se usou LocalDate.now() como fallback
        assertEquals(LocalDate.now(), results.get(0).getDate());
    }

    @Test
    void testMapAndCacheEvent_NullDate() {
        // Teste para cobertura do else quando data é null
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("Null Date Event");
        llmEvent.setDate(null);

        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(mockEventService.addEvent(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertFalse(results.isEmpty());
        assertEquals(LocalDate.now(), results.get(0).getDate());
    }

    @Test
    void testMapAndCacheEvent_BadTimeParsing() {
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("Bad Time Event");
        llmEvent.setDate("2025-12-12");
        llmEvent.setStartTime("99:99"); // Inválido

        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(mockEventService.addEvent(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertFalse(results.isEmpty());
        // Verifica o fallback time (12:00)
        assertEquals(LocalTime.of(12, 0), results.get(0).getStartTime());
    }

    @Test
    void testMapAndCacheEvent_NullTime() {
        // Teste para cobertura do else quando tempo é null
        EventLlmOutput llmEvent = new EventLlmOutput();
        llmEvent.setTitle("Null Time Event");
        llmEvent.setDate("2025-12-12");
        llmEvent.setStartTime(null);

        EventListContainer container = new EventListContainer();
        container.setEvents(List.of(llmEvent));

        when(mockSearchService.findEvents(anyString())).thenReturn(container);
        when(mockEventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(mockEventService.addEvent(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        List<Event> results = orchestrator.searchAndMapEvents("query");

        assertFalse(results.isEmpty());
        // Verifica o fallback time (12:00)
        assertEquals(LocalTime.of(12, 0), results.get(0).getStartTime());
    }
}