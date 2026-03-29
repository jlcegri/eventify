package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import UI.Services.NavigationService;
import Persistence.DatabaseManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EventCardControllerTest {

    private EventCardController controller;
    private Event testEvent;
    private User testUser;

    // UI Mocks
    private ImageView eventImageView;
    private Label titleLabel, eventTypeLabel, descriptionLabel, dateLabel, locationLabel, priceLabel, attendeesLabel;
    private Button joinButton;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(UserService.class);
        resetSingleton(EventService.class);
        resetSingleton(NavigationService.class);
        cleanDatabase();

        // Setup User
        testUser = new User(1, "Test User", "test@test.com", "pass", "Loc", new ArrayList<>(), new ArrayList<>(), "Bio");
        UserService.getInstance().addUserCurrent(testUser);

        // Setup Controller with Overrides
        controller = new EventCardController();

        // Initialize UI components
        eventImageView = new ImageView();
        titleLabel = new Label();
        eventTypeLabel = new Label();
        descriptionLabel = new Label();
        dateLabel = new Label();
        locationLabel = new Label();
        priceLabel = new Label();
        attendeesLabel = new Label();
        joinButton = new Button();

        // Inject fields
        setField("eventImageView", eventImageView);
        setField("titleLabel", titleLabel);
        setField("eventTypeLabel", eventTypeLabel);
        setField("descriptionLabel", descriptionLabel);
        setField("dateLabel", dateLabel);
        setField("locationLabel", locationLabel);
        setField("priceLabel", priceLabel);
        setField("attendeesLabel", attendeesLabel);
        setField("joinButton", joinButton);

        // Setup Test Event
        testEvent = new Event(1, 1, "Test Card", "Desc", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2),
                "Loc", "City", "Music", new ArrayList<>(), 100, "", 20.0);
        EventService.getInstance().addEvent(testEvent);
    }
    
    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
            stmt.executeUpdate("ALTER TABLE EVENTS ALTER COLUMN ID RESTART WITH 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void resetSingleton(Class<T> clazz) throws Exception {
        try {
            Field instance = clazz.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (NoSuchFieldException e) {}
    }

    private void setField(String name, Object value) throws Exception {
        Field field = EventCardController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void testSetEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.setEvent(testEvent);

            assertEquals("Test Card", titleLabel.getText());
            assertEquals("Music", eventTypeLabel.getText());
            assertNotNull(priceLabel.getText());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleJoinButton_Join() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setEvent(testEvent);
                joinButton.setText("Join");

                // Invoke private HandleJoinButton via FXML injection simulation or reflection?
                // It's private annotated with @FXML.
                java.lang.reflect.Method method = EventCardController.class.getDeclaredMethod("HandleJoinButton");
                method.setAccessible(true);
                method.invoke(controller);

                assertEquals("Joined", joinButton.getText());
                assertTrue(testEvent.getUsers().contains(testUser));
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

}
