package Model.Entities;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id; ///> pongo id porque puede ser útil para la base de datos, aunque no esté explícito en el el mockup
    private String name;
    private String email;
    private String password; // Corresponde al campo 'Password'
    private String location;
    private List<String> interests;
    private String bio;
    private List<String> eventTypes;
    private String profileImagePath;
    private List<Event> events;
    private List<Event> createdEvents;
    private boolean firstEntry;


    // 2. Constructor
    /**
     * Constructor parametrizado para inicializar un nuevo objeto de tipo User
     * @param id id del usuario
     * @param name nombre del usuario
     * @param email dirección de correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @param location La ubicación del usuario.
     * @param interests La lista de intereses del usuario.
     * @param eventTypes La lista de tipos de eventos del usuario.
     * @param bio La biografía del usuario.
     */
    public User(int id, String name, String email, String password, String location, List<String> interests, List<String> eventTypes, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.location = location;
        this.interests = interests;
        this.bio = bio;
        this.eventTypes = eventTypes;
        this.profileImagePath = "/src/main/resources/images/missing-image.png";
        this.events = new ArrayList<>();
        this.createdEvents = new ArrayList<>();
        this.firstEntry = true;
    }

    /**
     * @brief Constructor por defecto
     */
    public User() {
        this.events = new ArrayList<>();
        this.createdEvents = new ArrayList<>();
        this.firstEntry = true;
    }

    /**
     * Getter de id
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Getter de name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter de email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Getter de password
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter de id
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Setter de name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter de email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Setter de password
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Método para devolver los datos de un usuario
     * @return datos de un usuario
     */
    @Override
    public String toString() { //"User{"
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                ", interests=" + interests +
                ", eventTypes=" + eventTypes +
                ", bio='" + bio + '\'' +
                ", profileImagePath='" + profileImagePath + '\'' +
                ", events=" + events +
                ", createdEvents=" + createdEvents +
                '}';
    }

    /**
     * Getter de location
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Getter de interests
     * @return interests
     */
    public List<String> getInterests() {
        return interests;
    }

    /**
     * Getter de eventTypes
     * @return eventTypes
     */
    public List<String> getEventTypes() {
        return eventTypes;
    }



    /**
     * Getter de bio
     * @return bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * Setter de location
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Setter de interests
     * @param interests
     */
    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    /**
     * Setter de eventTypes
     * @param eventTypes
     */
    public void setEventTypes(List<String> eventTypes) {
        this.eventTypes = eventTypes;
    }

    /**
     * Setter de bio
     * @param bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Getter de profileImagePath
     * @return profileImagePath
     */
    public String getProfileImagePath() {
        return profileImagePath;
    }

    /**
     * Setter de profileImagePath
     * @param profileImagePath
     */
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    /**
     * Getter de events
     * @return events
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Add event to user
     * @param event
     */
    public void addEvent(Event event) {
        this.events.add(event);
    }

    /**
     * Remove event from user
     * @param event
     * @return
     */
    public boolean removeEvent(Event event) {
        return this.events.remove(event);
    }

    /**
     * Getter de createdEvents
     * @return createdEvents
     */
    public List<Event> getCreatedEvents() {
        return createdEvents;
    }

    /**
     * Add created event to user
     * @param event
     */
    public void addCreatedEvent(Event event) {
        this.createdEvents.add(event);
    }

    /**
     * Remove created event from user
     * @param event
     * @return
     */
    public boolean removeCreatedEvent(Event event) {
        return this.createdEvents.remove(event);
    }

    /**
     * Getter de firstEntry
     * @return firstEntry
     */
    public boolean isFirstEntry() {
        return firstEntry;
    }

    /**
     * Setter de firstEntry
     * @param firstEntry
     */
    public void setFirstEntry(boolean firstEntry) {
        this.firstEntry = firstEntry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
