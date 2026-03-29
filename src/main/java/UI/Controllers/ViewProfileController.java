package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.SegmentedButton;

import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewProfileController {

    @FXML private Button backButton;
    @FXML private SegmentedButton segmentedTabButtons;
    @FXML private ToggleButton joinedEventsSelect;
    @FXML private ToggleButton createdEventsSelect;
    @FXML private ToggleButton reviewSelect;
    @FXML private FlowPane interestsFlow;
    @FXML private FlowPane eventTypesFlow;
    @FXML private FlowPane interestsEditFlow;
    @FXML private FlowPane eventTypesEditFlow;
    @FXML private ScrollPane tabsScrollPane;
    @FXML private AnchorPane tabsAnchorPane;
    @FXML private VBox tabsVBox;
    @FXML private TextFlow tabsHeaderLabel;
    @FXML private Button editProfileButton;
    @FXML private HBox saveCancelHBox;
    @FXML private VBox editProfileVBox;
    @FXML private Label nameLabel;
    @FXML private Label locationLabel;
    @FXML private Label bioLabel;
    @FXML private TextField fullName;
    @FXML private TextField locationField;
    @FXML private TextArea bio;
    @FXML private VBox viewProfileVBox;
    @FXML private Circle profilePic;
    @FXML private Button profilePicChanger;
    @FXML private Circle profileCircle;
    @FXML private Label attendedStatistic;
    @FXML private Label createdStatistic;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private Label fullNameErrorLabel;
    @FXML private Label locationErrorLabel;
    @FXML private Label bioErrorLabel;
    @FXML private Label interestsErrorLabel;
    @FXML private Label eventTypesErrorLabel;
    @FXML private StackPane profileStack;
    @FXML private Button profileButton;
    @FXML private Label interestsLabel;
    @FXML private Label eventTypesLabel;


    private User currentUser;
    private double lastScrollPosition = 0.0;


    private final List<String> interests = List.of("Music", "Sports", "Technology", "Art", "Food & Drink", "Business", "Health & Wellness", "Travel", "Photography",
            "Gaming", "Books", "Fashion", "Dance", "Comedy", "Film", "Fitness", "Outdoor Adventures");

    private final List<String> events = List.of("Concerts", "Workshops", "Networking", "Parties", "Conferences", "Sports Events", "Art Exhibitions", "Food Festivals",
            "Markets", "Meetups", "Classes", "Volunteer Work");


    @FXML
    public void initialize() {
        // Initial setup that only needs to happen once
        profilePicChanger.setOnAction(event -> handleProfilePicChanger());
        backButton.setOnAction(event -> onBack());
        refreshProfileContent();
        setupProfileMenu();

        Platform.runLater(() -> {
            double x = segmentedTabButtons.getWidth() / 2;
            joinedEventsSelect.setPrefWidth(x);
            createdEventsSelect.setPrefWidth(x);
            reviewSelect.setPrefWidth(0);
            reviewSelect.setVisible(false);
            reviewSelect.setDisable(true);

            tabsAnchorPane.setPrefWidth(tabsScrollPane.getLayoutBounds().getMaxX());
            tabsVBox.setPrefWidth(tabsScrollPane.getLayoutBounds().getMaxX());
            tabsVBox.setSpacing(10);

            // Add listeners to tab buttons
            joinedEventsSelect.setOnAction(event -> populateJoinedEvents());
            createdEventsSelect.setOnAction(event -> populateCreatedEvents());
            //reviewSelect.setOnAction(event -> populateReviews());

            // Set default view
            joinedEventsSelect.setSelected(true); // Ensure "Joined Events" tab is selected by default
            toggleEditMode(false);

            fullName.textProperty().addListener((obs, oldVal, newVal) -> validateFullName());
            locationField.textProperty().addListener((obs, oldVal, newVal) -> validateLocationField());
            bio.textProperty().addListener((obs, oldVal, newVal) -> validateBio());

            fullName.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) validateFullName();
            });
            locationField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) validateLocationField();
            });
            bio.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) validateBio();
            });
        });
    }

    private void setupProfileMenu() {
        // Create the context menu
        VBox profileDrop = new VBox();
        profileDrop.setFillWidth(true);
        Button logoutMenuItem = new Button("Logout");
        profileDrop.setVisible(false);

        profileDrop.getChildren().addAll(logoutMenuItem);
        profileStack.getChildren().add(profileDrop);


        // Set actions for the menu items
        logoutMenuItem.setOnAction(event -> {
            UserService.getInstance().logout();
            NavigationService.getInstance().navigateTo("LandingPage");
        });

        // --- Hover-to-show logic ---
        PauseTransition hideDelay = new PauseTransition(Duration.seconds(0.2));
        hideDelay.setOnFinished(e -> profileDrop.setVisible(false));

        profileButton.setOnMouseEntered(event -> {
            hideDelay.stop(); // Cancel any pending hide action

            if (!profileDrop.isVisible()) {
                profileDrop.setPrefWidth(profileButton.getWidth());
                profileDrop.setMinWidth(profileButton.getWidth());
                logoutMenuItem.setPrefWidth(profileButton.getWidth());
                logoutMenuItem.setMinWidth(profileButton.getWidth());
                profileDrop.setTranslateX(profileButton.getLayoutX() + profileButton.getParent().getLayoutX());
                profileDrop.setTranslateY(profileButton.getHeight());
                profileDrop.setVisible(true);
            }
        });

        profileButton.setOnMouseExited(event -> {
            hideDelay.playFromStart();
        });

        logoutMenuItem.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                hideDelay.stop();
            } else {
                hideDelay.playFromStart();
            }
        });

        profileButton.setOnAction(event -> {
            profileDrop.setVisible(!profileDrop.isVisible());
        });
    }

    @FXML
    private void onBack() {
        NavigationService.getInstance().goBackAndRefresh();
    }

    public void refreshProfileContent() {
        // Reload current user data
        currentUser = UserService.getInstance().getCurrentUser();

        // Update static profile info
        if (currentUser != null) {
            nameLabel.setText(currentUser.getName());
            locationLabel.setText(currentUser.getLocation());
            bioLabel.setText(currentUser.getBio());
            loadProfileImage(currentUser.getProfileImagePath());
            profileButton.setText(currentUser.getName());

            if (interestsLabel != null) {
                int count = (currentUser.getInterests() != null) ? currentUser.getInterests().size() : 0;
                interestsLabel.setText("Selected: " + count + " interests");
            }

            if (eventTypesLabel != null) {
                int count = (currentUser.getEventTypes() != null) ? currentUser.getEventTypes().size() : 0;
                eventTypesLabel.setText("Selected: " + count + " event types");
            }

            try {
                int currentUserId = currentUser.getId();
                List<Event> allEvents = EventService.getInstance().getAllEvents();

                if (allEvents != null) {
                    long createdCount = allEvents.stream()
                            .filter(event -> event.getCreatorID() == currentUserId)
                            .count();
                    createdStatistic.setText(String.valueOf(createdCount));

                    long attendedCount = allEvents.stream()
                            .filter(event ->
                                    event.getUsers().stream()
                                            .anyMatch(attendee -> attendee.getId() == currentUserId)
                            )
                            .filter(event -> event.getDate().isBefore(java.time.LocalDate.now()))
                            .count();
                    attendedStatistic.setText(String.valueOf(attendedCount));
                }
            } catch (Exception e) {
                System.err.println("Error calculating statistics: " + e.getMessage());
                createdStatistic.setText("N/A");
                attendedStatistic.setText("N/A");
            }
        }

        // Repopulate interests and event types...
        populateFlowPane(interestsFlow, interests, currentUser != null ? currentUser.getInterests() : List.of(), false);
        populateFlowPane(eventTypesFlow, events, currentUser != null ? currentUser.getEventTypes() : List.of(), false);
        populateFlowPane(interestsEditFlow, interests, currentUser != null ? currentUser.getInterests() : List.of(), true);
        populateFlowPane(eventTypesEditFlow, events, currentUser != null ? currentUser.getEventTypes() : List.of(), true);

        // Repopulate event lists...
        if (joinedEventsSelect.isSelected()) {
            Platform.runLater(this::populateJoinedEvents);
        } else if (createdEventsSelect.isSelected()) {
            Platform.runLater(this::populateCreatedEvents);
        } else {
            Platform.runLater(this::populateJoinedEvents);
        }
    }


    private void loadProfileImage(String imagePath) {
        Image image;
        File file = new File(imagePath);
        if (file.exists()) {
            image = new Image(file.toURI().toString());
        } else {
            InputStream imageStream = getClass().getResourceAsStream("/images/missing-image.png");
            if (imageStream != null) {
                image = new Image(imageStream);
            } else {
                // Handle the case where the default image is also missing
                // For example, create a placeholder image
                image = new Image("https://via.placeholder.com/100");
            }
        }
        ImagePattern imagePattern = new ImagePattern(image);
        profilePic.setFill(imagePattern);

        if (profileButton != null) {
            Circle buttonProfilePic = new Circle();
            // Bind radius to half of the button's height, so it resizes automatically.
            // Subtract a small amount for padding.
            profileCircle.setFill(imagePattern);
        }
    }

    private void handleProfilePicChanger() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(profilePicChanger.getScene().getWindow());
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            currentUser.setProfileImagePath(imagePath);
            UserService.getInstance().updateUser(currentUser);
            loadProfileImage(imagePath);
        }
    }

    private void populateFlowPane(FlowPane flowPane, List<String> allItems, List<String> selectedItems, boolean editable) {
        flowPane.getChildren().clear();

        Label targetErrorLabel = (flowPane == interestsEditFlow) ? interestsErrorLabel : eventTypesErrorLabel;
        String fieldName = (flowPane == interestsEditFlow) ? "interest" : "event type";

        for (String item : allItems) {
            Label label = new Label(item);
            label.getStyleClass().add("label-toggle-buttons-edit-profile");
            ToggleButton button = new ToggleButton();
            button.setGraphic(label);
            button.getStyleClass().add("toggle-buttons-edit-profile");
            if (selectedItems.contains(item)) {
                button.setSelected(true);
            }

            if (!editable) {
                button.addEventFilter(MouseEvent.MOUSE_PRESSED, MouseEvent::consume);
            } else {
                button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    validateFlowPaneSelection(flowPane, targetErrorLabel, fieldName);

                    long currentCount = flowPane.getChildren().stream()
                            .filter(node -> node instanceof ToggleButton && ((ToggleButton) node).isSelected())
                            .count();

                    if (flowPane == interestsEditFlow) {
                        if (interestsLabel != null) {
                            interestsLabel.setText("Selected: " + currentCount + " interests");
                        }
                    } else if (flowPane == eventTypesEditFlow) {
                        if (eventTypesLabel != null) {
                            eventTypesLabel.setText("Selected: " + currentCount + " event types");
                        }
                    }
                });
            }

            FlowPane.setMargin(button, new Insets(1));
            flowPane.getChildren().add(button);
        }
    }

    @FXML
    private void handleEditProfile() {
        toggleEditMode(true);
        fullName.setText(nameLabel.getText());
        locationField.setText(locationLabel.getText());
        bio.setText(bioLabel.getText());
    }

    @FXML
    private void handleSave() {
        validateFullName();
        validateBio();
        validateLocationField();

        boolean interestsValid = validateFlowPaneSelection(interestsEditFlow, interestsErrorLabel, "interest");
        boolean eventTypesValid = validateFlowPaneSelection(eventTypesEditFlow, eventTypesErrorLabel, "event type");
        boolean textFieldsValid = !fullNameErrorLabel.isVisible() && !locationErrorLabel.isVisible() && !bioErrorLabel.isVisible();

        if (!interestsValid || !eventTypesValid || !textFieldsValid) {
            return;
        }

        nameLabel.setText(fullName.getText());
        locationLabel.setText(locationField.getText());
        bioLabel.setText(bio.getText());

        if (currentUser != null) {
            currentUser.setName(fullName.getText());
            currentUser.setLocation(locationField.getText());
            currentUser.setBio(bio.getText());

            List<String> selectedInterests = new ArrayList<>();
            for (Node node : interestsEditFlow.getChildren()) {
                if (node instanceof ToggleButton && ((ToggleButton) node).isSelected()) {
                    selectedInterests.add(((Label)((ToggleButton) node).getGraphic()).getText());
                }
            }
            currentUser.setInterests(selectedInterests);

            List<String> selectedEventTypes = new ArrayList<>();
            for (Node node : eventTypesEditFlow.getChildren()) {
                if (node instanceof ToggleButton && ((ToggleButton) node).isSelected()) {
                    selectedEventTypes.add(((Label)((ToggleButton) node).getGraphic()).getText());
                }
            }
            currentUser.setEventTypes(selectedEventTypes);

            UserService.getInstance().updateUser(currentUser);
        }

        // After saving, refresh the displayed content
        refreshProfileContent();
        toggleEditMode(false);
    }

    @FXML
    private void handleCancel() {
        toggleEditMode(false);

        // ADD THIS BLOCK: Reset input fields to match current user data
        User currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser != null) {
            fullName.setText(currentUser.getName());
            locationField.setText(currentUser.getLocation());
            bio.setText(currentUser.getBio());
            // Add any other fields that need resetting (e.g. flows/lists)
        }

        refreshProfileContent(); // Updates the Labels (View Mode)
    }

    private void toggleEditMode(boolean edit) {
        if (edit) {
            editProfileButton.setVisible(false);
            editProfileButton.setDisable(true);
            saveCancelHBox.setVisible(true);
            saveCancelHBox.setDisable(false);
            editProfileVBox.setVisible(true);
            editProfileVBox.setDisable(false);
            if (viewProfileVBox != null) {
                viewProfileVBox.setVisible(false);
                viewProfileVBox.setDisable(true);
            }
        } else {
            editProfileButton.setVisible(true);
            editProfileButton.setDisable(false);
            saveCancelHBox.setVisible(false);
            saveCancelHBox.setDisable(true);
            editProfileVBox.setVisible(false);
            editProfileVBox.setDisable(true);
            if (viewProfileVBox != null) {
                viewProfileVBox.setVisible(true);
                viewProfileVBox.setDisable(false);
            }
        }
    }


    private void updateTabsHeader(String tab) {
        tabsHeaderLabel.getChildren().clear();
        SVGPath icon = new SVGPath();
        Text text = new Text();
        text.setStyle("-fx-font-weight: 700; -fx-font-family: ui-sans-serif,system-ui,sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",Segoe UI Symbol,\"Noto Color Emoji\"; -fx-font-size: 26px;");

        switch (tab) {
            case "joined":
                icon.setContent("M12 6v6l4 2 M12 22A10 10 0 1 1 12 2a10 10 0 0 1 0 20z");
                text.setText(" Upcoming Events");
                break;
            case "created":
                icon.setContent("M6 9H4.5a2.5 2.5 0 0 1 0-5H6 M18 9h1.5a2.5 2.5 0 0 0 0-5H18 M4 22h16 M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22 M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22 M18 2H6v7a6 6 0 0 0 12 0V2Z");
                text.setText(" Created Events");
                break;
            case "reviews":
                text.setText("Past Event Reviews");
                tabsHeaderLabel.getChildren().add(text);
                return;
        }

        icon.setStroke(Paint.valueOf("black"));
        icon.setStrokeWidth(2);
        icon.setStrokeLineCap(StrokeLineCap.ROUND);
        icon.setStrokeLineJoin(StrokeLineJoin.ROUND);
        icon.setFill(Paint.valueOf("transparent"));
        icon.setMouseTransparent(true); // Make icon transparent to mouse events

        StackPane iconPane = new StackPane(icon);
        iconPane.setPrefSize(20, 20);
        iconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
        tabsHeaderLabel.getChildren().addAll(iconPane, text);
    }

    private void populateJoinedEvents() {
        updateTabsHeader("joined");
        tabsVBox.getChildren().clear();
        if (currentUser != null && currentUser.getEvents() != null) {
            for (Event event : currentUser.getEvents()) {
                tabsVBox.getChildren().add(createCard(event, "joined"));
            }
        }
        Platform.runLater(() -> tabsScrollPane.setVvalue(lastScrollPosition));
    }

    private void populateCreatedEvents() {
        updateTabsHeader("created");
        tabsVBox.getChildren().clear();
        if (currentUser != null && currentUser.getCreatedEvents() != null) {
            for (Event event : currentUser.getCreatedEvents()) {
                tabsVBox.getChildren().add(createCard(event, "created"));
            }
        }
        Platform.runLater(() -> tabsScrollPane.setVvalue(lastScrollPosition));
    }

/*    private void populateReviews() {
        updateTabsHeader("reviews");
        tabsVBox.getChildren().clear();
        // Logic to populate reviews
    }
*/

    public HBox createCard(Event event, String cardType) { // Changed parameter name from Event to event
        HBox cardHBox = new HBox();
        cardHBox.setMinWidth(Region.USE_PREF_SIZE);
        cardHBox.setMinHeight(Region.USE_PREF_SIZE);
        cardHBox.setMaxWidth(Region.USE_PREF_SIZE);
        cardHBox.setMaxHeight(Region.USE_PREF_SIZE);
        cardHBox.setPrefWidth(tabsScrollPane.getLayoutBounds().getMaxX() - 41);
        cardHBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        cardHBox.getStyleClass().add("box-border");
        cardHBox.setAlignment(Pos.CENTER);
        cardHBox.setPadding(new Insets(10));

        VBox vBoxLeft = new VBox();
        vBoxLeft.setPrefWidth((tabsScrollPane.getLayoutBounds().getMaxX() - 41) * 0.6);
        vBoxLeft.setPadding(new Insets(0, 0, 0, 10));
        HBox.setHgrow(vBoxLeft, Priority.ALWAYS); // Make vBoxLeft grow

        Label titleLabel = new Label(event.getTitle());

        // Updated date and time formatting
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Changed to 24-hour format
        String formattedDateTime = event.getDate().format(dateFormatter) + " from " +
                event.getStartTime().format(timeFormatter) + " to " +
                event.getEndTime().format(timeFormatter);
        Label dateLabel = new Label(formattedDateTime);

        Label locationLabel = new Label(event.getLocation());
        titleLabel.getStyleClass().add("tab-buttons-view-profile");
        titleLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: black; -fx-font-size: 14px;");
        dateLabel.getStyleClass().add("tab-buttons-view-profile");
        locationLabel.getStyleClass().add("tab-buttons-view-profile");
        vBoxLeft.getChildren().addAll(titleLabel, dateLabel, locationLabel);

        HBox hBoxRight = new HBox(5);
        hBoxRight.setPrefWidth((tabsScrollPane.getLayoutBounds().getMaxX() - 41) * 0.4);
        hBoxRight.setAlignment(Pos.CENTER_RIGHT);
        hBoxRight.setPadding(new Insets(0, 20, 0, 0));
        HBox.setHgrow(hBoxRight, Priority.NEVER); // Prevent hBoxRight from growing

        if ("joined".equals(cardType)) {
            // Button 1: Info
            Button infoButton = new Button(); // Renamed to infoButton
            SVGPath infoIcon = new SVGPath();
            infoIcon.setContent("M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z");
            infoIcon.setFill(Paint.valueOf("Black"));
            infoIcon.setStroke(Paint.valueOf("Black"));
            infoIcon.setStrokeWidth(4);
            infoIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            infoIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
            infoIcon.getStyleClass().add("infoSVG");
            infoIcon.setMouseTransparent(true);
            StackPane infoIconPane = new StackPane(infoIcon);
            infoIconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
            infoIcon.setScaleX(0.025);
            infoIcon.setScaleY(0.025);

            infoButton.setGraphic(infoIconPane);
            infoButton.getStyleClass().add("toggle-buttons-edit-profile");
            infoButton.setPrefSize(30, 30);
            infoButton.setMinSize(30, 30);
            infoButton.setMaxSize(30, 30);
            infoButton.setCursor(Cursor.OPEN_HAND);
            // Apply clip to infoButton
            Rectangle infoClip = new Rectangle(infoButton.getPrefWidth(), infoButton.getPrefHeight());
            infoButton.setClip(infoClip);
            infoButton.setOnAction(e -> {
                NavigationService.getInstance().navigateTo("EventDetails", event);
            });

            // Button 2: Joined/Unjoin
            Label unjoinButtonLabel = new Label("Joined"); // Renamed to unjoinButtonLabel
            ToggleButton unjoinButton = new ToggleButton(); // Renamed to unjoinButton
            unjoinButtonLabel.getStyleClass().add("label-toggle-buttons-edit-profile");
            unjoinButton.setGraphic(unjoinButtonLabel);
            unjoinButton.getStyleClass().add("toggle-buttons-edit-profile");
            unjoinButton.setPrefSize(60, 30);
            unjoinButton.setMinSize(60, 30);
            unjoinButton.setMaxSize(60, 30);
            unjoinButton.setCursor(Cursor.OPEN_HAND);
            unjoinButton.setSelected(true);

            EventHandler<MouseEvent> mouseEnteredHandler = e -> unjoinButtonLabel.setText("Unjoin");
            EventHandler<MouseEvent> mouseExitedHandler = e -> unjoinButtonLabel.setText("Joined");

            unjoinButton.setOnMouseEntered(mouseEnteredHandler);
            unjoinButton.setOnMouseExited(mouseExitedHandler);

            unjoinButton.setOnAction(e -> {
                unjoinButton.setSelected(true);
                unjoinButton.setOnMouseExited(null); // Disable mouse exit event

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unjoin Event");
                alert.setHeaderText("Are you sure you want to unjoin this event?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        lastScrollPosition = tabsScrollPane.getVvalue();
                        // Use the correct service method
                        EventService.getInstance().removeUserFromEvent(event.getId(), currentUser);

                        // Refresh the list
                        refreshProfileContent();
                    }
                    unjoinButton.setOnMouseExited(mouseExitedHandler); // Re-enable mouse exit event
                    if (!unjoinButton.isHover()) { // If mouse is not over the button, reset text
                        unjoinButtonLabel.setText("Joined");
                    }
                });
            });
            hBoxRight.getChildren().addAll(infoButton, unjoinButton); // Changed button1, button2 to infoButton, unjoinButton

        } else if ("created".equals(cardType)) {
            SVGPath attendeesIcon = new SVGPath();
            attendeesIcon.setContent("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 4a4 4 0 1 1 0 8 4 4 0 0 1 0-8z M16 11l2 2 4-4");
            attendeesIcon.setStroke(Paint.valueOf("#999999"));
            attendeesIcon.setStrokeWidth(1);
            attendeesIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            attendeesIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
            attendeesIcon.setFill(Paint.valueOf("transparent"));
            attendeesIcon.setScaleX(0.8);
            attendeesIcon.setScaleY(0.8);
            attendeesIcon.setMouseTransparent(true); // Make icon transparent to mouse events

            StackPane attendeesIconPane = new StackPane(attendeesIcon);
            attendeesIconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
            attendeesIconPane.setPrefSize(16,16);
            attendeesIconPane.setAlignment(Pos.CENTER_LEFT);
            attendeesIconPane.setTranslateX(-2);


            Label attendeesLabel = new Label(event.getUsers().size() + "/" + event.getMaxAttendees() + " attendees"); // Updated with actual attendees count
            attendeesLabel.getStyleClass().add("tab-buttons-view-profile");

            HBox attendeesHBox = new HBox(attendeesIconPane, attendeesLabel);
            attendeesHBox.setSpacing(1);
            attendeesHBox.setAlignment(Pos.CENTER_LEFT);
            vBoxLeft.getChildren().add(attendeesHBox);

            // Button 1: Info (Added for created events as well)
            Button infoButton = new Button();
            SVGPath infoIcon = new SVGPath();
            infoIcon.setContent("M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z");
            infoIcon.setFill(Paint.valueOf("Black"));
            infoIcon.setStroke(Paint.valueOf("Black"));
            infoIcon.setStrokeWidth(4);
            infoIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            infoIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
            infoIcon.getStyleClass().add("infoSVG");
            infoIcon.setMouseTransparent(true); // Make icon transparent to mouse events
            StackPane infoIconPane = new StackPane(infoIcon);
            infoIconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
            infoIcon.setScaleX(0.025);
            infoIcon.setScaleY(0.025);

            infoButton.setGraphic(infoIconPane);
            infoButton.getStyleClass().add("toggle-buttons-edit-profile");
            infoButton.setPrefSize(30, 30);
            infoButton.setMinSize(30, 30);
            infoButton.setMaxSize(30, 30);
            infoButton.setCursor(Cursor.OPEN_HAND);
            // Apply clip to infoButton
            Rectangle infoClip = new Rectangle(infoButton.getPrefWidth(), infoButton.getPrefHeight());
            infoButton.setClip(infoClip);
            infoButton.setOnAction(e -> {
                NavigationService.getInstance().navigateTo("EventDetails", event);
            });


            // Button 2: Edit
            Button editButton = new Button(); // Renamed to editButton
            SVGPath editSVG = new SVGPath();
            editSVG.setContent("M200-200h57l391-391-57-57-391 391v57Zm-80 80v-170l528-527q12-11 26.5-17t30.5-6q16 0 31 6t26 18l55 56q12 11 17.5 26t5.5 30q0 16-5.5 30.5T817-647L290-120H120Zm640-584-56-56 56 56Zm-141 85-28-29 57 57-29-28Z");
            editSVG.setFill(Paint.valueOf("black"));
            editSVG.setStroke(Paint.valueOf("Black"));
            editSVG.setStrokeWidth(0.5);
            editSVG.setStrokeLineCap(StrokeLineCap.ROUND);
            editSVG.setStrokeLineJoin(StrokeLineJoin.ROUND);
            editSVG.getStyleClass().add("infoSVG");
            editSVG.setMouseTransparent(true); // Make icon transparent to mouse events
            StackPane editIconPane = new StackPane(editSVG);
            editIconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
            editIconPane.setPrefSize(16, 16);
            editIconPane.setMinSize(16, 16);
            editIconPane.setMaxSize(16, 16);
            editSVG.setScaleX(0.02);
            editSVG.setScaleY(0.02);


            editButton.setGraphic(editIconPane);
            editButton.getStyleClass().add("edit-button-profile");
            editButton.setStyle("-fx-pref-height: 30px; -fx-max-height: 30px; -fx-min-height: 30px; -fx-pref-width: 30px; -fx-max-width: 30px; -fx-min-width: 30px;"); // Adjusted width for icon only
            editButton.setCursor(Cursor.OPEN_HAND);
            editButton.setGraphicTextGap(5);
            // Apply clip to editButton
            Rectangle editClip = new Rectangle(30, 30); // Fixed dimensions
            editButton.setClip(editClip);
            editButton.setOnAction(e -> {
                lastScrollPosition = tabsScrollPane.getVvalue();
                NavigationService.getInstance().navigateTo("EditEvent", event);
            });

            // Button 3: Delete
            Button deleteButton = new Button(); // Renamed to deleteButton
            SVGPath deleteIcon = new SVGPath();
            deleteIcon.setContent("M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2 M10 11v6 M14 11v6");
            deleteIcon.setFill(Paint.valueOf("TRANSPARENT"));
            deleteIcon.setStroke(Paint.valueOf("RED"));
            deleteIcon.setStrokeWidth(2);
            deleteIcon.setStrokeLineCap(StrokeLineCap.ROUND);
            deleteIcon.setStrokeLineJoin(StrokeLineJoin.ROUND);
            deleteIcon.getStyleClass().add("infoSVG");
            deleteIcon.setMouseTransparent(true); // Make icon transparent to mouse events
            StackPane deleteIconPane = new StackPane(deleteIcon);
            deleteIconPane.setMouseTransparent(true); // Make StackPane transparent to mouse events
            deleteIconPane.setPrefSize(16, 16);
            deleteIconPane.setMinSize(16, 16);
            deleteIconPane.setMaxSize(16, 16);
            deleteIcon.setScaleX(.8);
            deleteIcon.setScaleY(.8);

            deleteButton.setGraphic(deleteIconPane);
            deleteButton.getStyleClass().add("toggle-buttons-edit-profile");
            deleteButton.setStyle("-fx-pref-height: 30px; -fx-max-height: 30px; -fx-min-height: 30px; -fx-pref-width: 30px; -fx-max-width: 30px; -fx-min-width: 30px;");
            deleteButton.setCursor(Cursor.OPEN_HAND);
            // Apply clip to deleteButton
            Rectangle deleteClip = new Rectangle(30, 30); // Fixed dimensions
            deleteButton.setClip(deleteClip);
            deleteButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Event");
                alert.setHeaderText("Are you sure you want to delete this event?");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        lastScrollPosition = tabsScrollPane.getVvalue();
                        EventService.getInstance().deleteEvent(event.getId());
                        refreshProfileContent(); // Refresh the entire profile
                    }
                });
            });
            hBoxRight.getChildren().addAll(infoButton, editButton, deleteButton); // Changed button1, button2 to editButton, deleteButton
        }

        cardHBox.getChildren().addAll(vBoxLeft, hBoxRight);
        return cardHBox;
    }

    /**
     * @brief Add an error message if the field is null or empty.
     */
    private void appendIfEmpty(StringBuilder errors, String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            errors.append("* The field '").append(fieldName).append("' is mandatory.\n");
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


    private void validateFullName() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, fullName.getText(), "Full Name");
        updateFieldState(errors.toString(), "Full Name", fullName, fullNameErrorLabel);
    }

    private void validateLocationField() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, locationField.getText(), "Location");
        updateFieldState(errors.toString(), "Location", locationField, locationErrorLabel);
    }

    private void validateBio() {
        StringBuilder errors = new StringBuilder();
        appendIfEmpty(errors, bio.getText(), "Bio");
        updateFieldState(errors.toString(), "Bio", bio, bioErrorLabel);
    }

    private boolean validateFlowPaneSelection(FlowPane flowPane, Label errorLabel, String fieldName) {
        boolean hasSelection = false;

        for (Node node : flowPane.getChildren()) {
            if (node instanceof ToggleButton toggleButton) {
                if (toggleButton.isSelected()) {
                    hasSelection = true;
                    break;
                }
            }
        }

        if (!hasSelection) {
            errorLabel.setText("At least one " + fieldName + " must be selected.");
            errorLabel.setVisible(true);
            return false;
        } else {
            errorLabel.setVisible(false);
            errorLabel.setText("");
            return true;
        }
    }

}
