package Model.Services;

import Model.Entities.Event;
import Model.Entities.User;
import Persistence.JdbcEventDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @brief Service to manage operations related to events.
 * Implements the Singleton pattern. Delegates persistence to EventDAO.
 */
public class EventService {

    private final EventDAO eventDAO;

    private static EventService instance;

    /**
     * @brief Private constructor to prevent direct instantiation.
     * Initializes the EventDAO.
     */
    protected EventService() {
        this.eventDAO = new JdbcEventDAO();
    }

    /**
     * @brief Static method to get the single instance of the EventService class.
     * @return The single instance of EventService.
     */
    public static EventService getInstance() {
        if (instance == null) {
            synchronized (EventService.class) {
                if (instance == null) {
                    instance = new EventService();
                }
            }
        }
        return instance;
    }

    /**
     * @brief Adds a new event and persists it via DAO.
     * @param event The Event object to add.
     * @return The saved Event object with the generated ID.
     */
    public Event addEvent(Event event) {
        eventDAO.save(event);
        return event;
    }

    /**
     * @brief Retrieves an event by its ID.
     * @param eventId The ID of the event.
     * @return The Event object or null if not found.
     */
    public Event getEventById(int eventId) {
        return eventDAO.findById(eventId);
    }

    /**
     * @brief Retrieves all events from the repository.
     * @return A list of all Event objects.
     */
    public List<Event> getAllEvents() {
        return eventDAO.findAll();
    }

    /**
     * @brief Retrieves all events created by a specific user.
     * @param userId The ID of the creator user.
     * @return A list of events created by the user.
     */
    public List<Event> getEventsByCreator(int userId) {
        return eventDAO.findByCreator(userId);
    }

    /**
     * @brief Updates an existing event.
     * @param event The updated Event object.
     * @return The updated Event object or null if update failed.
     */
    public Event updateEvent(Event event) {
        boolean updated = eventDAO.update(event);
        return updated ? eventDAO.findById(event.getId()) : null;
    }

    /**
     * @brief Deletes an event by ID.
     * @param eventId The ID of the event to delete.
     * @return true if deleted, false otherwise.
     */
    public boolean deleteEvent(int eventId) {
        return eventDAO.delete(eventId);
    }

    // --- ATTENDEE MANAGEMENT (Called by UserService) ---

    /**
     * @brief Adds a user to an event's attendee list and persists the relationship.
     * @param eventId The ID of the event to join.
     * @param user The User object to add to the event.
     * @return true if the user was successfully added, false otherwise.
     */
    public boolean addUserToEvent(int eventId, User user) {
        Event event = getEventById(eventId);

        if (event == null) {
            System.err.println("EventService: Cannot join, Event ID " + eventId + " not found.");
            return false;
        }

        if (event.getMaxAttendees() != null && event.getUsers().size() >= event.getMaxAttendees()) {
            System.err.println("EventService: Event ID " + eventId + " is full.");
            return false;
        }

        // 1. Persist the relationship in the join table
        boolean persisted = eventDAO.addUserToEvent(eventId, user.getId());

        if (persisted) {
            // 2. Update the in-memory state of the event
            event.addUser(user);
            System.out.println("EventService: User " + user.getId() + " joined Event " + eventId + " successfully.");
            return true;
        } else {
            System.err.println("EventService: User " + user.getId() + " is already in Event " + eventId + " or persistence failed.");
            return false;
        }
    }


    /**
     * @brief Removes a user from an event's attendee list and persists the removal.
     * @param eventId The ID of the event to leave.
     * @param user The User object to remove from the event.
     * @return true if the user was successfully removed, false otherwise.
     */
    public boolean removeUserFromEvent(int eventId, User user) {
        Event event = getEventById(eventId);

        if (event == null) {
            System.err.println("EventService: Cannot leave, Event ID " + eventId + " not found.");
            return false;
        }

        // 1. Remove the relationship from the join table
        boolean removed = eventDAO.removeUserFromEvent(eventId, user.getId());

        if (removed) {
            // 2. Update the in-memory state of the event
            event.removeUser(user);
            System.out.println("EventService: User " + user.getId() + " removed from Event " + eventId + " successfully.");
            return true;
        } else {
            System.err.println("EventService: User " + user.getId() + " was not attending Event " + eventId + ".");
            return false;
        }
    }

    /**
     * @brief Filters events by search text in title or description.
     */
    public List<Event> searchEvents(String searchText) {
        String lowerCaseText = searchText.toLowerCase();
        return eventDAO.findAll().stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lowerCaseText) ||
                        e.getDescription().toLowerCase().contains(lowerCaseText) ||
                        e.getCity().toLowerCase().contains(lowerCaseText) ||
                        e.getLocation().toLowerCase().contains(lowerCaseText))
                .collect(Collectors.toList());
    }



}