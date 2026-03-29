package UI.Services;

import UI.Controllers.DiscoverEventsController;
import UI.Controllers.EventCreationController;
import UI.Controllers.EventDetailsController;
import UI.Controllers.ViewProfileController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationService {
    private static NavigationService instance;
    private Stage primaryStage;
    private final Map<String, String> fxmlPaths = new HashMap<>();
    private Object data;

    // Scene cache
    private Scene discoverEventsScene;
    private Scene viewProfileScene;
    private Scene eventDetailsScene;
    // Navigation history
    private final List<String> sceneHistory = new ArrayList<>();
    private String currentSceneName;

    protected NavigationService() {}

    public static synchronized NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void init(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void addScene(String name, String fxmlPath) {
        fxmlPaths.put(name, fxmlPath);
    }

    public void navigateTo(String sceneName, Object data) {
        this.data = data;
        navigateTo(sceneName, true);
    }

    public void navigateTo(String sceneName) {
        navigateTo(sceneName, false);
    }

    public void navigateTo(String destinationSceneName, boolean refresh) {
        if (destinationSceneName.equals(currentSceneName)) {
            return; // Avoid re-navigating to the same scene
        }

        // Cache the scene we are leaving, if it's a cachable one.
        if (currentSceneName != null) {
            if ("DiscoverEvents".equals(currentSceneName)) {
                this.discoverEventsScene = primaryStage.getScene();
            } else if ("ViewProfile".equals(currentSceneName)) {
                this.viewProfileScene = primaryStage.getScene();
            } else if ("EventDetails".equals(currentSceneName)) {
                this.eventDetailsScene = primaryStage.getScene();
            }
        }

        // Update history and current scene tracker
        this.currentSceneName = destinationSceneName;
        this.sceneHistory.add(destinationSceneName);

        // Perform the actual scene switch
        switchScene(destinationSceneName, refresh);
    }

    public void goBack() {
        if (sceneHistory.size() <= 1) {
            return; // Cannot go back from the root scene
        }

        // Remove the current scene from history
        sceneHistory.remove(sceneHistory.size() - 1);

        // The new destination is the scene that is now at the end of the list
        String destinationSceneName = sceneHistory.get(sceneHistory.size() - 1);

        // Update the current scene tracker
        this.currentSceneName = destinationSceneName;

        // Switch to that scene, preventing a refresh on the way back
        switchScene(destinationSceneName, false);
    }

    public void goBackAndRefresh() {
        if (sceneHistory.size() <= 1) {
            return; // Cannot go back from the root scene
        }

        // Remove the current scene from history
        sceneHistory.remove(sceneHistory.size() - 1);

        // The new destination is the scene that is now at the end of the list
        String destinationSceneName = sceneHistory.get(sceneHistory.size() - 1);

        // Update the current scene tracker
        this.currentSceneName = destinationSceneName;

        // Switch to that scene, and force a refresh
        switchScene(destinationSceneName, true);
    }

    private void switchScene(String sceneName, boolean refresh) {
        // 1. Attempt to load from cache
        if ("DiscoverEvents".equals(sceneName) && discoverEventsScene != null) {
            primaryStage.setScene(discoverEventsScene);
            if (refresh) {
                refreshController(discoverEventsScene);
            }
            return;
        }
        if ("ViewProfile".equals(sceneName) && viewProfileScene != null) {
            primaryStage.setScene(viewProfileScene);
            if (refresh) {
                refreshController(viewProfileScene);
            }
            return;
        }
        if ("EventDetails".equals(sceneName) && eventDetailsScene != null) {
            primaryStage.setScene(eventDetailsScene);
            if (refresh) {
                refreshController(eventDetailsScene);
            }
            return;
        }

        // 2. If not in cache, load from FXML
        try {
            String fxmlPath = fxmlPaths.get(sceneName);
            if (fxmlPath == null) {
                System.err.println("No FXML path found for scene: " + sceneName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene newScene = new Scene(root);
            newScene.setUserData(loader.getController()); // Store controller for refreshes

            primaryStage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshController(Scene scene) {
        Object controller = scene.getUserData();
        if (controller instanceof DiscoverEventsController) {
            ((DiscoverEventsController) controller).refreshEvents();
        } else if (controller instanceof ViewProfileController) {
            ((ViewProfileController) controller).refreshProfileContent();
        } else if (controller instanceof EventDetailsController) {
            ((EventDetailsController) controller).refresh();
        }
    }

    public Object getData() {
        return data;
    }
}
