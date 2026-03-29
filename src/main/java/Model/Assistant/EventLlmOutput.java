// EventLlmOutput.java (New File)

package Model.Assistant;

import java.util.List;

public class EventLlmOutput {
    // LLM-friendly types for extraction
    private String title;
    private String description;

    // IMPORTANT: Date/Time as String, instructing the LLM on format
    private String date;
    private String startTime;
    private String endTime;

    private String location;
    private String city;
    private String eventType;
    private List<String> interests;
    private Integer maxAttendees;
    private double price;
    private String imageURL;

    // Must include a default no-argument constructor for deserialization
    public EventLlmOutput() {}
    // You must include all necessary public getters and setters!
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {return city;}

    public void setCity(String city) {this.city = city;}

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String category) {
        this.eventType = category;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {this.date = date;}

    public String getStartTime() {return startTime;}

    public void setStartTime(String time) { this.startTime = time; }

    public String getEndTime() {return endTime;}

    public void setEndTime(String endTime) {this.endTime = endTime;}

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}