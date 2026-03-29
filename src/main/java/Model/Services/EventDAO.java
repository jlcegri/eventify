package Model.Services;

import Model.Entities.Event;
import Model.Entities.User;
import java.util.List;

public interface EventDAO {
    void save(Event event);
    Event findById(int id);
    List<Event> findAll();
    List<Event> findByCreator(int userId);
    boolean update(Event event);
    boolean delete(int id);

    // Methods for the N:M relationship (Attendees)
    boolean addUserToEvent(int eventId, int userId);
    boolean removeUserFromEvent(int eventId, int userId);
    List<User> findAttendeesByEventId(int eventId);
    void loadAttendees(Event event);
}