package Persistence;

import Model.Entities.User;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and Integration tests for JdbcUserDAO.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcUserDAOTest {

    private JdbcUserDAO userDAO;
    private User testUser;

    @BeforeAll
    void init() {
        DatabaseManager.initTestDatabase();
        // Initialize the DAO before running tests
        userDAO = new JdbcUserDAO();
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();

        // Prepare a fresh User object for each test case
        testUser = new User(
                0,
                "John Doe",
                "john.doe@example.com",
                "password123",
                "New York",
                Arrays.asList("Music", "Tech"),
                Arrays.asList("Concert", "Workshop"),
                "Hello, I am a software enthusiast."
        );
    }

    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Should save a user and retrieve a generated ID")
    void testSaveUser() {
        userDAO.save(testUser);

        // Verify that the ID was set by the database
        assertNotEquals(0, testUser.getId(), "User ID should be updated after saving to DB.");

        // Retrieve and verify data integrity
        User retrieved = userDAO.findById(testUser.getId());
        assertNotNull(retrieved);
        assertEquals("John Doe", retrieved.getName());
        assertEquals("john.doe@example.com", retrieved.getEmail());
    }

    @Test
    @DisplayName("Should find a user by email")
    void testFindByEmail() {
        userDAO.save(testUser);

        User retrieved = userDAO.findByEmail("john.doe@example.com");

        assertNotNull(retrieved);
        assertEquals(testUser.getId(), retrieved.getId());
    }

    @Test
    @DisplayName("Should validate login credentials correctly")
    void testValidateLogin() {
        userDAO.save(testUser);

        // Test correct credentials
        User validUser = userDAO.validateLogin("john.doe@example.com", "password123");
        assertNotNull(validUser, "Login should succeed with correct credentials.");

        // Test wrong password
        User invalidUser = userDAO.validateLogin("john.doe@example.com", "wrong_pass");
        assertNull(invalidUser, "Login should fail with incorrect password.");
    }

    @Test
    @DisplayName("Should update user profile information")
    void testUpdateUser() {
        userDAO.save(testUser);

        // Update local object
        testUser.setName("John Smith");
        testUser.setLocation("Los Angeles");
        testUser.setInterests(Arrays.asList("Sports"));

        boolean isUpdated = userDAO.update(testUser);
        assertTrue(isUpdated, "The update operation should return true.");

        // Verify database state
        User retrieved = userDAO.findById(testUser.getId());
        assertEquals("John Smith", retrieved.getName());
        assertEquals("Los Angeles", retrieved.getLocation());
        assertTrue(retrieved.getInterests().contains("Sports"));
    }

    @Test
    @DisplayName("Should delete a user from the database")
    void testDeleteUser() {
        userDAO.save(testUser);
        int id = testUser.getId();

        boolean isDeleted = userDAO.delete(id);
        assertTrue(isDeleted);

        User retrieved = userDAO.findById(id);
        assertNull(retrieved, "User should not exist after deletion.");
    }

    @Test
    @DisplayName("Should correctly handle Name Splitting logic")
    void testNameSplitting() {
        // We test this by saving a user with a multipart name
        // and checking if mapResultSetToUser reconstructs it
        User complexNameUser = new User(0, "Maria Garcia Lopez", "m.garcia@test.com", "pass", "Madrid", null, null, "");

        userDAO.save(complexNameUser);
        User retrieved = userDAO.findById(complexNameUser.getId());

        // Based on JdbcUserDAO logic: firstName="Maria", lastName="Garcia Lopez"
        // mapResultSetToUser should join them back to "Maria Garcia Lopez"
        assertEquals("Maria Garcia Lopez", retrieved.getName());
    }

    @Test
    @DisplayName("Should handle empty interests or event types")
    void testEmptyLists() {
        testUser.setInterests(null);
        testUser.setEventTypes(new ArrayList<>());

        userDAO.save(testUser);
        User retrieved = userDAO.findById(testUser.getId());

        assertNotNull(retrieved.getInterests());
        assertTrue(retrieved.getInterests().isEmpty());
    }
}