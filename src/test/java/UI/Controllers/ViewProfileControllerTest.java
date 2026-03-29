package UI.Controllers;

import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import Persistence.DatabaseManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.SegmentedButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ViewProfileControllerTest {

    private ViewProfileController controller;
    private User testUser;

    // UI Components
    private Label nameLabel, locationLabel, bioLabel, createdStatistic, attendedStatistic;
    private TextField fullName, locationField;
    private TextArea bio;
    private Button editProfileButton, confirmButton, cancelButton, profilePicChanger, profileButton, backButton;
    private HBox saveCancelHBox;
    private VBox editProfileVBox, viewProfileVBox, tabsVBox;
    private FlowPane interestsFlow, eventTypesFlow, interestsEditFlow, eventTypesEditFlow;
    private Circle profilePic, profileCircle;
    private ToggleButton joinedEventsSelect, createdEventsSelect, reviewSelect;
    private SegmentedButton segmentedTabButtons;
    private ScrollPane tabsScrollPane;
    private AnchorPane tabsAnchorPane;
    private TextFlow tabsHeaderLabel;
    private Label fullNameErrorLabel, locationErrorLabel, bioErrorLabel, interestsErrorLabel, eventTypesErrorLabel;

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
        cleanDatabase();

        testUser = new User(0, "John Doe", "john@test.com", "pass", "Lisbon",
                new ArrayList<>(List.of("Tech")), new ArrayList<>(List.of("Conference")), "Bio");
        UserService.getInstance().addUserCurrent(testUser);

        controller = new ViewProfileController();
        initializeUIComponents();
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
        Field instance = clazz.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private void initializeUIComponents() throws Exception {
        nameLabel = new Label(); locationLabel = new Label(); bioLabel = new Label();
        createdStatistic = new Label(); attendedStatistic = new Label();
        fullName = new TextField(); locationField = new TextField();
        bio = new TextArea();
        editProfileButton = new Button(); confirmButton = new Button(); cancelButton = new Button();
        profilePicChanger = new Button(); profileButton = new Button(); backButton = new Button();
        saveCancelHBox = new HBox();
        editProfileVBox = new VBox(); viewProfileVBox = new VBox(); tabsVBox = new VBox();
        interestsFlow = new FlowPane(); eventTypesFlow = new FlowPane();
        interestsEditFlow = new FlowPane(); eventTypesEditFlow = new FlowPane();
        profilePic = new Circle(); profileCircle = new Circle();
        joinedEventsSelect = new ToggleButton(); createdEventsSelect = new ToggleButton(); reviewSelect = new ToggleButton();
        segmentedTabButtons = new SegmentedButton();
        tabsScrollPane = new ScrollPane();
        tabsAnchorPane = new AnchorPane();
        tabsHeaderLabel = new TextFlow();
        fullNameErrorLabel = new Label(); locationErrorLabel = new Label(); bioErrorLabel = new Label();
        interestsErrorLabel = new Label(); eventTypesErrorLabel = new Label();

        setField("nameLabel", nameLabel); setField("locationLabel", locationLabel); setField("bioLabel", bioLabel);
        setField("createdStatistic", createdStatistic); setField("attendedStatistic", attendedStatistic);
        setField("fullName", fullName); setField("locationField", locationField); setField("bio", bio);
        setField("editProfileButton", editProfileButton); setField("confirmButton", confirmButton);
        setField("cancelButton", cancelButton); setField("profilePicChanger", profilePicChanger);
        setField("profileButton", profileButton); setField("backButton", backButton);
        setField("saveCancelHBox", saveCancelHBox);
        setField("editProfileVBox", editProfileVBox); setField("viewProfileVBox", viewProfileVBox);
        setField("tabsVBox", tabsVBox);
        setField("interestsFlow", interestsFlow); setField("eventTypesFlow", eventTypesFlow);
        setField("interestsEditFlow", interestsEditFlow); setField("eventTypesEditFlow", eventTypesEditFlow);
        setField("profilePic", profilePic); setField("profileCircle", profileCircle);
        setField("joinedEventsSelect", joinedEventsSelect); setField("createdEventsSelect", createdEventsSelect);
        setField("reviewSelect", reviewSelect);
        setField("segmentedTabButtons", segmentedTabButtons);
        setField("tabsScrollPane", tabsScrollPane); setField("tabsAnchorPane", tabsAnchorPane);
        setField("tabsHeaderLabel", tabsHeaderLabel);
        setField("fullNameErrorLabel", fullNameErrorLabel); setField("locationErrorLabel", locationErrorLabel);
        setField("bioErrorLabel", bioErrorLabel); setField("interestsErrorLabel", interestsErrorLabel);
        setField("eventTypesErrorLabel", eventTypesErrorLabel);

        javafx.scene.layout.StackPane profileStack = new javafx.scene.layout.StackPane();
        setField("profileStack", profileStack);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ViewProfileController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void testRefreshProfileContent() {
        Platform.runLater(() -> {
            controller.refreshProfileContent();
            assertEquals("John Doe", nameLabel.getText());
            assertEquals("Lisbon", locationLabel.getText());
            assertEquals("Bio", bioLabel.getText());
            assertEquals("0", createdStatistic.getText());
            assertEquals("0", attendedStatistic.getText());
            assertFalse(interestsFlow.getChildren().isEmpty());
            assertFalse(eventTypesFlow.getChildren().isEmpty());
        });
    }

    @Test
    void testEditModeToggle() throws Exception {
        Platform.runLater(() -> {
            try {
                controller.refreshProfileContent();
                var method = ViewProfileController.class.getDeclaredMethod("handleEditProfile");
                method.setAccessible(true);
                method.invoke(controller);

                assertFalse(editProfileButton.isVisible());
                assertTrue(saveCancelHBox.isVisible());
                assertTrue(editProfileVBox.isVisible());
                assertFalse(viewProfileVBox.isVisible());

                assertEquals("John Doe", fullName.getText());
                assertEquals("Lisbon", locationField.getText());
                assertEquals("Bio", bio.getText());
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    void testHandleCancel() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.refreshProfileContent();
                var editMethod = ViewProfileController.class.getDeclaredMethod("handleEditProfile");
                editMethod.setAccessible(true);
                editMethod.invoke(controller);

                fullName.setText("Changed Name");

                var cancelMethod = ViewProfileController.class.getDeclaredMethod("handleCancel");
                cancelMethod.setAccessible(true);
                cancelMethod.invoke(controller);

                assertTrue(editProfileButton.isVisible());
                assertFalse(saveCancelHBox.isVisible());
                assertEquals("John Doe", fullName.getText());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testPopulateCreatedEvents() throws Exception {
        Model.Entities.Event event = new Model.Entities.Event();
        event.setTitle("My Event");
        event.setCreatorID(1);
        event.setDate(java.time.LocalDate.now());
        event.setStartTime(java.time.LocalTime.of(10, 0));
        event.setEndTime(java.time.LocalTime.of(12, 0));
        EventService.getInstance().addEvent(event);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.refreshProfileContent(); // Needs refresh to fetch new events
                var method = ViewProfileController.class.getDeclaredMethod("populateCreatedEvents");
                method.setAccessible(true);
                method.invoke(controller);
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testCreateCard() throws Exception {
        Model.Entities.Event event = new Model.Entities.Event();
        event.setTitle("Test Card Event");
        event.setDate(java.time.LocalDate.of(2025, 12, 25));
        event.setStartTime(java.time.LocalTime.of(14, 0));
        event.setEndTime(java.time.LocalTime.of(16, 0));
        event.setLocation("Home");
        event.setUsers(new ArrayList<>());
        event.setMaxAttendees(50);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                HBox cardJoined = controller.createCard(event, "joined");
                assertNotNull(cardJoined);
                assertFalse(cardJoined.getChildren().isEmpty());

                VBox leftBox = (VBox) cardJoined.getChildren().get(0);
                boolean titleFound = false;
                for(javafx.scene.Node n : leftBox.getChildren()){
                    if(n instanceof Label && ((Label)n).getText().equals("Test Card Event")) {
                        titleFound = true; break;
                    }
                }
                assertTrue(titleFound, "Card should contain event title");

                HBox cardCreated = controller.createCard(event, "created");
                assertNotNull(cardCreated);
            } catch (Exception e) {
                fail("Card creation failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}