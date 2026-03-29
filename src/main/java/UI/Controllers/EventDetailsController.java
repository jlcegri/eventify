package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.UserService;
import UI.Services.NavigationService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Controller class for the event details view.
 */
public class EventDetailsController {

    private final NavigationService navigationService;
    private final UserService userService;
    private Event currentEvent;

    // Labels
    @FXML
    private Label eventTypeLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label aboutLabel;
    @FXML
    private Label organizedByLabel;

    // Buttons
    @FXML
    private Button backButton;
    @FXML
    private Button joinEventButton;

    @FXML
    private ImageView eventImageView;

    @FXML
    private VBox Attendees;

    @FXML
    private StackPane profileStack;
    @FXML
    private Button profileButton;
    @FXML
    private Circle profileCircle;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm"); // Changed to 24-hour format

    /**
     * Public constructor required by the FXMLLoader.
     */
    public EventDetailsController() {
        this.navigationService = NavigationService.getInstance();
        this.userService = UserService.getInstance();
        this.currentEvent = (Event) navigationService.getData();
    }

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(eventImageView.fitWidthProperty());
        clip.heightProperty().bind(eventImageView.fitHeightProperty());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        eventImageView.setClip(clip);

        populateEventDetails();
        setupProfileMenu();

        backButton.setOnAction(event -> handleBackButton());
        joinEventButton.setOnAction(event -> handleJoinEventButton());
        joinEventButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (joinEventButton.getText() == "Joined" || joinEventButton.getText() == "Unjoin") {
                if (newValue)
                    joinEventButton.setText("Unjoin");
                else
                    joinEventButton.setText("Joined");

            }
        });
    }

    public void refresh() {
        this.currentEvent = (Event) navigationService.getData();
        populateEventDetails();
        setupProfileMenu();
    }

    private void populateEventDetails() {
        if (currentEvent != null) {
            eventTypeLabel.setText(currentEvent.getEventType());
            nameLabel.setText(currentEvent.getTitle());
            dateLabel.setText(currentEvent.getDate().toString());
            locationLabel.setText(currentEvent.getLocation());
            aboutLabel.setText(currentEvent.getDescription());

            // Set image
            final Image defaultImage = new Image(getClass().getResourceAsStream("/images/missing-image.png"));
            String imagePath = currentEvent.getImageURL();
            Image image;

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                try {
                    String imageSource;
                    // Check if it's a web URL or a local file path
                    if (imagePath.matches("^(http|https)://.*")) {
                        imageSource = imagePath;
                    } else {
                        File file = new File(imagePath);
                        imageSource = file.toURI().toString();
                    }

                    image = new Image(imageSource, true); // true for background loading

                    image.errorProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            System.err.println("Error loading image: " + imagePath);
                            eventImageView.setImage(defaultImage);
                        }
                    });

                    // If there's an immediate error (e.g., invalid path), fall back.
                    if (image.isError()) {
                        image = defaultImage;
                    }
                } catch (Exception e) {
                    System.err.println("Exception while creating image from path: " + imagePath);
                    image = defaultImage;
                }
            } else {
                image = defaultImage;
            }
            eventImageView.setImage(image);

            User creator = userService.getUserById(currentEvent.getCreatorID());

            if (creator != null) {
                organizedByLabel.setText(creator.getName());
            } else {
                organizedByLabel.setText("Unknown");
            }

            // Logic to manage the state of join button
            User currentUser = userService.getCurrentUser();

            if (currentUser != null) {
                boolean isAttending = currentEvent.getUsers().contains(currentUser);
                boolean isFull = currentEvent.getMaxAttendees() != null && currentEvent.getUsers().size() >= currentEvent.getMaxAttendees();

                if (isAttending) {
                    // User is already Joined
                    joinEventButton.setText("Joined");
                    joinEventButton.setDisable(false);
                } else if (isFull) {
                    // Event is full
                    joinEventButton.setText("Full");
                    joinEventButton.setDisable(true);
                } else {
                    // User can join
                    joinEventButton.setText("Join Event");
                    joinEventButton.setDisable(false);
                }
            } else {
                // User is not logged in
                joinEventButton.setText("Log in to join the event");
                joinEventButton.setDisable(true);
            }

            populateAttendees();
        }
    }

    private void populateAttendees() {
        Attendees.getChildren().clear();
        List<User> users = currentEvent.getUsers();
        Collections.shuffle(users);
        int count = 0;
        for (User user : users) {
            if (count < 4) {
                Label userLabel = new Label(user.getName());
                userLabel.setStyle("-fx-font-size: 14px;");
                VBox.setMargin(userLabel, new Insets(5));
                Attendees.getChildren().add(userLabel);
                count++;
            } else {
                break;
            }
        }
    }

    private void setupProfileMenu() {
        // Load profile image
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            loadProfileImage(currentUser.getProfileImagePath());
            profileButton.setText(currentUser.getName());
        }

        // Remove any existing dropdown from a previous view
        profileStack.getChildren().removeIf(node -> node.getStyleClass().contains("profile-dropdown"));

        // Create the context menu
        VBox profileDrop = new VBox();
        profileDrop.getStyleClass().add("profile-dropdown");
        profileDrop.setFillWidth(true);
        Button profileMenuItem = new Button("Profile");
        Button logoutMenuItem = new Button("Logout");
        profileDrop.setVisible(false);

        profileDrop.getChildren().addAll(profileMenuItem, logoutMenuItem);
        profileStack.getChildren().add(profileDrop);


        // Set actions for the menu items
        profileMenuItem.setOnAction(event -> navigateToProfile());
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
                profileMenuItem.setPrefWidth(profileButton.getWidth());
                profileMenuItem.setMinWidth(profileButton.getWidth());
                logoutMenuItem.setPrefWidth(profileButton.getWidth());
                logoutMenuItem.setMinWidth(profileButton.getWidth());
                profileDrop.setTranslateX(profileButton.getLayoutX());
                profileDrop.setTranslateY(profileButton.getHeight());
                profileDrop.setVisible(true);
            }
        });

        profileButton.setOnMouseExited(event -> {
            hideDelay.playFromStart();
        });

        profileMenuItem.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                hideDelay.stop();
            } else {
                hideDelay.playFromStart();
            }
        });
        logoutMenuItem.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                hideDelay.stop();
            } else {
                hideDelay.playFromStart();
            }
        });


        profileButton.setOnAction(event -> {
            if (profileDrop.isVisible()) {
                profileDrop.setVisible(false);
            }
            navigateToProfile();
        });
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

        if (profileCircle != null) {
            profileCircle.setFill(imagePattern);
        }
    }

    private void navigateToProfile() {
        navigationService.navigateTo("ViewProfile", true);
    }

    @FXML
    private void handleBackButton() {
        navigationService.goBack();
    }

    @FXML
    private void handleJoinEventButton() {
        User activeUser = userService.getCurrentUser();

        if (activeUser == null) {
            System.err.println("Cannot join: No active user found.");
            return; // Exit if no user is logged in
        }

        if (joinEventButton.getText().contains("Unjoin") || joinEventButton.getText().contains("Joined")) {
            handleUnregister(activeUser);
            return;
        }

        if (joinEventButton.getText().equals("Join Event")) {

            boolean joined = userService.joinEvent(currentEvent, activeUser);

            if (joined) {
                refresh();
            } else {
                boolean isFull = currentEvent.getMaxAttendees() != null && currentEvent.getUsers().size() >= currentEvent.getMaxAttendees();

                if (isFull) {
                    joinEventButton.setText("Full");
                    joinEventButton.setDisable(true);
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to register for the event.");
                    errorAlert.showAndWait();
                }
            }
        }
    }

    /**
     * @brief Function to handle user unregistration from the event.
     * @param activeUser user who wants to unregister
     */
    private void handleUnregister(User activeUser) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm leaving event");
        alert.setHeaderText("Are you sure you want to leave \"" + currentEvent.getTitle() + "\"?");
        alert.setContentText("You can rejoin later if space is available.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean removed = userService.removeEventFromUser(currentEvent, activeUser);

                if (removed) {
                    System.out.println("User successfully left event: " + currentEvent.getTitle());
                    refresh();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to leave event. Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });
    }
}
