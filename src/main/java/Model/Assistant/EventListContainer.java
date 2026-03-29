package Model.Assistant;

import java.util.List;

public class EventListContainer {
    public List<EventLlmOutput> events;

    // Default constructor is required
    public EventListContainer() {}

    // Optional: Add a getter/setter if preferred
    public List<EventLlmOutput> getEvents() { return events; }
    public void setEvents(List<EventLlmOutput> events) { this.events = events; }
}
