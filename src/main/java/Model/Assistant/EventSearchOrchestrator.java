package Model.Assistant;

import Model.Entities.Event;
import Model.Services.EventService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EventSearchOrchestrator {

    private final EventSearchService eventSearchService;
    private final EventService eventService;

    public EventSearchOrchestrator(EventSearchService eventSearchService, EventService eventService) {
        this.eventSearchService = eventSearchService;
        this.eventService = eventService;
    }

    public List<Event> searchAndMapEvents(String query) {
        System.out.println("entrei" + query);
        if (eventSearchService == null) {
            System.err.println("LLM Search Service is not initialized.");
            return Collections.emptyList();
        }

        try {
            System.out.println("Executing LLM Query: " + query);
            EventListContainer container = eventSearchService.findEvents(query);
            List<EventLlmOutput> llmOutputEvents = (container != null && container.events != null)
                    ? container.events : Collections.emptyList();

            return llmOutputEvents.stream()
                    .map(this::mapAndCacheEvent)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("FATAL: LLM Search execution failed: " + e.getMessage());
            System.out.println("erro" + query);
            return Collections.emptyList();
        }
    }

    private Event mapAndCacheEvent(EventLlmOutput dto) {
        // Check if an event with the same title already exists
        Event existingEvent = eventService.getAllEvents().stream()
                .filter(e -> e.getTitle().equalsIgnoreCase(dto.getTitle()))
                .findFirst()
                .orElse(null);

        if (existingEvent != null) {
            System.out.println("--- Event Found in Cache ---");
            System.out.println("Title: " + existingEvent.getTitle());
            System.out.println("ID: " + existingEvent.getId());
            System.out.println("--------------------------");
            return existingEvent;
        }

        // If not, map the DTO to a new Event object
        System.out.println("--- Generating New Event ---");
        System.out.println("Title: " + dto.getTitle());
        System.out.println("City: " + dto.getCity());
        System.out.println("Date: " + dto.getDate());
        System.out.println("Start Time: " + dto.getStartTime());
        System.out.println("End Time: " + dto.getEndTime());
        System.out.println("Price: " + dto.getPrice());
        System.out.println("Image URL: " + dto.getImageURL());
        System.out.println("--------------------------");

        Event newEvent = new Event();
        // Set creatorID to 1 (Default User) to satisfy Foreign Key constraints
        newEvent.setCreatorID(1); 
        newEvent.setTitle(dto.getTitle());
        newEvent.setDescription(dto.getDescription());
        newEvent.setPrice(dto.getPrice());
        newEvent.setLocation(dto.getLocation());
        newEvent.setCity(dto.getCity());
        newEvent.setEventType(dto.getEventType());
        newEvent.setInterests(dto.getInterests());
        newEvent.setMaxAttendees(dto.getMaxAttendees());
        newEvent.setImageURL(dto.getImageURL());

        // Date/Time Conversion
        LocalDate eventDate;
        if (dto.getDate() != null) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                eventDate = LocalDate.parse(dto.getDate(), dateFormatter);
            } catch (DateTimeParseException e) {
                System.err.println("Failed to parse date for event: " + dto.getTitle() + ". Using current date.");
                eventDate = LocalDate.now();
            }
        } else {
            eventDate = LocalDate.now();
        }

        LocalTime eventStartTime;
        LocalTime eventEndTime;
        if (dto.getStartTime() != null) {
            try {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                eventStartTime = LocalTime.parse(dto.getStartTime(), timeFormatter);
                eventEndTime = LocalTime.parse(dto.getStartTime(), timeFormatter);
            } catch (DateTimeParseException e) {
                System.err.println("Failed to parse time for event: " + dto.getTitle() + ". Using default time 12:00.");
                eventStartTime = LocalTime.of(12, 0);
                eventEndTime = LocalTime.of(12, 0);
            }
        } else {
             System.err.println("Time not provided for event: " + dto.getTitle() + ". Using default time 12:00.");
             eventStartTime = LocalTime.of(12, 0);
             eventEndTime = LocalTime.of(12, 0);
        }

        LocalDateTime combinedDateTime = eventDate.atTime(eventStartTime);

        newEvent.setDate(eventDate);
        newEvent.setStartTime(eventStartTime);
        newEvent.setEndTime(eventEndTime);
        // Add the new event to the central EventService
        return eventService.addEvent(newEvent);
    }
}