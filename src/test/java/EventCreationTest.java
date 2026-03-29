import UI.Controllers.EventCreationController;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.SegmentedButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the validation logic of the EventCreationController.
 */
public class EventCreationTest {

    private EventCreationController controller;

    private TextField eventTitle;
    private DatePicker datePicker;
    private TextField startTime;
    private TextField endTime;
    private TextField locationField;
    private TextField city;
    private ComboBox<String> eventType;
    private TextArea eventDescription;
    private Spinner<Integer> maximumAttendees;
    private TextField imageURL;
    private Button createButton;
    private FlowPane interestsFlow;
    private SegmentedButton imageSourceToggleGroup;
    private ToggleButton fileSelect;
    private ToggleButton urlSelect;
    private Button uploadButton;


    // Labels for error checking
    private Label tittleErrorLabel;
    private Label dateErrorLabel;
    private Label timeErrorLabel;
    private Label locationErrorLabel;
    private Label cityErrorLabel;
    private Label eventTypeErrorLabel;
    private Label interestsErrorLabel;
    private Label descriptionErrorLabel;
    private Label timeWarnLabel;
    private Label imageErrorLabel;


    @BeforeAll
    static void initJFX() {
        new JFXPanel(); // Initializes the JavaFX environment.
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new EventCreationController();

        // Initialize UI components
        eventTitle = new TextField();
        datePicker = new DatePicker();
        startTime = new TextField();
        endTime = new TextField();
        locationField = new TextField();
        city = new TextField();
        eventType = new ComboBox<>();
        eventDescription = new TextArea();
        maximumAttendees = new Spinner<>();
        imageURL = new TextField();
        createButton = new Button("Create");
        interestsFlow = new FlowPane();
        fileSelect = new ToggleButton("File");
        urlSelect = new ToggleButton("URL");
        imageSourceToggleGroup = new SegmentedButton(fileSelect, urlSelect);
        uploadButton = new Button("Upload");

        // Initialize Labels
        tittleErrorLabel = new Label();
        dateErrorLabel = new Label();
        timeErrorLabel = new Label();
        locationErrorLabel = new Label();
        cityErrorLabel = new Label();
        eventTypeErrorLabel = new Label();
        interestsErrorLabel = new Label();
        descriptionErrorLabel = new Label();
        timeWarnLabel = new Label();
        imageErrorLabel = new Label();

        // Set private fields in the controller using reflection
        setPrivateField(controller, "eventTitle", eventTitle);
        setPrivateField(controller, "datePicker", datePicker);
        setPrivateField(controller, "startTime", startTime);
        setPrivateField(controller, "endTime", endTime);
        setPrivateField(controller, "locationField", locationField);
        setPrivateField(controller, "city", city);
        setPrivateField(controller, "eventType", eventType);
        setPrivateField(controller, "eventDescription", eventDescription);
        setPrivateField(controller, "maximumAttendees", maximumAttendees);
        setPrivateField(controller, "imageURL", imageURL);
        setPrivateField(controller, "createButton", createButton);
        setPrivateField(controller, "interestsBox", new javafx.scene.layout.VBox());
        setPrivateField(controller, "interestsFlow", interestsFlow);
        setPrivateField(controller, "imageSourceToggleGroup", imageSourceToggleGroup);
        setPrivateField(controller, "fileSelect", fileSelect);
        setPrivateField(controller, "urlSelect", urlSelect);
        setPrivateField(controller, "uploadButton", uploadButton);


        // Set label fields
        setPrivateField(controller, "tittleErrorLabel", tittleErrorLabel);
        setPrivateField(controller, "dateErrorLabel", dateErrorLabel);
        setPrivateField(controller, "timeErrorLabel", timeErrorLabel);
        setPrivateField(controller, "locationErrorLabel", locationErrorLabel);
        setPrivateField(controller, "cityErrorLabel", cityErrorLabel);
        setPrivateField(controller, "eventTypeErrorLabel", eventTypeErrorLabel);
        setPrivateField(controller, "interestsErrorLabel", interestsErrorLabel);
        setPrivateField(controller, "descriptionErrorLabel", descriptionErrorLabel);
        setPrivateField(controller, "timeWarnLabel", timeWarnLabel);
        setPrivateField(controller, "imageErrorLabel", imageErrorLabel);

        controller.initialize();
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSuccessfulCreation() {
        setValidMinimumData();
        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertTrue(errors.isEmpty(), "Validation should be successful with valid data. Errors: " + errors);
        assertFalse(tittleErrorLabel.isVisible(), "Title error label should not be visible on success.");
        assertFalse(dateErrorLabel.isVisible(), "Date error label should not be visible on success.");
        assertFalse(timeErrorLabel.isVisible(), "Time error label should not be visible on success.");
        assertFalse(locationErrorLabel.isVisible(), "Location error label should not be visible on success.");
    }

    @Test
    void testRequiredFields() {
        // Set all required fields to empty/null
        eventTitle.setText("");
        datePicker.setValue(null);
        datePicker.getEditor().setText("");
        startTime.setText("");
        endTime.setText("");
        locationField.setText(" ");
        city.setText("");
        eventType.setValue(null);
        eventDescription.setText("");
        // interestSelected is 0 by default

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty(), "There should be errors about missing required fields.");
        assertTrue(errors.contains("'Event Title' is mandatory"));
        assertTrue(errors.contains("Date is a required field"));
        assertTrue(errors.contains("Event Type is a required field"));
        assertTrue(errors.contains("You must select at least one interest"));

        // Check label visibility
        assertTrue(tittleErrorLabel.isVisible(), "Title error label should be visible.");
        assertTrue(dateErrorLabel.isVisible(), "Date error label should be visible.");
        assertTrue(timeErrorLabel.isVisible(), "Time error label should be visible.");
        assertTrue(locationErrorLabel.isVisible(), "Location error label should be visible.");
        assertTrue(cityErrorLabel.isVisible(), "City error label should be visible.");
        assertTrue(eventTypeErrorLabel.isVisible(), "Event Type error label should be visible.");
        assertTrue(interestsErrorLabel.isVisible(), "Interests error label should be visible.");
        assertTrue(descriptionErrorLabel.isVisible(), "Description error label should be visible.");


        long mandatoryErrorCount = errors.lines()
                .filter(line -> line.contains("mandatory") || line.contains("required") || line.contains("must select"))
                .count();
        assertEquals(9, mandatoryErrorCount, "Nine errors were expected in required fields.");
    }

    @Test
    void testInvalidTimeFormat() {
        setValidMinimumData();
        startTime.setText("10-00"); // incorrect format

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("must be in the format HH:mm"), "Error message should contain format instruction. Actual: " + errors);
        assertTrue(timeErrorLabel.isVisible(), "Time error label should be visible for invalid format.");
    }

    @Test
    void testStartTimeInThePast() {
        setValidMinimumData();
        datePicker.setValue(LocalDate.now().minusDays(1)); // yesterday

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The Date cannot be in the past"));
        assertTrue(dateErrorLabel.isVisible(), "Date error label should be visible for past date.");
    }

    @Test
    void testInvalidImageUrl() {
        setValidMinimumData();
        imageURL.setText("not-a-valid-url");

        String errors = callValidateInput(controller);
        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The image URL does not appear to be a valid URL"));
    }

    @Test
    void testValidImageUrl() {
        setValidMinimumData();
        imageURL.setText("https://example.com/image.jpg");
        String errors = callValidateInput(controller);
        assertTrue(errors.isEmpty(), "The HTTPS URL should be valid. Errors: " + errors);
    }

    // --- New and Updated Tests ---

    @Test
    void testNoInterestSelected() {
        setValidMinimumData();
        try {
            setPrivateField(controller, "interestSelected", 0); // Override valid data
        } catch (Exception e) {
            fail("Failed to set interestSelected field");
        }

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty(), "Validation should fail when no interest is selected.");
        assertTrue(errors.contains("You must select at least one interest"));
        assertTrue(interestsErrorLabel.isVisible(), "Interests error label should be visible.");
    }

    @Test
    void testMaxAttendeesAsZeroIsValid() {
        setValidMinimumData();
        maximumAttendees.getValueFactory().setValue(0);

        String errors = callValidateInput(controller);

        assertTrue(errors.isEmpty(), "Validation should pass when max attendees is 0. Errors: " + errors);
    }


    /**
     * Fill in all fields with valid minimum future data.
     */
    private void setValidMinimumData() {
        eventTitle.setText("Test event");
        datePicker.setValue(LocalDate.now().plusDays(1));
        startTime.setText("10:00");
        endTime.setText("12:00");
        locationField.setText("A place");
        city.setText("A city");
        eventType.setValue("Music");
        eventDescription.setText("A description.");
        maximumAttendees.getValueFactory().setValue(10);
        imageURL.setText("");
        // Simulate selecting an interest to pass validation by default
        try {
            setPrivateField(controller, "interestSelected", 1);
        } catch (Exception e) {
            fail("Failed to set interestSelected field");
        }
    }

    /**
     * Call the private method validateInput() using Reflection to get the error message.
     */
    private String callValidateInput(EventCreationController controller) {
        try {
            Method method = EventCreationController.class.getDeclaredMethod("validateInput");
            method.setAccessible(true);
            StringBuilder result = (StringBuilder) method.invoke(controller);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception when accessing or invoking validateInput: " + e.getMessage());
            return "Internal test error";
        }
    }

    /**
     * Call the private method updateErrorLabelsAndStyles() using Reflection.
     */
    private void callUpdateErrorLabelsAndStyles(EventCreationController controller, StringBuilder errors) {
        try {
            Method method = EventCreationController.class.getDeclaredMethod("updateErrorLabelsAndStyles", StringBuilder.class);
            method.setAccessible(true);
            method.invoke(controller, errors);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception when accessing or invoking updateErrorLabelsAndStyles: " + e.getMessage());
        }
    }
}
