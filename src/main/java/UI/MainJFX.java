package UI;

import UI.Services.NavigationService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.io.IOException;

public class MainJFX extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        primaryStage.setTitle("Eventify");

        NavigationService navigationService = NavigationService.getInstance();
        navigationService.init(primaryStage);

        navigationService.addScene("LandingPage", "/FXMLs/LandingPage.fxml");
        navigationService.addScene("Register", "/FXMLs/RegisterScene.fxml");
        navigationService.addScene("Login", "/FXMLs/LoginScene.fxml");
        navigationService.addScene("EditProfile", "/FXMLs/EditProfileScene.fxml");
        navigationService.addScene("DiscoverEvents", "/FXMLs/DiscoverEventsScene.fxml");
        navigationService.addScene("EventDetails", "/FXMLs/EventDetails.fxml");
        navigationService.addScene("CreateEvent", "/FXMLs/CreateEventScene.fxml");
        navigationService.addScene("EditEvent", "/FXMLs/EditEventScene.fxml");
        navigationService.addScene("ViewProfile", "/FXMLs/ViewProfileScene.fxml");

        navigationService.navigateTo("LandingPage");

        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
