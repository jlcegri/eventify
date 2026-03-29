package UI.Controllers;

import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import Persistence.DatabaseManager;
import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class LoginControllerTest {

    private LoginController controller;
    private TextField emailField;
    private PasswordField passwordField;
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

        controller = new LoginController() {
            @Override
            protected void showAlert(String title, String content) {
                // Do nothing
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
        emailField = new TextField();
        passwordField = new PasswordField();
        new HBox(emailField);
        new HBox(passwordField);

        setField("emailField", emailField, controller);
        setField("passwordField", passwordField, controller);
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
        throw new NoSuchFieldException("Field '" + name + "' not found in class hierarchy of " + target.getClass().getName());
    }

    private void setField(String name, Object value, Class<?> clazz) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    void testHandleLoginSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                // 1. Pre-register a user
                User existingUser = new User(1, "Test User", "test@login.com", "pass123", "", new ArrayList<>(), new ArrayList<>(), "");
                existingUser.setFirstEntry(false);
                UserService.getInstance().addUserCurrent(existingUser);

                // 2. Set fields
                emailField.setText("test@login.com");
                passwordField.setText("pass123");

                // 3. Invoke Login
                java.lang.reflect.Method method = LoginController.class.getDeclaredMethod("handleLogin");
                method.setAccessible(true);
                method.invoke(controller);

                // 4. Verify
                User currentUser = UserService.getInstance().getCurrentUser();
                assertNotNull(currentUser);
                assertEquals("test@login.com", currentUser.getEmail());
                verify(mockNav).navigateTo(eq("DiscoverEvents"), eq(true));

            } catch (Throwable e) {
                e.printStackTrace();
                fail(e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleLoginFailure_WrongPassword() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                User existingUser = new User(1, "Test User", "test@login.com", "pass123", "", new ArrayList<>(), new ArrayList<>(), "");
                UserService.getInstance().addUserCurrent(existingUser);

                emailField.setText("test@login.com");
                passwordField.setText("wrongpassword");

                java.lang.reflect.Method method = LoginController.class.getDeclaredMethod("handleLogin");
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
    void testHandleLoginFailure_EmptyFields() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                emailField.setText("");
                passwordField.setText("pass123");

                java.lang.reflect.Method method = LoginController.class.getDeclaredMethod("handleLogin");
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
    void testHandleJoinLink() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = LoginController.class.getDeclaredMethod("handleJoinLink");
                method.setAccessible(true);
                method.invoke(controller);

                verify(mockNav).navigateTo(eq("Register"));
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
