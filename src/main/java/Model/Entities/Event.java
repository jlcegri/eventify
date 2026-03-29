package Model.Entities;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private int creatorID;

    private String title;
    private String description;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private String location;
    private String city;
    private String eventType;
    private List<String> interests;

    private Integer maxAttendees;
    private String imageURL;
    private List<User> users;
    private double price;


    public Event(){
        this.interests = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public Event(int id, int creatorID, String title, String description, LocalDate date,
                 LocalTime startTime, LocalTime endTime, String location, String city, String eventType,
                 List<String> interests, Integer maxAttendees, String imageUrl, double price) {
        this.id = id;
        this.creatorID = creatorID;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.city = city;
        this.eventType = eventType;
        this.interests = interests != null ? interests : new ArrayList<>();
        this.maxAttendees = maxAttendees;
        this.imageURL = imageUrl;
        this.price = price;
        this.users = new ArrayList<>();
    }

    // Constructor sin ID (para crear un nuevo evento antes de guardarlo)
    public Event(int creatorID, String title, String description, LocalDate date,
                 LocalTime startTime, LocalTime endTime, String location, String city, String eventType,
                 List<String> interests, Integer maxAttendees, String imageUrl, double price) {
        this(0, creatorID, title, description, date, startTime, endTime, location, city, eventType, interests, maxAttendees, imageUrl, price);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCreatorID() { return creatorID; }
    public void setCreatorID(int creatorID) { this.creatorID = creatorID; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
    public Integer getMaxAttendees() { return maxAttendees; }
    public void setMaxAttendees(Integer maxAttendees) { this.maxAttendees = maxAttendees; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Métodos de gestión de asistentes
    public boolean addUser(User user) {
        if (this.users.stream().noneMatch(u -> u.getId() == user.getId())) {
            this.users.add(user);
            return true;
        }
        return false;
    }
    public boolean removeUser(User user) {
        return this.users.removeIf(u -> u.getId() == user.getId());
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", creatorId=" + creatorID +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", price=" + price +
                '}';
    }

}