package Model.Services;

import Model.Entities.Event;
import Model.Entities.User;
import Persistence.JdbcUserDAO;
import java.util.List;

/**
 * @brief Service to manage user-related operations.
 * Implements the Singleton pattern. Delegates persistence to UserDAO.
 */
public class UserService {

    private final UserDAO userDAO;

    private static UserService instance;

    private EventService eventService;

    // 0 or -1 indicates "not logged in"
    private int currentUserID = 0;


    /**
     * @brief Private constructor to avoid direct instantiation.
     * Initializes the UserDAO.
     */
    protected UserService() {
        // Initialization of the concrete DAO implementation
        this.userDAO = new JdbcUserDAO();
    }

    /**
     * @brief Static method to get the single instance of the UserService class.
     * @return The only instance of UserService.
     */
    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    /**
     * @brief Lazily initializes and returns the EventService instance.
     * @return The singleton instance of EventService.
     */
    private EventService getEventService() {
        if (eventService == null) {
            eventService = EventService.getInstance();
        }
        return eventService;
    }

    /**
     * @brief Add a new user (Registration). Checks for email existence.
     * @param user The user object to save.
     * @return The saved user object, including the generated ID.
     */
    public User addUser(User user) {
        // Business Logic: Check if the email already exists before saving
        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists.");
        }
        // Persistence: The DAO handles ID generation and saving.
        userDAO.save(user);
        return user;
    }

    /**
     * @brief Gets a user by its ID.
     */
    public User getUserById(int id) {
        return userDAO.findById(id); // Delegation to DAO
    }

    /**
     * @brief Gets all users.
     */
    public List<User> getAllUsers() {
        return userDAO.findAll(); // Delegation to DAO
    }

    /**
     * @brief Updates an existing user's information.
     */
    public User updateUser(User updatedUser) {
        // Delegation: The DAO handles the update logic.
        boolean updated = userDAO.update(updatedUser);

        // Return the updated object if the operation was successful
        return updated ? userDAO.findById(updatedUser.getId()) : null;
    }

    /**
     * @brief Delete a user by their ID.
     */
    public boolean deleteUser(int id) {
        return userDAO.delete(id); // Delegation to DAO
    }


    /**
     * @brief Adds a NEW user and sets them as current (REGISTRATION).
     */
    public User addUserCurrent(User user) {
        User savedUser = addUser(user);
        currentUserID = savedUser.getId();
        return savedUser;
    }

    /**
     * @brief Sets the currently logged-in user (LOGIN).
     */
    public void setCurrentUser(User user) {
        this.currentUserID = user.getId();
    }


    /**
     * @brief Retrieves the currently active user based on currentUserID.
     * @return The current User object, or null if no user is active.
     */
    public User getCurrentUser() {
        if (currentUserID <= 0) {
            return null; // No active user
        }
        return userDAO.findById(currentUserID); // Use DAO
    }


    /**
     * @brief Registers a user as an attendee for a specific event.
     * @param event The Event to join.
     * @param user The User who wants to join.
     * @return true if the user was successfully added as an attendee, false otherwise.
     */
    public boolean joinEvent(Event event, User user) {
        if (event == null || user == null) {
            System.err.println("UserService: Cannot join event. Event or User is null.");
            return false;
        }
        return getEventService().addUserToEvent(event.getId(), user);
    }

    /**
     * @brief Unregisters a user from an event using the EventService.
     * @param event The Event to leave.
     * @param user The User who wants to leave.
     * @return true if the user was successfully removed, false otherwise.
     */
    public boolean removeEventFromUser(Event event, User user) {
        if (event == null || user == null) {
            System.err.println("UserService: Cannot leave event. Event or User is null.");
            return false;
        }

        boolean removed = getEventService().removeUserFromEvent(event.getId(), user);

        if (removed) {
            System.out.println("UserService: User ID " + user.getId() + " successfully left Event ID " + event.getId());
        } else {
            System.err.println("UserService: Failed to remove User ID " + user.getId() + " from Event ID " + event.getId() + ". User was likely not attending.");
        }

        return removed;
    }


    /**
     * @brief Authenticates a user based on email and password.
     */
    public User login(String email, String password) {
        // Delegate authentication and object retrieval to the DAO
        User user = userDAO.validateLogin(email, password);

        if (user != null) {
            setCurrentUser(user); // Set the current user if login is successful
        }
        return user;
    }

    /**
     * @brief Logs out the current user.
     */
    public void logout() {
        this.currentUserID = 0;
    }
}