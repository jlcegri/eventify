package UI.Controllers;

import Model.Assistant.EventSearchOrchestrator;
import Model.Entities.Event;
import Model.Services.EventService;
import Model.Services.UserService;
import Persistence.DatabaseManager;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.RangeSlider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoverEventsControllerTest {

    private DiscoverEventsController controller;

    // UI Fields
    private TextField searchTextField;
    private CheckComboBox<String> selectCity;
    private CheckComboBox<String> selectEventType;
    private CheckComboBox<String> selectInterests;
    private DatePicker startingDate;
    private DatePicker endingDate;
    private RangeSlider priceRangeSlider;
    private RangeSlider currentAttendeesSlider;
    private RangeSlider maxAttendeesSlider;
    private FlowPane eventCardsFilteredSearch;
    private VBox complexSearch;
    private Label noEventsLabel;
    private VBox filtersModal;
    private RowConstraints filtersRow;
    private ScrollPane scrollRecommended, scrollPopular, scrollNear;
    private FlowPane flowRecommended, flowPopular, flowNear;
    private Button prevRecommended, nextRecommended, prevPopular, nextPopular, prevNear, nextNear;
    private EventSearchOrchestrator mockOrchestrator;

    @BeforeAll
    static void initJFX() {
        new JFXPanel();
        Platform.setImplicitExit(false);
        DatabaseManager.initTestDatabase();
    }

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EventService.class);
        resetSingleton(UserService.class);
        
        cleanDatabase();
        createTestUsers();

        EventService.getInstance().addEvent(new Event(0, 1, "Java Workshop", "Learn Java", LocalDate.now().plusDays(1), LocalTime.of(10,0), LocalTime.of(12,0), "Library", "Lisbon", "Workshops", List.of("Technology"), 50, "", 0.0));
        EventService.getInstance().addEvent(new Event(0, 1, "Music Fest", "Fun music", LocalDate.now().plusDays(5), LocalTime.of(20,0), LocalTime.of(23,0), "Park", "Porto", "Concerts", List.of("Music"), 500, "", 50.0));
        EventService.getInstance().addEvent(new Event(0, 2, "Expensive Gala", "Fancy", LocalDate.now().plusDays(10), LocalTime.of(19,0), LocalTime.of(22,0), "Hotel", "Lisbon", "Parties", List.of("Social"), 100, "", 200.0));

        controller = new DiscoverEventsController();
        initializeUIFields();

        mockOrchestrator = mock(EventSearchOrchestrator.class);
        when(mockOrchestrator.searchAndMapEvents(anyString())).thenReturn(Collections.emptyList());
        setField("llmSearchOrchestrator", mockOrchestrator);
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
    
    private void createTestUsers() {
        try (java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (1, 'User', 'One', 'user1@test.com', 'pass')");
            stmt.executeUpdate("INSERT INTO USERS (id, firstName, lastName, email, password) VALUES (2, 'User', 'Two', 'user2@test.com', 'pass')");
            stmt.executeUpdate("ALTER TABLE USERS ALTER COLUMN ID RESTART WITH 3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void resetSingleton(Class<T> clazz) throws Exception {
        Field instance = clazz.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    private void initializeUIFields() throws Exception {
        searchTextField = new TextField();
        selectCity = new CheckComboBox<>();
        selectEventType = new CheckComboBox<>();
        selectInterests = new CheckComboBox<>();
        startingDate = new DatePicker();
        endingDate = new DatePicker();
        priceRangeSlider = new RangeSlider(0, 1000, 0, 1000);
        currentAttendeesSlider = new RangeSlider(0, 1000, 0, 1000);
        maxAttendeesSlider = new RangeSlider(0, 1000, 0, 1000);

        eventCardsFilteredSearch = new FlowPane();
        complexSearch = new VBox();
        noEventsLabel = new Label();
        filtersModal = new VBox();
        filtersRow = new RowConstraints();

        scrollRecommended = new ScrollPane(); scrollPopular = new ScrollPane(); scrollNear = new ScrollPane();
        flowRecommended = new FlowPane(); flowPopular = new FlowPane(); flowNear = new FlowPane();
        prevRecommended = new Button(); nextRecommended = new Button();
        prevPopular = new Button(); nextPopular = new Button();
        prevNear = new Button(); nextNear = new Button();

        setField("searchTextField", searchTextField);
        setField("selectCity", selectCity);
        setField("selectEventType", selectEventType);
        setField("selectInterests", selectInterests);
        setField("startingDate", startingDate);
        setField("endingDate", endingDate);
        setField("priceRangeSlider", priceRangeSlider);
        setField("currentAttendeesSlider", currentAttendeesSlider);
        setField("maxAttendeesSlider", maxAttendeesSlider);
        setField("eventCardsFilteredSearch", eventCardsFilteredSearch);
        setField("complexSearch", complexSearch);
        setField("noEventsLabel", noEventsLabel);
        setField("filtersModal", filtersModal);
        setField("filtersRow", filtersRow);

        setField("priceRangeLow", new TextField()); setField("priceRangeHigh", new TextField());
        setField("currentAttendeesLow", new TextField()); setField("currentAttendeesHigh", new TextField());
        setField("maxAttendeesLow", new TextField()); setField("maxAttendeesHigh", new TextField());

        setField("scrollRecommended", scrollRecommended); setField("flowRecommended", flowRecommended);
        setField("prevRecommended", prevRecommended); setField("nextRecommended", nextRecommended);
        setField("scrollPopular", scrollPopular); setField("flowPopular", flowPopular);
        setField("prevPopular", prevPopular); setField("nextPopular", nextPopular);
        setField("scrollNear", scrollNear); setField("flowNear", flowNear);
        setField("prevNear", prevNear); setField("nextNear", nextNear);

        setField("profileButton", new Button());
        setField("profileStack", new javafx.scene.layout.StackPane());
        setField("profileCircle", new javafx.scene.shape.Circle());
        
        // IMPORTANT: Ensure ScrollPane has content or parent to avoid NPEs in controller
        StackPane recommendedStack = new StackPane(scrollRecommended);
        StackPane popularStack = new StackPane(scrollPopular);
        StackPane nearStack = new StackPane(scrollNear);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            complexSearch.setVisible(true);
            eventCardsFilteredSearch.setVisible(false);
            latch.countDown();
        });
        latch.await(2, TimeUnit.SECONDS);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Class<?> clazz = controller.getClass();
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
            field.set(controller, value);
        }
    }

    @Test
    void testSearchByText() {
        List<Event> results = controller.searchEvents("Java", List.of(), List.of(), List.of(), null, null);
        assertEquals(1, results.size());
        assertEquals("Java Workshop", results.get(0).getTitle());
    }

    @Test
    void testSearchByCity() {
        List<Event> results = controller.searchEvents("", List.of("Lisbon"), List.of(), List.of(), null, null);
        assertEquals(2, results.size());
    }

    @Test
    void testSearchByPriceRange() {
        priceRangeSlider.setLowValue(40);
        priceRangeSlider.setHighValue(60);
        List<Event> results = controller.searchEvents("", List.of(), List.of(), List.of(), null, null);
        assertEquals(1, results.size());
        assertEquals("Music Fest", results.get(0).getTitle());
    }

    @Test
    void testSearchByDate() {
        List<Event> results = controller.searchEvents("", List.of(), List.of(), List.of(), LocalDate.now().plusDays(2).atStartOfDay(), null);
        assertEquals(2, results.size());
    }

    @Test
    void testSearchNoResults() {
        List<Event> results = controller.searchEvents("NonExistent", List.of(), List.of(), List.of(), null, null);
        assertTrue(results.isEmpty());
    }

    @Test
    void testToggleFiltersModal() throws InterruptedException {
        Platform.runLater(() -> {
            filtersModal.setVisible(false);
            filtersRow.setPrefHeight(0);
        });

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.toggleFiltersModal();
            assertTrue(filtersModal.isVisible());
            assertEquals(150, filtersRow.getPrefHeight(), 1.0);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.toggleFiltersModal();
            assertFalse(filtersModal.isVisible());
            assertEquals(0, filtersRow.getPrefHeight(), 1.0);
            latch2.countDown();
        });
        assertTrue(latch2.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testInitializeFiltersModal() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initializeFiltersModal();
            assertFalse(selectCity.getItems().isEmpty());
            assertEquals("All Cities", selectCity.getCheckModel().getCheckedItems().get(0));
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testInitializeEventCarousels() throws Exception {
        Method method = DiscoverEventsController.class.getDeclaredMethod("initializeEventCarousels");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                eventCardsFilteredSearch.setVisible(true);
                method.invoke(controller);
                assertTrue(complexSearch.isVisible());
                assertFalse(eventCardsFilteredSearch.isVisible());
                assertFalse(flowPopular.getChildren().isEmpty());
            } catch (Exception e) {
                fail(e);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testHandleSearchOrFilterChange() throws Exception {
        Method method = DiscoverEventsController.class.getDeclaredMethod("handleSearchOrFilterChange");
        method.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            searchTextField.setText("Java");
            try {
                method.invoke(controller);
                assertFalse(complexSearch.isVisible());
            } catch (Exception e) { fail(e); }

            searchTextField.setText("");
            try {
                method.invoke(controller);
                assertTrue(complexSearch.isVisible());
            } catch (Exception e) { fail(e); }

            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testBuildHybridQuery() throws Exception {
        Method method = DiscoverEventsController.class.getDeclaredMethod("buildHybridQuery", String.class, List.class, List.class, List.class, LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);

        String searchTerm = "Concert";
        List<String> cities = new ArrayList<>(); cities.add("Lisbon");
        List<String> types = new ArrayList<>(); types.add("Music");
        List<String> interests = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        // Ensure UI updates happen on FX thread if necessary, though simpler set should work
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            priceRangeSlider.setLowValue(10);
            priceRangeSlider.setHighValue(100);
            latch.countDown();
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        String query = (String) method.invoke(controller, searchTerm, cities, types, interests, start, end);
        
        // Debug output
        System.out.println("Generated Hybrid Query: " + query);

        assertNotNull(query);
        assertTrue(query.contains("Concert"), "Query should contain 'Concert'. Got: " + query);
        assertTrue(query.contains("Lisbon"), "Query should contain 'Lisbon'. Got: " + query);
        assertTrue(query.contains("Music"), "Query should contain 'Music'. Got: " + query);
        // Updated assertion to match the actual implementation in DiscoverEventsController
        assertTrue(query.contains("with prices between 10 and 100"), "Query should contain 'with prices between 10 and 100'. Got: " + query);
    }
}
