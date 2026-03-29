package UI.Controllers;

import Model.Entities.User;
import Model.Services.UserService;
import UI.Services.NavigationService;
import Persistence.DatabaseManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class EditProfileControllerTest {

    private EditProfileController controller;

    // UI Components
    private TextField locArea;
    private GridPane gridOpt;
    private FlowPane interestsOptions, eventTypes;
    private TextArea bioArea;
    private Label selectedLabel, continueButtonLabel, backButtonLabel, progressID0, titleEditP, subtitleEditP;
    private Button continueButton, backButton;
    private Circle progressID1, progressID2, progressID3, progressID4;
    private SVGPath svgEditP;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        createUser();

        Field instance = UserService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
        
        UserService.getInstance().setCurrentUser(new User(1, "Test", "test@test.com", "pass", "Loc", null, null, ""));

        controller = new EditProfileController();
        initializeUI();
    }
    
    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createUser() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (1, 'Test', 'User', 'test@test.com', 'pass')");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() throws Exception {
        locArea = new TextField();
        gridOpt = new GridPane();
        gridOpt.getRowConstraints().add(new RowConstraints());
        gridOpt.getRowConstraints().add(new RowConstraints());

        interestsOptions = new FlowPane();
        eventTypes = new FlowPane();
        bioArea = new TextArea();
        selectedLabel = new Label();
        continueButtonLabel = new Label();
        backButtonLabel = new Label();
        progressID0 = new Label();
        titleEditP = new Label();
        subtitleEditP = new Label();
        continueButton = new Button();
        backButton = new Button();
        progressID1 = new Circle(); progressID2 = new Circle();
        progressID3 = new Circle(); progressID4 = new Circle();
        svgEditP = new SVGPath();

        setField("locArea", locArea);
        setField("gridOpt", gridOpt);
        setField("interestsOptions", interestsOptions);
        setField("eventTypes", eventTypes);
        setField("bioArea", bioArea);
        setField("selectedLabel", selectedLabel);
        setField("continueButtonLabel", continueButtonLabel);
        setField("backButtonLabel", backButtonLabel);
        setField("progressID0", progressID0);
        setField("titleEditP", titleEditP);
        setField("subtitleEditP", subtitleEditP);
        setField("continueButton", continueButton);
        setField("backButton", backButton);
        setField("progressID1", progressID1);
        setField("progressID2", progressID2);
        setField("progressID3", progressID3);
        setField("progressID4", progressID4);
        setField("svgEditP", svgEditP);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = EditProfileController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void testInitializeStep1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            assertTrue(locArea.isVisible());
            assertFalse(interestsOptions.isVisible());
            assertEquals("1 ", progressID0.getText());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testNavigationToStep2() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();
            controller.step2();

            assertFalse(locArea.isVisible());
            assertTrue(interestsOptions.isVisible());
            assertEquals("2 ", progressID0.getText());
            assertTrue(backButton.isVisible());
            assertFalse(interestsOptions.getChildren().isEmpty());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testNavigationToStep3() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.step3();
            assertTrue(eventTypes.isVisible());
            assertEquals("3 ", progressID0.getText());
            assertFalse(eventTypes.getChildren().isEmpty());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testNavigationToStep4() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.step4();
            assertTrue(bioArea.isVisible());
            assertEquals("4 ", progressID0.getText());
            assertEquals("Complete setup", continueButtonLabel.getText());
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStepForwardLogic() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            progressID0.setText("1 ");
            controller.stepForward(new ActionEvent());
            assertEquals("2 ", progressID0.getText());

            controller.stepForward(new ActionEvent());
            assertEquals("3 ", progressID0.getText());

            controller.stepForward(new ActionEvent());
            assertEquals("4 ", progressID0.getText());

            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStepBackwardLogic() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            progressID0.setText("4 ");
            controller.stepBackward(new ActionEvent());
            assertEquals("3 ", progressID0.getText());

            controller.stepBackward(new ActionEvent());
            assertEquals("2 ", progressID0.getText());

            controller.stepBackward(new ActionEvent());
            assertEquals("1 ", progressID0.getText());

            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleInteractionToggle() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ToggleButton interestBtn = new ToggleButton();
            interestBtn.getStyleClass().add("interestLabel");

            interestBtn.setSelected(true);
            controller.handleInteractionToggle(new ActionEvent(interestBtn, null));
            assertTrue(selectedLabel.getText().contains("1 interests"));

            interestBtn.setSelected(false);
            controller.handleInteractionToggle(new ActionEvent(interestBtn, null));
            assertTrue(selectedLabel.getText().contains("0 interests"));

            ToggleButton eventBtn = new ToggleButton();
            eventBtn.getStyleClass().add("eventLabel");

            eventBtn.setSelected(true);
            controller.handleInteractionToggle(new ActionEvent(eventBtn, null));
            assertTrue(selectedLabel.getText().contains("1 events"));

            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleInteractionTArea() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            bioArea.setText("Hello");
            KeyEvent keyEvent = new KeyEvent(bioArea, bioArea, KeyEvent.KEY_TYPED, "o", "o", KeyCode.O, false, false, false, false);
            controller.handleInteractionTArea(keyEvent);
            assertTrue(selectedLabel.getText().contains("5 characters"));
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleComplete() throws Exception {
        NavigationService mockNav = Mockito.mock(NavigationService.class);
        Field navField = EditProfileController.class.getDeclaredField("navigationService");
        navField.setAccessible(true);
        navField.set(controller, mockNav);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                locArea.setText("Porto");
                bioArea.setText("My Bio");

                ToggleButton t1 = new ToggleButton();
                t1.setSelected(true); t1.setGraphic(new Label("Music"));
                interestsOptions.getChildren().add(t1);

                ToggleButton t2 = new ToggleButton();
                t2.setSelected(true); t2.setGraphic(new Label("Concerts"));
                eventTypes.getChildren().add(t2);

                controller.handleComplete();

                User currentUser = UserService.getInstance().getCurrentUser();
                assertNotNull(currentUser);
                assertEquals("Porto", currentUser.getLocation());
                assertEquals("My Bio", currentUser.getBio());

                verify(mockNav).navigateTo(eq("DiscoverEvents"));
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}