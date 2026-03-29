package UI.Controllers;

import Model.Entities.User;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Text;

import java.util.List;
import java.util.ArrayList;

public class EditProfileController {

    private final NavigationService navigationService;
    private final UserService userService;

    @FXML
    private TextField locArea;

    @FXML
    private GridPane gridOpt;

    @FXML
    private FlowPane interestsOptions;

    @FXML
    private FlowPane eventTypes;

    @FXML
    private TextArea bioArea;

    @FXML
    private Label selectedLabel;

    @FXML
    private Button continueButton;

    @FXML
    private Button backButton;

    @FXML
    private Label continueButtonLabel;

    @FXML
    private Label backButtonLabel;

    @FXML
    private Label progressID0;

    @FXML
    private Circle progressID1;

    @FXML
    private Circle progressID2;

    @FXML
    private Circle progressID3;

    @FXML
    private Circle progressID4;

    @FXML
    private SVGPath svgEditP;

    @FXML
    private Label titleEditP;

    @FXML
    private Label subtitleEditP;

    private final List<String> interests = List.of("Music", "Sports", "Technology", "Art", "Food & Drink", "Business", "Health & Wellness", "Travel", "Photography",
            "Gaming", "Books", "Fashion", "Dance", "Comedy", "Film", "Fitness", "Outdoor Adventures");

    private final List<String> events = List.of("Concerts", "Workshops", "Networking", "Parties", "Conferences", "Sports Events", "Art Exhibitions", "Food Festivals",
            "Markets", "Meetups", "Classes", "Volunteer Work");

    private int interestSelected = 0;
    private int eventsSelected = 0;
    private int nrCharacters = 0;

    public EditProfileController() {
        this.navigationService = NavigationService.getInstance();
        this.userService = UserService.getInstance();
    }


    @FXML
    public void stepForward(ActionEvent event) {
        switch (progressID0.getText()) {
            case "1 ":
                step2();
                break;
            case "2 ":
                step3();
                break;
            case "3 ":
                step4();
                break;
            case "4 ":
                handleComplete();
                break;
            default:
                System.err.println("Incorrect progress state: " + progressID0);
                break;
        }
    }

    @FXML
    public void stepBackward(ActionEvent event) {
        switch (progressID0.getText()) {
            case "1 ":
                break;
            case "2 ":
                step1();
                break;
            case "3 ":
                step2();
                break;
            case "4 ":
                step3();
                break;
            default:
                System.err.println("Incorrect progress state: " + progressID0);
                break;
        }
    }

    @FXML
    public void initialize() {
        step1();

        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getLocation() != null) {
                locArea.setText(currentUser.getLocation());
            }
            if (currentUser.getBio() != null) {
                bioArea.setText(currentUser.getBio());
                nrCharacters = currentUser.getBio().length();
            } else {
                bioArea.setText("");
                nrCharacters = 0;
            }
        }
    }

    @FXML
    public void step1() {
        gridOpt.getRowConstraints().get(1).setPercentHeight(9);
        locArea.setVisible(true);
        locArea.setDisable(false);
        interestsOptions.setVisible(false);
        interestsOptions.setDisable(true);
        continueButton.setVisible(true);
        continueButton.setDisable(false);
        backButton.setVisible(false);
        backButton.setDisable(true);
        selectedLabel.setVisible(false);
        selectedLabel.setDisable(true);
        progressID0.setText("1 ");
        progressID1.getStyleClass().setAll("currentStepsSHOW");
        progressID2.getStyleClass().setAll("nextStepsSHOW");
        progressID3.getStyleClass().setAll("nextStepsSHOW");
        progressID4.getStyleClass().setAll("nextStepsSHOW");
        svgEditP.setContent("M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0 M15 10 a 3 3 0 1 1 -6 0 a 3 3 0 1 1 6 0");
        titleEditP.setText("Where are you located?");
        subtitleEditP.setText("Help us find amazing events in your area");
        locArea.setPromptText("Enter your city or address");
        continueButtonLabel.setText("Continue");
    }

    @FXML
    public void step2() {
        gridOpt.getRowConstraints().get(1).setPercentHeight(20);
        interestsOptions.setVisible(true);
        interestsOptions.setDisable(false);
        selectedLabel.setVisible(true);
        selectedLabel.setDisable(false);
        locArea.setVisible(false);
        locArea.setDisable(true);
        eventTypes.setVisible(false);
        eventTypes.setDisable(true);
        backButton.setVisible(true);
        backButton.setDisable(false);
        progressID0.setText("2 ");
        progressID1.getStyleClass().setAll("pastStepsSHOW");
        progressID2.getStyleClass().setAll("currentStepsSHOW");
        progressID3.getStyleClass().setAll("nextStepsSHOW");
        progressID4.getStyleClass().setAll("nextStepsSHOW");
        svgEditP.setContent("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M13 7 A 4 4 0 1 1 5 7 A 4 4 0 1 1 13 7 M22 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75");
        titleEditP.setText("What interests you?");
        subtitleEditP.setText("Select your interests so we can recommend perfect events");
        backButtonLabel.setText("Back");

        if (interestsOptions.getChildren().isEmpty()) {
            User currentUser = userService.getCurrentUser();
            List<String> userInterests = (currentUser != null && currentUser.getInterests() != null) ? currentUser.getInterests() : new ArrayList<>();
            interestSelected = 0;

            for (String interest : interests) {
                Label interestLabel = new Label(interest);
                interestLabel.getStyleClass().add("label-toggle-buttons-edit-profile");
                ToggleButton interestBtn = new ToggleButton();
                interestBtn.setGraphic(interestLabel);
                interestBtn.getStyleClass().add("toggle-buttons-edit-profile");
                interestBtn.getStyleClass().add("interestLabel");
                FlowPane.setMargin(interestBtn, new Insets(3));
                interestBtn.setOnAction(this::handleInteractionToggle);
                interestLabel.getStyleClass().add("insterestLabel");

                if (userInterests.contains(interest)) {
                    interestBtn.setSelected(true);
                    interestSelected++;
                }
                interestsOptions.getChildren().add(interestBtn);
            }
        }
        selectedLabel.setText("Selected: " + interestSelected + " interests");
    }

    @FXML
    public void step3() {
        gridOpt.getRowConstraints().get(1).setPercentHeight(20);
        eventTypes.setVisible(true);
        eventTypes.setDisable(false);
        selectedLabel.setVisible(true);
        selectedLabel.setDisable(false);
        interestsOptions.setVisible(false);
        interestsOptions.setDisable(true);
        bioArea.setVisible(false);
        bioArea.setDisable(true);
        progressID0.setText("3 ");
        progressID1.getStyleClass().setAll("pastStepsSHOW");
        progressID2.getStyleClass().setAll("pastStepsSHOW");
        progressID3.getStyleClass().setAll("currentStepsSHOW");
        progressID4.getStyleClass().setAll("nextStepsSHOW");
        svgEditP.setContent("M8 2v4 M16 2v4 M 5 4 L 19 4 A 2 2 0 0 1 21 6 L 21 20 A 2 2 0 0 1 19 22 L 5 22 A 2 2 0 0 1 3 20 L 3 6 A 2 2 0 0 1 5 4 Z M3 10h18");
        titleEditP.setText("What types of events?");
        subtitleEditP.setText("Choose the event formats you enjoy most");

        if (eventTypes.getChildren().isEmpty()) {
            User currentUser = userService.getCurrentUser();
            List<String> userEventTypes = (currentUser != null && currentUser.getEventTypes() != null) ? currentUser.getEventTypes() : new ArrayList<>();
            eventsSelected = 0;

            for (String event : events) {
                Label eventLabel = new Label(event);
                eventLabel.getStyleClass().add("label-toggle-buttons-edit-profile");
                ToggleButton eventBtn = new ToggleButton();
                eventBtn.setGraphic(eventLabel);
                eventBtn.getStyleClass().add("toggle-buttons-edit-profile");
                eventBtn.getStyleClass().add("eventLabel");
                eventBtn.setOnAction(this::handleInteractionToggle);
                FlowPane.setMargin(eventBtn, new Insets(3));

                if (userEventTypes.contains(event)) {
                    eventBtn.setSelected(true);
                    eventsSelected++;
                }
                eventTypes.getChildren().add(eventBtn);
            }
        }
        selectedLabel.setText("Selected: " + eventsSelected + " events");
    }

    @FXML
    public void step4() {
        gridOpt.getRowConstraints().get(1).setPercentHeight(20);
        bioArea.setVisible(true);
        bioArea.setDisable(false);
        selectedLabel.setVisible(true);
        selectedLabel.setDisable(false);
        interestsOptions.setVisible(false);
        interestsOptions.setDisable(true);
        eventTypes.setVisible(false);
        eventTypes.setDisable(true);
        progressID0.setText("4 ");
        progressID1.getStyleClass().setAll("pastStepsSHOW");
        progressID2.getStyleClass().setAll("pastStepsSHOW");
        progressID3.getStyleClass().setAll("pastStepsSHOW");
        progressID4.getStyleClass().setAll("currentStepsSHOW");
        bioArea.setPromptText("Tell us about your interests, hobbies, or what makes you unique...");
        svgEditP.setContent("M 22 12 A 10 10 0 1 1 2 12 A 10 10 0 1 1 22 12 M 15 10 A 3 3 0 1 1 9 10 A 3 3 0 1 1 15 10 M7 20.662V19a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v1.662");
        titleEditP.setText("Tell us about yourself");
        subtitleEditP.setText("Share a bit about who you are");
        continueButtonLabel.setText("Complete setup");

        nrCharacters = bioArea.getText().length();
        selectedLabel.setText(nrCharacters + " characters");
    }

    @FXML
    public void handleComplete() {
        String location = locArea.getText();
        List<String> selectedInterests = new ArrayList<>();
        List<String> selectedEvents = new ArrayList<>();
        String bio = bioArea.getText();

        for (Node node : interestsOptions.getChildren()) {
            if (node instanceof ToggleButton btn) {
                if (btn.isSelected()) {
                    Label label = (Label) btn.getGraphic();
                    selectedInterests.add(label.getText());
                }
            }
        }

        for (Node node : eventTypes.getChildren()) {
            if (node instanceof ToggleButton btn) {
                if (btn.isSelected()) {
                    Label label = (Label) btn.getGraphic();
                    selectedEvents.add(label.getText());
                }
            }
        }

        List<String> errors = new ArrayList<>();

        if (location == null || location.trim().isEmpty()) {
            errors.add("Please enter your location.");
        }

        if (selectedInterests.isEmpty()) {
            errors.add("Please select at least one interest.");
        }

        if (selectedEvents.isEmpty()) {
            errors.add("Please select at least one event type.");
        }

        if (bio == null || bio.trim().isEmpty()) {
            errors.add("Please enter a bio.");
        }

        if (!errors.isEmpty()) {
            showAlert("Validation Error", String.join("\n", errors));
            return;
        }

        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            currentUser.setLocation(location);
            currentUser.setInterests(selectedInterests);
            currentUser.setEventTypes(selectedEvents);
            currentUser.setBio(bio);
            currentUser.setFirstEntry(false);
            userService.updateUser(currentUser);
        }

        navigationService.navigateTo("DiscoverEvents");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleInteractionToggle(ActionEvent event) {
        Object source = event.getSource();

        if(source instanceof ToggleButton btn)
        {
            if(btn.getStyleClass().contains("interestLabel")) {
                if(btn.isSelected()) interestSelected++;
                else interestSelected--;
                selectedLabel.setText("Selected: " + interestSelected + " interests");
            } else if(btn.getStyleClass().contains("eventLabel")) {
                if(btn.isSelected()) eventsSelected++;
                else eventsSelected--;
                selectedLabel.setText("Selected: " + eventsSelected + " events");

            }
        }
    }

    @FXML
    public void handleInteractionTArea(KeyEvent keyEvent) {
        Object source = keyEvent.getSource();

        if(source instanceof TextArea ta) {
            nrCharacters = ta.getText().length();
            selectedLabel.setText(nrCharacters + " characters");
        }
    }
}
