    package UI.Controllers;
    
    import Model.Entities.Event;
    import Model.Entities.User;
    import Model.Services.EventService;
    import Model.Services.UserService;
    import UI.Services.NavigationService;
    import javafx.event.EventHandler;
    import javafx.fxml.FXML;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.ButtonType;
    import javafx.scene.control.Label;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.shape.Rectangle;
    
    import java.io.File;
    import java.util.concurrent.atomic.AtomicBoolean;

    public class EventCardController {
    
        private Event event;
        private final NavigationService navigationService;
    
        @FXML
        private ImageView eventImageView;
    
        @FXML
        private Label titleLabel;
    
        @FXML
        private Label eventTypeLabel;
    
        @FXML
        private Label descriptionLabel;
    
        @FXML
        private Label dateLabel;

        @FXML
        private Label locationLabel;
    
        @FXML
        private Label priceLabel;
    
        @FXML
        private Label attendeesLabel;

        @FXML
        private Button joinButton;

        EventHandler<MouseEvent> mouseEnteredHandler = e -> joinButton.setText("Unjoin");
        EventHandler<MouseEvent> mouseExitedHandler = e -> joinButton.setText("Joined");


        public EventCardController() {
            this.navigationService = NavigationService.getInstance();
        }
    
        public void setEvent(Event event) {
            this.event = event;
    
            // Set image
            final Image defaultImage = new Image(getClass().getResourceAsStream("/images/missing-image.png"));
            String imagePath = event.getImageURL();
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
    
    
            titleLabel.setText(event.getTitle());
            eventTypeLabel.setText(event.getEventType());
            descriptionLabel.setText(event.getDescription());
            dateLabel.setText(event.getDate().toString());
            locationLabel.setText(event.getLocation());
            priceLabel.setText(event.getPrice() == 0 ? "Free" : String.format("%.2f€", event.getPrice()));
    
            String maxAttendeesText = (event.getMaxAttendees() == null || event.getMaxAttendees() == 0)
                    ? "Unknown"
                    : event.getMaxAttendees().toString();
            attendeesLabel.setText(event.getUsers().size() + "/" + maxAttendeesText + " attending");

            if(event.getUsers().contains(UserService.getInstance().getCurrentUser())) {
                joinButton.setText("Joined");
                joinButton.setOnMouseEntered(mouseEnteredHandler);
                joinButton.setOnMouseExited(mouseExitedHandler);
            } else {
                joinButton.setText("Join");
            }
        }

        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    
        @FXML
        public void initialize() {
            // Create a Rectangle to use as a clip for rounded corners
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(eventImageView.fitWidthProperty());
            clip.heightProperty().bind(eventImageView.fitHeightProperty());
    
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            eventImageView.setClip(clip);

        }
    
        @FXML
        private void viewDetails() {
            navigationService.navigateTo("EventDetails", event);
        }

        @FXML
        private void HandleJoinButton() {
        User currentUser = UserService.getInstance().getCurrentUser();
            if (event == null) {
                System.err.println("Attempted to join event, but event is null.");
                return;
            }

            if (joinButton.getText().equals("Unjoin") || joinButton.getText().equals("Joined")) {
                handleUnregister(currentUser);
                return;
            }


            // check if the user is already attending (just to avoid duplicates)
            if (event.getUsers().contains(currentUser)) {
                showAlert("Info", "You are already attending this event.");
                return;
            }

            // check for capacity limits
            if (event.getMaxAttendees() != null && event.getMaxAttendees() > 0 && event.getUsers().size() >= event.getMaxAttendees()) {
                showAlert("Capacity full", "Sorry, this event has reached its maximum capacity.");
                return;
            }
            
            boolean joined = EventService.getInstance().addUserToEvent(event.getId(), currentUser);

            if (joined) {
                // update the UI
                joinButton.setText("Joined");
                joinButton.setDisable(false);
                joinButton.setOnMouseEntered(mouseEnteredHandler);
                joinButton.setOnMouseExited(mouseExitedHandler);
            }
        }

        private void handleUnregister(User currentUser) {
            event.getUsers().remove(currentUser);


            joinButton.setOnMouseExited(null); // Disable mouse exit event
            joinButton.setOnMouseEntered(null); // Disable mouse enter event
            joinButton.getStyleClass().add("join-button-discover-page-hovered");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unjoin Event");
            alert.setHeaderText("Are you sure you want to unjoin this event?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Use the correct service method
                    EventService.getInstance().removeUserFromEvent(event.getId(), currentUser);

                    joinButton.setText("Join");
                    joinButton.setDisable(false);
                } else {
                    joinButton.setOnMouseEntered(mouseEnteredHandler);
                    joinButton.setOnMouseExited(mouseExitedHandler); // Re-enable mouse exit event
                    if (!joinButton.isHover()) { // If mouse is not over the button, reset text
                        joinButton.setText("Joined");
                    }
                }
                joinButton.getStyleClass().remove("join-button-discover-page-hovered");

            });
        }

    }
