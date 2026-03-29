package UI.Controllers;

import Model.Entities.Event;
import Model.Services.EventService;
import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.lang.annotation.Inherited;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for editing an existing Event.
 * Heavily reuses validation logic from EventCreationController.
 */
public class EditEventController extends EventCreationController {

    private Event eventToEdit;

    // FXML elements (reusing IDs from EventCreationController)
    @FXML protected Button saveButton;
    @FXML protected Button deleteButton;

    // Constants (re-declared for clarity, but they inherit from parent)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Note: The 'interests' list field is not necessary to redeclare here if only used in the parent/private methods.


    public EditEventController() {
        // Retrieve the Event object passed via NavigationService
        Object data = NavigationService.getInstance().getData();
        if (data instanceof Event) {
            this.eventToEdit = (Event) data;
        } else {
            System.err.println("FATAL: EditEventController started without a valid Event object.");
            // Fallback: Navigate back if data is missing
            NavigationService.getInstance().goBack();
        }
    }

    @FXML
    @Override
    public void initialize() {
        super.initialize(); // Initialize parent validation and UI setup first

        if (eventToEdit != null) {
            populateForm(eventToEdit);
        }

        saveButton.setOnAction(event-> handleSaveEvent(event));
        deleteButton.setOnAction(event-> handleDeleteEvent(event));
    }

    private void populateForm(Event event) {
        eventTitle.setText(event.getTitle());
        eventDescription.setText(event.getDescription());

        // Date & Time
        datePicker.setValue(event.getDate());
        startTime.setText(event.getStartTime().format(TIME_FORMATTER));
        endTime.setText(event.getEndTime().format(TIME_FORMATTER));


        locationField.setText(event.getLocation());
        city.setText(event.getCity());
        eventType.setValue(event.getEventType());
        // Handling null MaxAttendees if necessary
        Integer maxAttendeesValue = event.getMaxAttendees() != null ? event.getMaxAttendees() : 0;
        maximumAttendees.getValueFactory().setValue(maxAttendeesValue);
        imageURL.setText(event.getImageURL());

        // Interests (Set toggle buttons based on existing interests)
        if (event.getInterests() != null) {
            for (String interest : event.getInterests()) {
                // Find and select the corresponding toggle button
                interestsFlow.getChildren().stream()
                        .filter(node -> node instanceof ToggleButton)
                        .map(node -> (ToggleButton) node)
                        .filter(btn -> ((Label) btn.getGraphic()).getText().equals(interest))
                        .findFirst()
                        .ifPresent(btn -> {
                            btn.setSelected(true);
                            // Manually call the toggle handler to update the internal counter/list
                            handleInteractionToggle(new ActionEvent(btn, null));
                        });
            }
        }
    }

    @FXML
    void handleSaveEvent(ActionEvent event) {
        // 1. Run all validations (inherited from EventCreationController)
        StringBuilder validationErrors = validateInput();
        updateErrorLabelsAndStyles(validationErrors);

        if (!validationErrors.isEmpty()) {
            showAlert("Validation error", "Please, correct the following errors before saving:", validationErrors.toString(), Alert.AlertType.ERROR);
            return;
        }

        // 2. Map form data back to the existing event object (using ID)
        Event eventDataFromForm = new Event();
        eventDataFromForm.setId(eventToEdit.getId()); // Crucial: Set the original ID
        eventDataFromForm.setCreatorID(eventToEdit.getCreatorID()); // Keep original creator

        // Get values from form
        String finalImageUrl = (imageURL != null && imageURL.getText() != null && !imageURL.getText().trim().isEmpty()) ? imageURL.getText().trim() : "";

        // Date & Time parsing
        LocalDate eventDate = datePicker.getValue();
        LocalTime parsedStartTime = LocalTime.parse(startTime.getText());
        LocalTime parsedEndTime = LocalTime.parse(endTime.getText());

        eventDataFromForm.setTitle(eventTitle.getText());
        eventDataFromForm.setDescription(eventDescription.getText());
        eventDataFromForm.setDate(eventDate);
        eventDataFromForm.setStartTime(parsedStartTime);
        eventDataFromForm.setEndTime(parsedEndTime);
        eventDataFromForm.setLocation(locationField.getText());
        eventDataFromForm.setCity(city.getText());
        eventDataFromForm.setEventType(eventType.getValue());
        eventDataFromForm.setMaxAttendees(maximumAttendees.getValue());
        eventDataFromForm.setPrice(eventToEdit.getPrice()); // Preserve non-editable fields

        // Interests collection (from FlowPane selection)
        List<String> selectedInterests = interestsFlow.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .filter(ToggleButton::isSelected)
                .map(btn -> ((Label) btn.getGraphic()).getText())
                .collect(Collectors.toList());
        eventDataFromForm.setInterests(selectedInterests);

        // Image URL/Path
        eventDataFromForm.setImageURL(finalImageUrl);


        // 3. Persist the update
        Event updatedEvent = EventService.getInstance().updateEvent(eventDataFromForm);

        // Show the success message and immediately navigate.
        // NOTE: If you are experiencing navigation issues, ensure your showAlert method
        // uses showAndWait() to block the thread, or remove it entirely.
        //showAlert("Success", "Event updated", "O evento '" + eventTitle.getText() + "' foi updated successfully.", Alert.AlertType.INFORMATION);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Event updated");
        alert.setContentText("O evento '" + eventTitle.getText() + "' foi updated successfully.");

        // [Optional but Recommended]: Set the owner stage (to center the alert)
        // Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        // alert.initOwner(stage);

        // CRITICAL: Block the thread until the user clicks OK.
        alert.showAndWait();

        // 4. Ação de Navegação: Go to DiscoverEvents and force refresh
        NavigationService.getInstance().goBackAndRefresh();
    }


    /**
     * Handles deleting the event.
     */
    @FXML
    void handleDeleteEvent(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Event: " + eventToEdit.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this event? This action cannot be undone.");

        // Show the alert and wait for user response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete the event
                boolean deleted = EventService.getInstance().deleteEvent(eventToEdit.getId());

                if (deleted) {
                    showAlert("Deleted", "Event Deleted", "The event was successfully deleted.", Alert.AlertType.INFORMATION);
                    // Navigate back to DiscoverEvents and refresh
                    NavigationService.getInstance().goBackAndRefresh();
                } else {
                    showAlert("Error", "Deletion Failed", "Could not find the event to delete.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    protected void onBackButtonClicked() {
        // Back button during editing should navigate to the details page, not discover page.
        NavigationService.getInstance().goBack();
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Stage stage = (Stage) saveButton.getScene().getWindow();
        alert.initOwner(stage);
        alert.showAndWait();
    }

    // The validation methods are inherited from the parent.
}