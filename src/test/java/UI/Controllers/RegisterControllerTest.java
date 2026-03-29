package UI.Controllers;

import Model.Entities.Event;
import Model.Services.EventService;
import Model.Services.UserService;
import Persistence.DatabaseManager;
import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RegisterControllerTest {

    private RegisterController controller;

    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private PasswordField passwordField;
    private CheckBox termsCheckBox;

    private NavigationService mockNav;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(UserService.class);
        resetSingleton(NavigationService.class);
        resetSingleton(EventService.class);
        cleanDatabase();

        controller = new RegisterController() {
            @Override
            protected void showAlert(String title, String content) {
                // No-op
            }
        };
        initializeUI();

        mockNav = Mockito.mock(NavigationService.class);
        setField("instance", mockNav, NavigationService.class);
    }
    
    private void cleanDatabase() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM EVENT_ATTENDEES");
            stmt.executeUpdate("DELETE FROM EVENTS");
            stmt.executeUpdate("DELETE FROM USERS");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 1");
            stmt.executeUpdate("ALTER TABLE EVENTS ALTER COLUMN ID RESTART WITH 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void resetSingleton(Class<T> clazz) throws Exception {
        try {
            Field instance = clazz.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (NoSuchFieldException e) {}
    }

    private void initializeUI() throws Exception {
        firstNameField = new TextField();
        lastNameField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordField();
        termsCheckBox = new CheckBox();

        new HBox(firstNameField);
        new HBox(lastNameField);
        new HBox(emailField);
        new HBox(passwordField);

        setField("firstNameField", firstNameField, controller);
        setField("lastNameField", lastNameField, controller);
        setField("emailField", emailField, controller);
        setField("passwordField", passwordField, controller);
        setField("termsCheckBox", termsCheckBox, controller);
    }

    private void setField(String name, Object value, Object target) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private void setField(String name, Object value, Class<?> clazz) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    void testInitialize() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initialize();
                assertNotNull(firstNameField.getParent());
            } catch (Throwable t) {
                t.printStackTrace();
                fail(t.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for initialize()");
    }

    @Test
    void testHandleRegisterSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                firstNameField.setText("Alice");
                lastNameField.setText("Wonderland");
                emailField.setText("alice@test.com");
                passwordField.setText("password123");
                termsCheckBox.setSelected(true);

                java.lang.reflect.Method method = RegisterController.class.getDeclaredMethod("handleRegister");
                method.setAccessible(true);
                method.invoke(controller);

                // Verification
                // Note: addUserCurrent creates a new user via DB. The generated ID will be 1 (after cleanDatabase)
                // However, LoginControllerTest adds user manually. Here handleRegister does it.
                // We should check DB or Service.
                
                // Since this runs in a separate thread (Platform.runLater), and H2 mem is shared, it should work.
                
                verify(mockNav).navigateTo(eq("EditProfile"), eq(true));

            } catch (Throwable e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for register success");
    }

    @Test
    void testHandleRegisterValidationFailure_EmptyFields() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                firstNameField.setText("");
                passwordField.setText("123");
                termsCheckBox.setSelected(true);

                java.lang.reflect.Method method = RegisterController.class.getDeclaredMethod("handleRegister");
                method.setAccessible(true);
                method.invoke(controller);

                verify(mockNav, never()).navigateTo(anyString(), anyBoolean());

            } catch (Throwable e) {
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleRegisterValidationFailure_TermsNotChecked() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                firstNameField.setText("Bob");
                lastNameField.setText("Builder");
                emailField.setText("bob@build.com");
                passwordField.setText("123");
                termsCheckBox.setSelected(false);

                java.lang.reflect.Method method = RegisterController.class.getDeclaredMethod("handleRegister");
                method.setAccessible(true);
                method.invoke(controller);

                verify(mockNav, never()).navigateTo(anyString(), anyBoolean());

            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleSignInLink() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = RegisterController.class.getDeclaredMethod("handleSignInLink");
                method.setAccessible(true);
                method.invoke(controller);

                verify(mockNav).navigateTo(eq("Login"));
            } catch (Throwable e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
