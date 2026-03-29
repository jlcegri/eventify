package Persistence;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and Integration tests for DatabaseManager.
 */
class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeAll
    static void initTestDB() {
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
            stmt.executeUpdate("ALTER TABLE EVENTS ALTER COLUMN ID RESTART WITH 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    @DisplayName("You should get a valid connection")
    void testGetConnection() {
        try (Connection conn = dbManager.getConnection()) {
            assertNotNull(conn, "The connection should not be null");
            assertFalse(conn.isClosed(), "The connection should be open");
        } catch (Exception e) {
            fail("Error connecting: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("You should verify that the USERS and EVENTS tables exist")
    void testTablesExist() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Verify USERS table
            ResultSet rsUsers = stmt.executeQuery("SELECT * FROM USERS LIMIT 0");
            assertNotNull(rsUsers);

            // Verify EVENTS table
            ResultSet rsEvents = stmt.executeQuery("SELECT * FROM EVENTS LIMIT 0");
            assertNotNull(rsEvents);

        } catch (Exception e) {
            fail("The tables were not created correctly: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("It should allow you to insert and retrieve a user")
    void testInsertUser() {
        String insertSql = "INSERT INTO USERS (firstName, lastName, email, password) VALUES ('Test', 'User', 'test@test.com', '1234')";
        String selectSql = "SELECT firstName, lastName FROM USERS WHERE email = 'test@test.com'";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(insertSql);
            assertEquals(1, rowsAffected);

            ResultSet rs = stmt.executeQuery(selectSql);
            assertTrue(rs.next());
            assertEquals("Test", rs.getString("firstName"));
            assertEquals("User", rs.getString("lastName"));

        } catch (Exception e) {
            fail("Database operation error: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.getInstance().close();
    }
}