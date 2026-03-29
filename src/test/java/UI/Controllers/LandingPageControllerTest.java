package UI.Controllers;

import UI.Services.NavigationService;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;

class LandingPageControllerTest {

    private LandingPageController controller;
    private NavigationService mockNav;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new LandingPageController();
        mockNav = Mockito.mock(NavigationService.class);
        setSingleton(NavigationService.class, mockNav);
    }

    private void setSingleton(Class<?> clazz, Object mockInstance) throws Exception {
        Field instance = clazz.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, mockInstance);
    }

    @Test
    void testHandleGetStarted() throws Exception {
        Method method = LandingPageController.class.getDeclaredMethod("handleGetStarted");
        method.setAccessible(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);

        verify(mockNav).navigateTo("Register");
    }

    @Test
    void testHandleSignIn() throws Exception {
        Method method = LandingPageController.class.getDeclaredMethod("handleSignIn");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);

        verify(mockNav).navigateTo("Login");
    }
}