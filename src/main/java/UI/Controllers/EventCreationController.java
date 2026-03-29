package UI.Controllers;

import Model.Entities.Event;
import Model.Services.EventService;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCreationController {

    @FXML protected TextField eventTitle;
    @FXML protected DatePicker datePicker;
    @FXML protected TextField startTime;
    @FXML protected TextField endTime;
    @FXML protected TextField locationField;
    @FXML protected TextField city;
    @FXML protected ComboBox<String> eventType;
    @FXML protected TextArea eventDescription;
    @FXML protected Spinner<Integer> maximumAttendees;
    @FXML protected FlowPane interestsFlow;
    @FXML protected Label interestsLabel;
    @FXML protected TextField imageURL;
    @FXML protected VBox interestsBox;
    @FXML protected StackPane fileChooserArea;
    @FXML protected ToggleButton fileSelect;
    @FXML protected ToggleButton urlSelect;
    @FXML protected Button uploadButton;
    @FXML protected SegmentedButton imageSourceToggleGroup;

    @FXML protected Button backButton;
    @FXML protected Button createButton; // Renamed to saveButton conceptually
    @FXML protected Button cancelButton;
    @FXML protected Button aiButton;

    @FXML protected Label tittleErrorLabel;
    @FXML protected Label dateErrorLabel;
    @FXML protected Label timeErrorLabel;
    @FXML protected Label timeWarnLabel;
    @FXML protected Label locationErrorLabel;
    @FXML protected Label cityErrorLabel;
    @FXML protected Label eventTypeErrorLabel;
    @FXML protected Label interestsErrorLabel;
    @FXML protected Label descriptionErrorLabel;
    @FXML protected Label imageErrorLabel;

    private Event eventToEdit; // Field to hold the event being edited

    // default values
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DEFAULT_MAX_ATTENDEES = 50;
    private static final String DEFAULT_IMAGE_URL = "";
    private int interestSelected = 0;
    List<String> selectedInterests = new ArrayList<>();

    private final List<String> interests = List.of("Music", "Sports", "Technology", "Art", "Food & Drink", "Business", "Health & Wellness", "Travel", "Photography",
            "Gaming", "Books", "Fashion", "Dance", "Comedy", "Film", "Fitness", "Outdoor Adventures");

    private final List<String> events = List.of("Concerts", "Workshops", "Networking", "Parties", "Conferences", "Sports Events", "Art Exhibitions", "Food Festivals",
            "Markets", "Meetups", "Classes", "Volunteer Work");

    String selectedFilePath;


    /**
     * @brief Initialize method of the controller.
     */
    @FXML
    public void initialize() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 1);
        maximumAttendees.setValueFactory(valueFactory);

        if (interestsFlow.getChildren().isEmpty()) {
            for (String interest : interests) {
                Label interestLabel = new Label(interest);
                interestLabel.getStyleClass().add("label-toggle-buttons-edit-profile");
                ToggleButton interestButton = new ToggleButton();
                interestButton.setGraphic(interestLabel);
                interestButton.getStyleClass().add("toggle-buttons-edit-profile");
                interestButton.setOnAction(this::handleInteractionToggle);

                interestsFlow.getChildren().add(interestButton);
            }
        }

        // Image source selection logic
        imageSourceToggleGroup.getToggleGroup().selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                fileSelect.setSelected(true);
                return;
            }

            boolean isFileSelected = newToggle == fileSelect;

            uploadButton.setVisible(isFileSelected);
            uploadButton.setManaged(isFileSelected);

            imageURL.setVisible(!isFileSelected);
            imageURL.setManaged(!isFileSelected);
            imageURL.setDisable(isFileSelected);
        });

        // optional: configure the eventType ChoiceBox with sample data
        eventType.getItems().addAll(events);

        // Disable time fields initially
        startTime.setDisable(true);
        endTime.setDisable(true);

        // Add listeners for real-time validation
        eventTitle.textProperty().addListener((obs, oldVal, newVal) -> validateEventTitle());
        eventTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateEventTitle();});
        locationField.textProperty().addListener((obs, oldVal, newVal) -> validateLocation());
        locationField.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateLocation();});
        city.textProperty().addListener((obs, oldVal, newVal) -> validateCity());
        city.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateCity();});
        eventDescription.textProperty().addListener((obs, oldVal, newVal) -> validateDescription());
        eventDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateDescription();});
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDate();
            boolean dateSelected = !dateErrorLabel.isVisible();
            startTime.setDisable(!dateSelected);
            endTime.setDisable(!dateSelected);

            if (!dateSelected) {
                startTime.clear();
                endTime.clear();
            }

            timeWarnLabel.setDisable(dateSelected);
            timeWarnLabel.setVisible(!dateSelected);
        });
        datePicker.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) { validateDate(); }});
        startTime.textProperty().addListener((obs, oldVal, newVal) -> validateTime());
        startTime.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateTime();});
        endTime.textProperty().addListener((obs, oldVal, newVal) -> validateTime());
        endTime.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateTime();});
        eventType.valueProperty().addListener((obs, oldVal, newVal) -> validateEventType());
        eventType.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateEventType();});
        imageURL.textProperty().addListener((obs, oldVal, newVal) -> validateImage());
        imageURL.focusedProperty().addListener((obs, oldVal, newVal) -> {if(oldVal) validateImage();});
        datePicker.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> datePicker.show());
    }

    @FXML
    private void handleCreateEvent() {
        if(!onCreateEventClicked()) return;

        String title = eventTitle.getText();
        String description = eventDescription.getText();
        LocalDate eventDate = datePicker.getValue();
        LocalTime parsedStartTime = LocalTime.parse(startTime.getText());
        LocalTime parsedEndTime = LocalTime.parse(endTime.getText());



        String loc = locationField.getText();
        String cty = city.getText();
        String cat = eventType.getValue();
        int maxAtt = maximumAttendees.getValue();
        String finalImageUrl = (urlSelect.isSelected() && imageURL.getText() != null && !imageURL.getText().trim().isEmpty()) ? imageURL.getText().trim() : selectedFilePath;

        Event newEvent = new Event(
                0,
                UserService.getInstance().getCurrentUser().getId(),
                title,
                description,
                eventDate,
                parsedStartTime,
                parsedEndTime,
                loc,
                cty,
                cat,
                selectedInterests,
                maxAtt,
                selectedFilePath,
                0.0
        );

        EventService.getInstance().addEvent(newEvent);
        NavigationService.getInstance().navigateTo("DiscoverEvents", true);
    }

    @FXML
    private void setupFileChooser(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedFilePath = selectedFile.getAbsolutePath();
            // Optionally, update the button text to show the selected file name
            uploadButton.setText(selectedFile.getName());
        }
        validateImage();
    }


    /**
     * Handle the click on the 'Create Event' button and perform robust validation.
     */
    @FXML
    private boolean onCreateEventClicked() {
        StringBuilder validationErrors = validateInput();

        updateErrorLabelsAndStyles(validationErrors);

        if (!validationErrors.isEmpty()) {
            showAlert("Validation error", "Please, correct the following errors:", validationErrors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        String finalImageUrl = (imageURL != null && imageURL.getText() != null && !imageURL.getText().trim().isEmpty()) ? imageURL.getText().trim() : DEFAULT_IMAGE_URL;
        int finalMaxAttendees = maximumAttendees.getValue();

        showAlert("Success", "Event created", "The event '" + eventTitle.getText() + "' has been created with success.", Alert.AlertType.INFORMATION);

        return true;
    }

    void updateErrorLabelsAndStyles(StringBuilder errors) {
        String errorText = errors.toString();

        updateFieldState(errorText, "Event Title", eventTitle, tittleErrorLabel);
        updateFieldState(errorText, "Date", datePicker, dateErrorLabel);
        updateFieldState(errorText, "Location", locationField, locationErrorLabel);
        updateFieldState(errorText, "City", city, cityErrorLabel);
        updateFieldState(errorText, "Event Type", eventType, eventTypeErrorLabel);
        updateFieldState(errorText, "Description", eventDescription, descriptionErrorLabel);

        // Explicitly handle image error label
        boolean imageError = errorText.toLowerCase().contains("image");
        imageErrorLabel.setVisible(imageError);
        imageErrorLabel.setText(getErrorMessage(errorText, "Image"));
        updateNodeStyle(imageURL, imageError);
        updateNodeStyle(uploadButton, imageError);


        // Special case for Time (two fields, one label)
        boolean timeError = errorText.toLowerCase().contains("hour") || errorText.toLowerCase().contains("time");
        timeErrorLabel.setVisible(timeError);
        updateNodeStyle(startTime, timeError);
        updateNodeStyle(endTime, timeError);
        if (timeError) {
            String timeErrorMessage = getErrorMessage(errorText, "hour");
            if (timeErrorMessage.isEmpty()) {
                timeErrorMessage = getErrorMessage(errorText, "time");
            }
            timeErrorLabel.setText(timeErrorMessage);
        } else {
            timeErrorLabel.setText("");
        }


        // Special case for Interests (parent node)
        boolean interestsError = errorText.toLowerCase().contains("interest");
        interestsErrorLabel.setVisible(interestsError);
        interestsErrorLabel.setText(getErrorMessage(errorText, "interest"));
        if (interestsError) {
            interestsBox.setStyle("-fx-border-color: red; -fx-border-radius: 5; -fx-border-width: 0 0.2 0.2 0.2;");
        } else {
            interestsBox.setStyle("-fx-border-color: #636a75; -fx-border-radius: 5; -fx-border-width: 0 0.2 0.2 0.2;");
        }
    }

    private void updateFieldState(String errorText, String fieldName, Node fieldNode, Label errorLabel) {
        boolean hasError = errorText.contains(fieldName);
        errorLabel.setVisible(hasError);
        errorLabel.setText(getErrorMessage(errorText, fieldName));
        updateNodeStyle(fieldNode, hasError);
    }

    private void updateNodeStyle(Node node, boolean isInvalid) {
        String invalidClass = "my-double-border-invalid";
        if (isInvalid) {
            if (!node.getStyleClass().contains(invalidClass)) {
                node.getStyleClass().add(invalidClass);
            }
        } else {
            node.getStyleClass().remove(invalidClass);
        }
    }

    private String getErrorMessage(String errorText, String fieldName) {
        final String lowerField = fieldName == null ? "" : fieldName.toLowerCase();
        return Arrays.stream(errorText.split("\n"))
                .filter(line -> line.toLowerCase().contains(lowerField))
                .findFirst()
                .map(s -> s.replace("*", "").trim())
                .orElse("");
    }

    /**
     * Realiza todas las validaciones del lado del servidor (controlador).
     * @return Un StringBuilder que contiene todos los mensajes de error.
     */
    StringBuilder validateInput() {
        StringBuilder errors = new StringBuilder();

        validateTextFields(errors);
        validateDate(errors);
        validateTime(errors);
        validateEventType(errors);
        validateAttendees(errors);
        validateImage(errors);
        validateInterests(errors);

        return errors;
    }

    private void validateTextFields(StringBuilder errors) {
        appendIfEmpty(errors, eventTitle.getText(), "Event Title");
        appendIfEmpty(errors, locationField.getText(), "Location");
        appendIfEmpty(errors, city.getText(), "City");
        appendIfEmpty(errors, eventDescription.getText(), "Description");
    }

    private void validateEventTitle() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, eventTitle.getText(), "Event Title");
        updateFieldState(errors.toString(), "Event Title", eventTitle, tittleErrorLabel);
    }

    private void validateLocation() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, locationField.getText(), "Location");
        updateFieldState(errors.toString(), "Location", locationField, locationErrorLabel);
    }

    private void validateCity() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, city.getText(), "City");
        updateFieldState(errors.toString(), "City", city, cityErrorLabel);
    }

    private void validateDescription() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, eventDescription.getText(), "Description");
        updateFieldState(errors.toString(), "Description", eventDescription, descriptionErrorLabel);
    }

    private void validateDate(StringBuilder errors) {
        if (datePicker.getValue() == null) {
            if (datePicker.getEditor().getText().isEmpty()) {
                errors.append("* Date is a required field.\n");
            } else {
                errors.append("* Invalid date format. Please use the format yyyy-MM-dd.\n");
            }
        } else { // Value is not null, so it's a valid date. Now check if it's in the past.
            if (datePicker.getValue().isBefore(LocalDate.now())) {
                errors.append("* The Date cannot be in the past.\n");
            }
        }
    }

    private void validateDate() {
        StringBuilder errors = new StringBuilder();
        validateDate(errors);
        updateFieldState(errors.toString(), "Date", datePicker, dateErrorLabel);
    }

    private void validateTime(StringBuilder errors) {
        // This method is now called for final validation.
        // It should always check if the time fields are empty.
        appendIfEmpty(errors, this.startTime.getText(), "Start hour");
        appendIfEmpty(errors, this.endTime.getText(), "End hour");

        if (datePicker.getValue() == null) {
            // If no date is selected, we can't perform further time validation (e.g., checking if it's in the past).
            // The empty checks above are sufficient for the final validation click.
            return;
        }

        // The rest of the validation logic remains the same.
        LocalDate eventDate = datePicker.getValue();
        LocalTime parsedStartTime = null;
        LocalTime parsedEndTime = null;

        try {
            parsedStartTime = LocalTime.parse(this.startTime.getText().trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.append("* The start time must be in the format HH:mm (e.g., 09:30).\n");
        }

        try {
            parsedEndTime = LocalTime.parse(this.endTime.getText().trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.append("* The end time must be in the format HH:mm (e.g., 17:00).\n");
        }

        if (parsedStartTime != null && parsedEndTime != null) {
            LocalDateTime startDateTime = eventDate.atTime(parsedStartTime);
            LocalDateTime now = LocalDateTime.now();

            if (startDateTime.isBefore(now)) {
                errors.append("* The start time cannot be in the past.\n");
            }
            if (parsedEndTime.isBefore(parsedStartTime)) {
                errors.append("* The end time cannot be before the start time.\n");
            }
        }
    }

    private void validateTime() {
        // This is for real-time validation. If no date is selected, do nothing and clear errors.
        if (datePicker.getValue() == null) {
            timeErrorLabel.setVisible(false);
            updateNodeStyle(this.startTime, false);
            updateNodeStyle(this.endTime, false);
            return;
        }

        // If a date is selected, proceed with real-time validation.
        StringBuilder errors = new StringBuilder();
        validateTime(errors);
        String errorText = errors.toString();

        boolean timeError = errorText.contains("hour") || errorText.contains("time");
        timeErrorLabel.setVisible(timeError);
        updateNodeStyle(this.startTime, timeError);
        updateNodeStyle(this.endTime, timeError);
        if (timeError) {
            String timeErrorMessage = getErrorMessage(errorText, "hour");
            if (timeErrorMessage.isEmpty()) {
                timeErrorMessage = getErrorMessage(errorText, "time");
            }
            timeErrorLabel.setText(timeErrorMessage);
        } else {
            timeErrorLabel.setText("");
        }
    }


    private void validateEventType(StringBuilder errors) {
        if (eventType.getValue() == null || eventType.getValue().isEmpty()) {
            errors.append("* Event Type is a required field.\n");
        }
    }

    private void validateEventType() {
        StringBuilder errors = new StringBuilder();
        validateEventType(errors);
        updateFieldState(errors.toString(), "Event Type", eventType, eventTypeErrorLabel);
    }

    private void validateAttendees(StringBuilder errors) {
        // A value of 0 is now considered valid for unlimited attendees.
        // The spinner's value factory prevents values below 0.
    }

    private void validateImageUrl(StringBuilder errors) {
        String imageUrlText = (imageURL != null && imageURL.getText() != null) ? imageURL.getText().trim() : "";
        if (!imageUrlText.isEmpty()) {
            if (!isValidUrl(imageUrlText)) {
                errors.append("* The image URL does not appear to be a valid URL.\n");
            } else {
                try {
                    Image image = new Image(imageUrlText, true); // true for background loading
                    if (image.isError()) {
                        errors.append("* The URL does not point to a valid image.\n");
                    }
                } catch (Exception e) {
                    errors.append("* Failed to load image from URL.\n");
                }
            }
        }
    }

    private void validateImageFile(StringBuilder errors) {
        if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
            File file = new File(selectedFilePath);
            if (!file.exists()) {
                errors.append("* The selected image file does not exist.\n");
                return;
            }
            try {
                Image image = new Image(file.toURI().toString());
                if (image.isError()) {
                    errors.append("* The selected file is not a valid image.\n");
                }
            } catch (Exception e) {
                errors.append("* Failed to load image from file.\n");
            }
        }
    }

    private void validateImage() {
        StringBuilder errors = new StringBuilder();
        validateImage(errors);
        // Explicitly handle image error label for real-time validation
        boolean imageError = errors.toString().toLowerCase().contains("image");
        imageErrorLabel.setVisible(imageError);
        imageErrorLabel.setText(getErrorMessage(errors.toString(), "Image"));
        updateNodeStyle(imageURL, imageError);
        updateNodeStyle(uploadButton, imageError);
    }

    private void validateImage(StringBuilder errors) {
        boolean isFileSelected = fileSelect.isSelected();
        if (isFileSelected) {
            validateImageFile(errors);
        } else {
            validateImageUrl(errors);
        }
    }

    private void validateInterests(StringBuilder errors) {

        if (interestSelected == 0) {
            errors.append("* You must select at least one interest.\n");
        }

    }


    /**
     * @brief Displays an alert window to the user.
     */
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) createButton.getScene().getWindow();
        alert.initOwner(stage);
        alert.showAndWait();
    }

    /**
     * @brief Add an error message if the field is null or empty.
     */
    private void appendIfEmpty(StringBuilder errors, String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            errors.append("* The field '").append(fieldName).append("' is mandatory.\n");
        }
    }

    /**
     * @brief Basic URL validation. Can be improved with regular expressions.
     */
    private boolean isValidUrl(String url) {
        // Very basic validation to check if it has 'http' or 'https'
        return url.matches("^(http|https)://.*");
    }

    // additional button methods
    @FXML
    private void onBackButtonClicked() {
        NavigationService.getInstance().goBack();
    }

    @FXML
    private void onCancelButtonClicked() {
        NavigationService.getInstance().goBack();
    }

    @FXML
    private void onAiButtonClicked() {
        StringBuilder validationErrors = new StringBuilder();
        validateEventTitle();
        validateDate();
        validateTime();
        validateLocation();
        validateCity();
        validateEventType();
        validateInterests(validationErrors);
        updateFieldState(validationErrors.toString(), "interest", interestsBox, interestsErrorLabel);


        if (tittleErrorLabel.isVisible() || dateErrorLabel.isVisible() || timeErrorLabel.isVisible() || locationErrorLabel.isVisible() || cityErrorLabel.isVisible() || eventTypeErrorLabel.isVisible() || interestsErrorLabel.isVisible()) {
            showAlert("Missing Information", "Please fill in all required fields before generating with AI.", "The AI needs the event title, date, time, location, city, event type, and interests to generate a description.", Alert.AlertType.WARNING);
            return;
        }

        // Placeholder for future LLM call
    }

    @FXML
    public void handleInteractionToggle(ActionEvent event) {
        Object source = event.getSource();

        if (source instanceof ToggleButton btn) {
            Label label = (Label) btn.getGraphic();
            if (btn.isSelected()) {
                interestSelected++;
                selectedInterests.add(label.getText());
            } else {
                interestSelected--;
                selectedInterests.remove(label.getText());
            }
            interestsLabel.setText("Selected: " + interestSelected + " interests");
        }
    }
}
