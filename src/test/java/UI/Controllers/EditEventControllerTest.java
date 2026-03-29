package UI.Controllers;

import Model.Entities.Event;
import Model.Services.EventService;
import Persistence.DatabaseManager;
import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class EditEventControllerTest {

    private EditEventController controller;
    private Event testEvent;
    private NavigationService mockNav;

    // UI Components
    private TextField eventTitle, startTime, endTime, locationField, city, imageURL;
    private DatePicker datePicker;
    private ComboBox<String> eventType;
    private TextArea eventDescription;
    private Spinner<Integer> maximumAttendees;
    private FlowPane interestsFlow;
    private Button saveButton, deleteButton, uploadButton;
    private SegmentedButton imageSourceToggleGroup;
    private ToggleButton fileSelect, urlSelect;
    private Label interestsLabel;

    // Labels
    private Label tittleErrorLabel, dateErrorLabel, timeErrorLabel, locationErrorLabel, cityErrorLabel,
            eventTypeErrorLabel, interestsErrorLabel, descriptionErrorLabel, timeWarnLabel, imageErrorLabel;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        Field instance = EventService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
        
        cleanDatabase();
        createTestUser();

        testEvent = new Event(0, 1, "Original Title", "Desc", LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0), "Loc", "City", "Music",
                Arrays.asList("Music"), 50, "http://img.com", 10.0);
        EventService.getInstance().addEvent(testEvent);

        // Mock NavigationService BEFORE creating controller because controller constructor uses it
        mockNav = Mockito.mock(NavigationService.class);
        Field navInstance = NavigationService.class.getDeclaredField("instance");
        navInstance.setAccessible(true);
        navInstance.set(null, mockNav);
        
        // Ensure mockNav returns an event when getData is called
        Mockito.when(mockNav.getData()).thenReturn(testEvent);

        controller = new EditEventController();
        
        initializeUIComponents();
        injectFields();
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
    
    private void createTestUser() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (1, 'Test', 'User', 'test@test.com', 'pass')");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUIComponents() {
        eventTitle = new TextField(); datePicker = new DatePicker();
        startTime = new TextField(); endTime = new TextField();
        locationField = new TextField(); city = new TextField();
        eventType = new ComboBox<>(); eventDescription = new TextArea();
        maximumAttendees = new Spinner<>(); imageURL = new TextField();
        interestsFlow = new FlowPane();
        saveButton = new Button(); deleteButton = new Button(); uploadButton = new Button();
        fileSelect = new ToggleButton(); urlSelect = new ToggleButton();
        imageSourceToggleGroup = new SegmentedButton(fileSelect, urlSelect);

        tittleErrorLabel = new Label(); dateErrorLabel = new Label(); timeErrorLabel = new Label();
        locationErrorLabel = new Label(); cityErrorLabel = new Label(); eventTypeErrorLabel = new Label();
        interestsErrorLabel = new Label(); descriptionErrorLabel = new Label(); timeWarnLabel = new Label();
        imageErrorLabel = new Label();
        interestsLabel = new Label();

        maximumAttendees.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));

        ToggleButton tb = new ToggleButton();
        tb.setGraphic(new Label("Music"));
        interestsFlow.getChildren().add(tb);
    }

    private void injectFields() throws Exception {
        setField("eventTitle", eventTitle); setField("datePicker", datePicker);
        setField("startTime", startTime); setField("endTime", endTime);
        setField("locationField", locationField); setField("city", city);
        setField("eventType", eventType); setField("eventDescription", eventDescription);
        setField("maximumAttendees", maximumAttendees); setField("imageURL", imageURL);
        setField("interestsFlow", interestsFlow); setField("saveButton", saveButton);
        setField("deleteButton", deleteButton); setField("uploadButton", uploadButton);
        setField("imageSourceToggleGroup", imageSourceToggleGroup);
        setField("fileSelect", fileSelect); setField("urlSelect", urlSelect);
        setField("interestsBox", new VBox());

        setField("tittleErrorLabel", tittleErrorLabel); setField("dateErrorLabel", dateErrorLabel);
        setField("timeErrorLabel", timeErrorLabel); setField("locationErrorLabel", locationErrorLabel);
        setField("cityErrorLabel", cityErrorLabel); setField("eventTypeErrorLabel", eventTypeErrorLabel);
        setField("interestsErrorLabel", interestsErrorLabel); setField("descriptionErrorLabel", descriptionErrorLabel);
        setField("timeWarnLabel", timeWarnLabel); setField("imageErrorLabel", imageErrorLabel);

        setField("interestsLabel", interestsLabel);
    }

    private void setField(String name, Object value) throws Exception {
        Class<?> clazz = controller.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(controller, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    @Test
    void testPopulateForm() throws Exception {
        Method method = EditEventController.class.getDeclaredMethod("populateForm", Event.class);
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller, testEvent);
                assertEquals("Original Title", eventTitle.getText());
                assertEquals("City", city.getText());
                assertEquals("10:00", startTime.getText());
                assertEquals("Music", eventType.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}