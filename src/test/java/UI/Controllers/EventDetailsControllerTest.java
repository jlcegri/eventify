package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EventDetailsControllerTest {

    private EventDetailsController controller;
    private Event testEvent;
    private User testUser;

    // UI Components
    private Button joinEventButton, backButton;
    private Label eventTypeLabel, nameLabel, dateLabel, locationLabel, aboutLabel, organizedByLabel;
    private ImageView eventImageView;
    private VBox Attendees;
    private StackPane profileStack;
    private Button profileButton;
    private Circle profileCircle;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        Persistence.DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        resetSingletons();
        
        testUser = new User(1, "Test User", "test@test.com", "pass", "Loc", new ArrayList<>(), new ArrayList<>(), "Bio");
        UserService.getInstance().setCurrentUser(testUser);

        testEvent = new Event(1, 1, "Test Event", "Desc", LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), "Loc", "City", "Type", new ArrayList<>(), 10, "", 0.0);
        
        mockNavigationData(testEvent);

        controller = new EventDetailsController();
        
        initializeUIComponents();
    }

    private void resetSingletons() throws Exception {
        Field instance = UserService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        instance = NavigationService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private void mockNavigationData(Object data) throws Exception {
        Constructor<NavigationService> constructor = NavigationService.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        NavigationService navService = constructor.newInstance();

        Field instanceField = NavigationService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, navService);

        Field dataField = NavigationService.class.getDeclaredField("data");
        dataField.setAccessible(true);
        dataField.set(navService, data);
    }

    private void initializeUIComponents() throws Exception {
        joinEventButton = new Button(); backButton = new Button();
        eventTypeLabel = new Label(); nameLabel = new Label();
        dateLabel = new Label(); locationLabel = new Label();
        aboutLabel = new Label(); organizedByLabel = new Label();
        eventImageView = new ImageView(); Attendees = new VBox();
        profileStack = new StackPane(); profileButton = new Button();
        profileCircle = new Circle();

        setField("joinEventButton", joinEventButton); setField("backButton", backButton);
        setField("eventTypeLabel", eventTypeLabel); setField("nameLabel", nameLabel);
        setField("dateLabel", dateLabel); setField("locationLabel", locationLabel);
        setField("aboutLabel", aboutLabel); setField("organizedByLabel", organizedByLabel);
        setField("eventImageView", eventImageView); setField("Attendees", Attendees);
        setField("profileStack", profileStack); setField("profileButton", profileButton);
        setField("profileCircle", profileCircle);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = EventDetailsController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void testInitializeStateForNonParticipant() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            assertEquals("Join Event", joinEventButton.getText());
            assertFalse(joinEventButton.isDisabled());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testInitializeStateForParticipant() throws Exception {
        testEvent.addUser(testUser);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.refresh();
            assertTrue(joinEventButton.getText().contains("Joined"));
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testUnknownOrganizer() throws Exception {
        testEvent.setCreatorID(999);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.refresh();
            assertEquals("Unknown", organizedByLabel.getText());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testInteractionButtons() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            assertNotNull(controller); 
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }


    @Test
    void testPopulateWithNullImage() throws Exception {
        testEvent.setImageURL(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.refresh();
            assertNotNull(eventImageView.getImage()); // Should load default image
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}