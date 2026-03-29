package UI.Services;

import UI.Controllers.DiscoverEventsController;
import UI.Controllers.EventDetailsController;
import UI.Controllers.ViewProfileController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NavigationServiceTest {

    private NavigationService navigationService;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        Field instanceField = NavigationService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        navigationService = NavigationService.getInstance();
    }

    @Test
    void testSingleton() {
        NavigationService instance1 = NavigationService.getInstance();
        NavigationService instance2 = NavigationService.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAddScene() throws NoSuchFieldException, IllegalAccessException {
        navigationService.addScene("TestScene", "/path/to/fxml");

        Field fxmlPathsField = NavigationService.class.getDeclaredField("fxmlPaths");
        fxmlPathsField.setAccessible(true);
        var paths = (Map<String, String>) fxmlPathsField.get(navigationService);

        assertTrue(paths.containsKey("TestScene"));
        assertEquals("/path/to/fxml", paths.get("TestScene"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGoBackAndRefresh() throws Exception {
        // Manually inject history
        Field historyField = NavigationService.class.getDeclaredField("sceneHistory");
        historyField.setAccessible(true);
        List<String> sceneHistory = (List<String>) historyField.get(navigationService);
        sceneHistory.add("Root");
        sceneHistory.add("SecondPage");

        // Mock switchScene or let it fail gracefully
        try {
            navigationService.goBackAndRefresh();
        } catch (Exception e) {
            // ignore load error
        }

        assertEquals(1, sceneHistory.size());
        assertEquals("Root", sceneHistory.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNavigateToSameSceneIgnored() throws Exception {
        Field currentSceneField = NavigationService.class.getDeclaredField("currentSceneName");
        currentSceneField.setAccessible(true);
        currentSceneField.set(navigationService, "Home");

        Field historyField = NavigationService.class.getDeclaredField("sceneHistory");
        historyField.setAccessible(true);
        List<String> history = (List<String>) historyField.get(navigationService);

        navigationService.navigateTo("Home");

        assertTrue(history.isEmpty(), "Should not add to history if navigating to current scene");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHistoryLogic_GoBackProtection() throws Exception {
        // Manually inject history
        Field historyField = NavigationService.class.getDeclaredField("sceneHistory");
        historyField.setAccessible(true);
        List<String> sceneHistory = (List<String>) historyField.get(navigationService);

        sceneHistory.add("Root"); // Only 1 item

        navigationService.goBack();

        assertEquals(1, sceneHistory.size(), "Should not pop if size <= 1");
        assertEquals("Root", sceneHistory.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGoBack_Success() throws Exception {
        // Manually inject history
        Field historyField = NavigationService.class.getDeclaredField("sceneHistory");
        historyField.setAccessible(true);
        List<String> sceneHistory = (List<String>) historyField.get(navigationService);
        sceneHistory.add("Root");
        sceneHistory.add("SecondPage");

        // Inject fxml path so switchScene doesn't crash early (though load will fail without file)
        // But we assume load fails in unit test environment, we just check history manipulation logic
        // OR we mock switchScene if possible (hard with private method).
        // Instead, let's verify the state changes on the list.

        // Since switchScene calls FXMLLoader, it will likely fail and print stacktrace,
        // but the history removal happens BEFORE switchScene.
        try {
            navigationService.goBack();
        } catch (Exception e) {
            // Ignore load exception
        }

        // Verify history was popped
        assertEquals(1, sceneHistory.size());
        assertEquals("Root", sceneHistory.get(0));
    }

    @Test
    void testSetGetData() throws Exception {
        String expectedData = "Test Data";

        // Test via public API wrapper
        navigationService.navigateTo("SomeScene", expectedData);

        assertEquals(expectedData, navigationService.getData());
    }

    @Test
    void testRefreshControllerLogic() throws Exception {
        // Mock Controllers
        DiscoverEventsController mockDiscover = Mockito.mock(DiscoverEventsController.class);
        ViewProfileController mockProfile = Mockito.mock(ViewProfileController.class);
        EventDetailsController mockDetails = Mockito.mock(EventDetailsController.class);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Scene sceneDiscover = new Scene(new StackPane());
                sceneDiscover.setUserData(mockDiscover);

                Scene sceneProfile = new Scene(new StackPane());
                sceneProfile.setUserData(mockProfile);

                Scene sceneDetails = new Scene(new StackPane());
                sceneDetails.setUserData(mockDetails);

                Method refreshMethod = NavigationService.class.getDeclaredMethod("refreshController", Scene.class);
                refreshMethod.setAccessible(true);

                refreshMethod.invoke(navigationService, sceneDiscover);
                refreshMethod.invoke(navigationService, sceneProfile);
                refreshMethod.invoke(navigationService, sceneDetails);

                Mockito.verify(mockDiscover).refreshEvents();
                Mockito.verify(mockProfile).refreshProfileContent();
                Mockito.verify(mockDetails).refresh();

            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception during JavaFX operations: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void testSwitchScene_CacheHit() throws Exception {
        // Test the caching branches in switchScene
        Field discoverSceneField = NavigationService.class.getDeclaredField("discoverEventsScene");
        discoverSceneField.setAccessible(true);
        Scene mockScene = Mockito.mock(Scene.class);
        discoverSceneField.set(navigationService, mockScene);

        // Mock controller for the refresh call
        DiscoverEventsController mockController = Mockito.mock(DiscoverEventsController.class);
        Mockito.when(mockScene.getUserData()).thenReturn(mockController);

        // We also need to mock primaryStage because switchScene calls setScene on it
        Field stageField = NavigationService.class.getDeclaredField("primaryStage");
        stageField.setAccessible(true);
        javafx.stage.Stage mockStage = Mockito.mock(javafx.stage.Stage.class);
        stageField.set(navigationService, mockStage);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Method switchMethod = NavigationService.class.getDeclaredMethod("switchScene", String.class, boolean.class);
                switchMethod.setAccessible(true);

                // Call with "DiscoverEvents" to hit the cache
                switchMethod.invoke(navigationService, "DiscoverEvents", true);

                // Verify stage.setScene was called with our cached scene
                Mockito.verify(mockStage).setScene(mockScene);
                // Verify refresh was called
                Mockito.verify(mockController).refreshEvents();

            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}