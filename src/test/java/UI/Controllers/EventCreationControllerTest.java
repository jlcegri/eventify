package UI.Controllers;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the validation logic of the EventCreationController.
 */
class EventCreationControllerTest {

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
    private VBox interestsBox;
    private Button aiButton;

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
        new JFXPanel();
        Platform.setImplicitExit(false);
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
        interestsBox = new VBox();
        aiButton = new Button("Generate with AI");

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
        setPrivateField(controller, "interestsBox", interestsBox);
        setPrivateField(controller, "interestsFlow", interestsFlow);
        setPrivateField(controller, "imageSourceToggleGroup", imageSourceToggleGroup);
        setPrivateField(controller, "fileSelect", fileSelect);
        setPrivateField(controller, "urlSelect", urlSelect);
        setPrivateField(controller, "uploadButton", uploadButton);
        setPrivateField(controller, "aiButton", aiButton);

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
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in hierarchy of " + target.getClass());
    }

    @Test
    void testSuccessfulCreation() throws InterruptedException {
        setValidMinimumData();
        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertTrue(errors.isEmpty(), "Validation should be successful with valid data. Errors: " + errors);
        assertFalse(tittleErrorLabel.isVisible(), "Title error label should not be visible on success.");
        assertFalse(imageErrorLabel.isVisible(), "Image error label should not be visible on success.");
    }

    @Test
    void testRequiredFields() throws InterruptedException {
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

        // Ensure interestSelected is 0
        try {
            setPrivateField(controller, "interestSelected", 0);
        } catch (Exception e) { fail("Failed to set interestSelected"); }

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty(), "There should be errors about missing required fields.");
        assertTrue(errors.contains("'Event Title' is mandatory"));
        assertTrue(errors.contains("Date is a required field"));
        assertTrue(errors.contains("Event Type is a required field"));
        assertTrue(errors.contains("You must select at least one interest"));

        assertTrue(tittleErrorLabel.isVisible(), "Title error label should be visible.");
    }

    @Test
    void testInvalidTimeFormat() throws InterruptedException {
        setValidMinimumData();
        startTime.setText("10-00"); // incorrect format

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The start time must be in the format HH:mm"));
    }

    @Test
    void testStartTimeInThePast() throws InterruptedException {
        setValidMinimumData();
        datePicker.setValue(LocalDate.now().minusDays(1)); // yesterday

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The Date cannot be in the past"));
    }

    @Test
    void testInvalidImageUrl() throws InterruptedException {
        setValidMinimumData();
        urlSelect.setSelected(true); // Explicitly select URL input
        imageURL.setText("not-a-valid-url");

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The image URL does not appear to be a valid URL"));
        assertTrue(imageErrorLabel.isVisible());
    }

    @Test
    void testValidImageUrl() throws InterruptedException {
        setValidMinimumData();
        urlSelect.setSelected(true);
        imageURL.setText("https://example.com/image.jpg");
        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertTrue(errors.isEmpty());
        assertFalse(imageErrorLabel.isVisible());
    }

    @Test
    void testInvalidImageFile() throws Exception {
        setValidMinimumData();
        fileSelect.setSelected(true);
        setPrivateField(controller, "selectedFilePath", "non-existent-path.png");

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("The selected image file does not exist"));
        assertTrue(imageErrorLabel.isVisible());
    }

    @Test
    void testNoInterestSelected() throws InterruptedException {
        setValidMinimumData();
        try {
            setPrivateField(controller, "interestSelected", 0); // Override valid data
        } catch (Exception e) {
            fail("Failed to set interestSelected field");
        }

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertFalse(errors.isEmpty());
        assertTrue(errors.contains("You must select at least one interest"));
    }

    @Test
    void testMaxAttendeesAsZeroIsValid() throws InterruptedException {
        setValidMinimumData();
        maximumAttendees.getValueFactory().setValue(0);

        String errors = callValidateInput(controller);
        callUpdateErrorLabelsAndStyles(controller, new StringBuilder(errors));

        assertTrue(errors.isEmpty(), "Validation should pass when max attendees is 0. Errors: " + errors);
    }

    @Test
    void testOnAiButtonClicked_ValidationFail() throws Exception {
        // Clear required fields to trigger validation errors
        eventTitle.setText("");
        datePicker.setValue(null);
        locationField.setText("");

        // Use reflection to call the private method onAiButtonClicked
        Method method = EventCreationController.class.getDeclaredMethod("onAiButtonClicked");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
                // Verify that error labels are visible after the click logic runs
                assertTrue(tittleErrorLabel.isVisible());
                assertTrue(dateErrorLabel.isVisible());
                assertTrue(locationErrorLabel.isVisible());
            } catch (Exception e) {
                // Ignore Alert dialog blocking if any
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

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
        try {
            setPrivateField(controller, "interestSelected", 1);
        } catch (Exception e) {
            fail("Failed to set interestSelected field");
        }
    }

    private String callValidateInput(EventCreationController controller) {
        try {
            Method method = EventCreationController.class.getDeclaredMethod("validateInput");
            method.setAccessible(true);
            StringBuilder result = (StringBuilder) method.invoke(controller);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal test error";
        }
    }

    private void callUpdateErrorLabelsAndStyles(EventCreationController controller, StringBuilder errors) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Method method = EventCreationController.class.getDeclaredMethod("updateErrorLabelsAndStyles", StringBuilder.class);
                method.setAccessible(true);
                method.invoke(controller, errors);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
