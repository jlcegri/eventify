package UI.Controllers;

import Model.Entities.Event;
import Model.Entities.User;
import Model.Services.EventService;
import Model.Services.UserService;
import Model.Assistant.EventSearchService;
import Model.Assistant.EventSearchOrchestrator;
import Model.Assistant.LangChain4jSetup;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import UI.Services.NavigationService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.css.StyleClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.RangeSlider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @brief Controller class for the Discover Events view, handling search, filters,
 * and displaying event cards in both filtered list and carousel sections.
 */
public class DiscoverEventsController {

    private final NavigationService navigationService;
    private final EventService eventService;
    private final UserService userService;
    private EventSearchOrchestrator llmSearchOrchestrator;
    private List<Event> cachedLlmEvents = new ArrayList<>();
    private Timeline scrollAnimation;

    // Executor for background tasks to prevent UI freezing and Rate Limiting
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // Ensure threads don't prevent app shutdown
        return t;
    });

    // --- FXML Bindings - General Search/Filters ---
    @FXML
    private FlowPane eventCardsFilteredSearch; // Used only for search/filter results
    @FXML
    private TextField searchTextField;
    @FXML
    private RowConstraints filtersRow;
    @FXML
    private VBox filtersModal;
    @FXML
    private Label noEventsLabel;
    @FXML
    private Button createEventButton;
    @FXML
    private Button searchButton;
    @FXML
    private StackPane profileStack;
    @FXML
    private Button profileButton;
    @FXML
    private Circle profileCircle;

    // --- FXML Bindings - Filters Controls (kept for compatibility) ---
    @FXML
    private CheckComboBox<String> selectCity;
    @FXML
    private CheckComboBox<String> selectEventType;
    @FXML
    private CheckComboBox<String> selectInterests;
    @FXML
    private DatePicker startingDate;
    @FXML
    private DatePicker endingDate;
    @FXML
    private RangeSlider priceRangeSlider;
    @FXML
    private RangeSlider currentAttendeesSlider;
    @FXML
    private RangeSlider maxAttendeesSlider;
    @FXML
    private TextField priceRangeLow;
    @FXML
    private TextField priceRangeHigh;
    @FXML
    private TextField currentAttendeesLow;
    @FXML
    private TextField currentAttendeesHigh;
    @FXML
    private TextField maxAttendeesLow;
    @FXML
    private TextField maxAttendeesHigh;

    // --- FXML Bindings - New Carousel Structure ---
    @FXML
    private VBox complexSearch; // Main container for all carousels (recommended, popular, near)

    // Recommended Events Carousel
    @FXML
    private AnchorPane anchorRecommended;
    @FXML
    private ScrollPane scrollRecommended;
    @FXML
    private FlowPane flowRecommended; // The flow pane inside the recommended scroll
    @FXML
    private Button prevRecommended;
    @FXML
    private Button nextRecommended;

    // Popular Events Carousel
    @FXML
    private ScrollPane scrollPopular;
    @FXML
    private FlowPane flowPopular;
    @FXML
    private Button prevPopular;
    @FXML
    private Button nextPopular;

    // Near You Events Carousel
    @FXML
    private ScrollPane scrollNear;
    @FXML
    private FlowPane flowNear;
    @FXML
    private Button prevNear;
    @FXML
    private Button nextNear;


    // --- Data Sources (Mock/Predefined) ---
    private final List<String> interests = List.of("Music", "Sports", "Technology", "Art", "Food & Drink", "Business", "Health & Wellness", "Travel", "Photography",
            "Gaming", "Books", "Fashion", "Dance", "Comedy", "Film", "Fitness", "Outdoor Adventures");

    private final List<String> events = List.of("Concerts", "Workshops", "Networking", "Parties", "Conferences", "Sports Events", "Art Exhibitions", "Food Festivals",
            "Markets", "Meetups", "Classes", "Volunteer Work");


    /**
     * @brief Private constructor to prevent direct instantiation.
     * Initializes the Service instances.
     */
    public DiscoverEventsController() {
        this.eventService = EventService.getInstance();
        this.navigationService = NavigationService.getInstance();
        this.userService = UserService.getInstance();

        // Initialize LLM Search Service
        ContentRetriever retriever = LangChain4jSetup.createHybridRetriever();
        if (retriever != null) {
            EventSearchService llmSearchService = EventSearchService.create(retriever);
            this.llmSearchOrchestrator = new EventSearchOrchestrator(llmSearchService, this.eventService);
        } else {
            System.err.println("WARNING: LLM Web Search not initialized. Check API Key and configuration.");
        }
    }

    @FXML
    public void initialize() {
        initializeFiltersModal();
        refreshEvents();
        setupProfileMenu();

        // Assign the search action to the search button
        if (searchButton != null) {
            searchButton.setOnAction(e -> onSearchButtonClicked());
        }
    }

    @FXML
    private void onSearchButtonClicked() {
        handleSearchOrFilterChange();
    }

    public void refreshEvents() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            loadProfileImage(currentUser.getProfileImagePath());
            profileButton.setText(currentUser.getName());
        }
        initializeEventCarousels();
    }

    /**
     * @brief Initializes all event carousels (Recommended, Popular, Near You)
     * by launching asynchronous searches and setting up the UI.
     */
    private void initializeEventCarousels() {
        // UI Reset
        eventCardsFilteredSearch.getChildren().clear();
        eventCardsFilteredSearch.setVisible(false);
        if (noEventsLabel != null) noEventsLabel.setVisible(false);
        complexSearch.setVisible(true);

        // Get User Data
        User currentUser = userService.getCurrentUser();
        String city = (currentUser != null && currentUser.getLocation() != null && !currentUser.getLocation().isEmpty()) ? currentUser.getLocation() : null;
        List<String> userInterests = (currentUser != null) ? currentUser.getInterests() : List.of();
        List<String> userEventTypes = (currentUser != null) ? currentUser.getEventTypes() : List.of();

        // --- 1. Recommended Events (Async) ---
        startRecommendedSearch(city, userInterests, userEventTypes);

        // --- 2. Near You Events (Async) ---
        startNearYouSearch(city, Collections.emptyList()); // Start with empty exclusion list

        // --- 3. Popular Events (Local Data, Async) ---
        startPopularSearch();
    }

    private void startRecommendedSearch(String city, List<String> interests, List<String> eventTypes) {
        boolean isLlmAvailable = (llmSearchOrchestrator != null);
        boolean hasPreferences = (!interests.isEmpty() || !eventTypes.isEmpty());

        if (isLlmAvailable && hasPreferences) {
            toggleLoading(scrollRecommended, true);
            backgroundExecutor.submit(() -> {
                String profileQuery = buildProfileQuery(city, interests, eventTypes, 10);
                List<Event> recommendedEvents = llmSearchOrchestrator.searchAndMapEvents(profileQuery);
                Platform.runLater(() -> {
                    toggleLoading(scrollRecommended, false);
                    displayEvents(recommendedEvents, flowRecommended);
                    setupCarouselNavigation(scrollRecommended, flowRecommended, prevRecommended, nextRecommended);
                });
            });
        } else {
            String message = isLlmAvailable ? "Add interests to your profile for AI recommendations." : "AI service is unavailable.";
            displayEmptyMessage(message, flowRecommended);
            prevRecommended.setVisible(false);
            nextRecommended.setVisible(false);
        }
    }

    private void startNearYouSearch(String city, List<Event> eventsToExclude) {
        boolean isLlmAvailable = (llmSearchOrchestrator != null);
        boolean hasCity = (city != null && !city.isEmpty());

        if (isLlmAvailable && hasCity) {
            toggleLoading(scrollNear, true);
            backgroundExecutor.submit(() -> {
                String nearYouQuery = buildProfileQuery(city, null, null, 10);
                List<Event> allNearYouEvents = llmSearchOrchestrator.searchAndMapEvents(nearYouQuery);

                List<String> titlesToExclude = eventsToExclude.stream().map(Event::getTitle).collect(Collectors.toList());
                List<Event> nearYouEvents = allNearYouEvents.stream()
                        .filter(event -> !titlesToExclude.contains(event.getTitle()))
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    toggleLoading(scrollNear, false);
                    displayEvents(nearYouEvents, flowNear);
                    setupCarouselNavigation(scrollNear, flowNear, prevNear, nextNear);
                });
            });
        } else {
            String message = isLlmAvailable ? "Add a city to your profile to find events near you." : "AI service is unavailable.";
            displayEmptyMessage(message, flowNear);
            prevNear.setVisible(false);
            nextNear.setVisible(false);
        }
    }

    private void startPopularSearch() {
        toggleLoading(scrollPopular, true);
        backgroundExecutor.submit(() -> {
            List<Event> popularEvents = eventService.getAllEvents().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getUsers().size(), e1.getUsers().size()))
                    .limit(10)
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                toggleLoading(scrollPopular, false);
                displayEvents(popularEvents, flowPopular);
                setupCarouselNavigation(scrollPopular, flowPopular, prevPopular, nextPopular);
            });
        });
    }


    /**
     * @brief Logic to load event cards into a specific FlowPane.
     * @param events List of events to display.
     * @param targetFlowPane The FlowPane to populate (e.g., flowRecommended, eventCardsFilteredSearch).
     */
    private void displayEvents(List<Event> events, FlowPane targetFlowPane) {
        targetFlowPane.getChildren().clear();
        System.out.println("yooo");
        // Standard calculations
        double scale = 0.70;
        double baseCardWidth = 350.0;
        double horizontalMargin = 10.0 + 10.0;
        double estimatedItemWidth = baseCardWidth * scale + horizontalMargin; // ~265.0

        // --- 1. SPECIAL LOGIC FOR RECOMMENDED SECTION ---
        if (targetFlowPane == flowRecommended) {

            int count = events.size();

            // Scenario 1: 3 or less events
            if (count <= 3) {
                // Width: Same formula (all in 1 row)
                targetFlowPane.setPrefWidth(count * estimatedItemWidth + 20);

                // Height: 277
                if (anchorRecommended != null) {
                    anchorRecommended.setPrefHeight(277);
                }

                // Scenario 2: 4 to 6 events
            } else if (count <= 6) {
                // Width: Same formula (all in 1 row, as requested)
                targetFlowPane.setPrefWidth(count * estimatedItemWidth + 20);

                // Height: 574
                if (anchorRecommended != null) {
                    anchorRecommended.setPrefHeight(574);
                }

                // Scenario 3: More than 6 events (Grid Layout)
            } else {
                // Width: Calculated by columns (Ceil of size / 2)
                // e.g., 7 events -> 4 cols; 10 events -> 5 cols
                int columns = (int) Math.ceil(count / 2.0);
                targetFlowPane.setPrefWidth(columns * estimatedItemWidth + 20);

                // Height: 574 (always 2 rows)
                if (anchorRecommended != null) {
                    anchorRecommended.setPrefHeight(574);
                }
            }

            // Handle empty case
            if (events.isEmpty()) {
                targetFlowPane.setPrefWidth(0);
            }

            // --- 2. LOGIC FOR POPULAR / NEAR / SEARCH ---
        } else {
            // Control carousel width based on content.
            if (events.isEmpty() && (targetFlowPane == flowPopular || targetFlowPane == flowNear)) {
                targetFlowPane.setPrefWidth(0);
                return;
            } else if (targetFlowPane == flowPopular || targetFlowPane == flowNear) {
                // Standard single-row carousel
                targetFlowPane.setPrefWidth(events.size() * estimatedItemWidth + 20);
            } else {
                // Search Results (FlowPane default wrapping)
                targetFlowPane.setPrefWidth(FlowPane.USE_COMPUTED_SIZE);
            }
        }

        // --- 3. CREATE CARDS ---
        for (Event event : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLs/EventCard.fxml"));
                VBox eventCard = loader.load();
                EventCardController controller = loader.getController();
                controller.setEvent(event);

                // Scaling
                eventCard.setScaleX(scale);
                eventCard.setScaleY(scale);

                double originalWidth = eventCard.getPrefWidth();
                double originalHeight = eventCard.getPrefHeight();

                StackPane wrapper = new StackPane(eventCard);
                wrapper.setPrefSize(originalWidth * scale, originalHeight * scale);
                wrapper.setMaxSize(originalWidth * scale, originalHeight * scale);
                wrapper.setMinSize(originalWidth * scale, originalHeight * scale);

                FlowPane.setMargin(wrapper, new Insets(10, 10, 0, 10));
                targetFlowPane.getChildren().add(wrapper);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupCarouselNavigation(ScrollPane scrollPane, FlowPane flowPane, Button prevButton, Button nextButton) {
        if (flowPane.getPrefWidth() <= 0) {
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            return;
        }

        prevButton.setFocusTraversable(false);
        nextButton.setFocusTraversable(false);

        prevButton.setVisible(scrollPane.getHvalue() > 0.01);
        nextButton.setVisible(scrollPane.getHvalue() < 0.99);

        // Define card dimensions matching the logic in displayEvents
        // Card width (350) * scale (0.7) + margins (10 left + 10 right)
        double cardPixelWidth = (350.0 * 0.70) + 20.0; // ~265.0 pixels

        // --- BUTTON ACTIONS ---

        prevButton.setOnAction(e -> {
            // 1. Calculate the total scrollable distance in pixels
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double contentWidth = flowPane.getPrefWidth();
            double scrollableDistance = contentWidth - viewportWidth;

            // 2. Convert card pixel width to Hvalue (0.0 to 1.0)
            double scrollStep = 0.30; // Default fallback
            if (scrollableDistance > 0) {
                scrollStep = cardPixelWidth / scrollableDistance;
            }

            // 3. Scroll
            double currentH = scrollPane.getHvalue();
            double newH = Math.max(0.0, currentH - scrollStep);
            animateScroll(scrollPane, newH);
        });

        nextButton.setOnAction(e -> {
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double contentWidth = flowPane.getPrefWidth();
            double scrollableDistance = contentWidth - viewportWidth;

            double scrollStep = 0.30;
            if (scrollableDistance > 0) {
                scrollStep = cardPixelWidth / scrollableDistance;
            }

            double currentH = scrollPane.getHvalue();
            double newH = Math.min(1.0, currentH + scrollStep);
            animateScroll(scrollPane, newH);
        });

        // --- VISIBILITY ---

        scrollPane.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            prevButton.setVisible(newVal.doubleValue() > 0.01);
            nextButton.setVisible(newVal.doubleValue() < 0.99);
        });
    }

    /**
     * @brief Animates the ScrollPane horizontally with conflict handling.
     */
    private void animateScroll(ScrollPane scrollPane, double targetHValue) {
        // 1. Stop any existing animation to prevent conflicts/jumps
        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }

        // 2. Create new timeline
        scrollAnimation = new Timeline();

        // 3. Define KeyValue: Animate hvalue to targetHValue
        KeyValue kv = new KeyValue(
                scrollPane.hvalueProperty(),
                targetHValue,
                Interpolator.EASE_BOTH // Smooth acceleration/deceleration
        );

        // 4. Set duration (500ms is slower and more noticeable than 300ms)
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);

        scrollAnimation.getKeyFrames().add(kf);
        scrollAnimation.play();
    }

    /**
     * @brief Toggles a loading spinner on top of the given carousel.
     * @param scrollPane The carousel ScrollPane (used to find the parent StackPane).
     * @param isLoading True to show spinner, False to hide it.
     */
    private void toggleLoading(ScrollPane scrollPane, boolean isLoading) {
        if (scrollPane == null || scrollPane.getParent() == null) return;

        // 1. Get the parent StackPane (the container for ScrollPane + Buttons)
        StackPane container = (StackPane) scrollPane.getParent();

        if (isLoading) {
            // 2. Check if spinner already exists to avoid duplicates
            boolean hasSpinner = container.getChildren().stream()
                    .anyMatch(node -> node instanceof ProgressIndicator);

            if (!hasSpinner) {
                ProgressIndicator spinner = new ProgressIndicator();
                spinner.setMaxSize(50, 50); // Set a reasonable size
                spinner.setStyle("-fx-progress-color: #5a43f0;"); // Match your theme color
                container.getChildren().add(spinner);
            }
            // Optional: Fade out the scroll pane content slightly
            scrollPane.setOpacity(0.3);

        } else {
            // 3. Remove the spinner
            container.getChildren().removeIf(node -> node instanceof ProgressIndicator);
            scrollPane.setOpacity(1.0); // Restore opacity
        }
    }

    // --- Search and Filter Logic ---

    /**
     * @brief Handles changes in search field or filters.
     * Toggles visibility between the filtered results and the carousels.
     */
    private void handleSearchOrFilterChange() {
        String searchTerm = searchTextField.getText().trim();
        List<String> cities = selectCity.getCheckModel().getCheckedItems();
        List<String> eventTypes = selectEventType.getCheckModel().getCheckedItems();
        List<String> interests = selectInterests.getCheckModel().getCheckedItems();

        boolean isSearchActive = !searchTerm.isEmpty() || isAnyFilterActive(cities, eventTypes, interests, startingDate.getValue() != null, endingDate.getValue() != null);

        if (isSearchActive) {
            updateFilteredResults();
            complexSearch.setVisible(false); // Hide carousels
        } else {
            // Clear filtered results and show carousels
            eventCardsFilteredSearch.getChildren().clear();
            eventCardsFilteredSearch.setVisible(false);
            if (noEventsLabel != null) noEventsLabel.setVisible(false);
            complexSearch.setVisible(true); // Show carousels
            // Re-initialize carousels to ensure they are displayed correctly
            initializeEventCarousels();
        }
    }

    /**
     * @brief Checks if any specific filter (city, event type, interest, date, slider) is actively selected/set.
     */
    private boolean isAnyFilterActive(List<String> cities, List<String> eventTypes, List<String> interests, boolean hasStartDate, boolean hasEndDate) {
        // Check if lists are not empty AND not only containing the "All" option
        boolean citiesActive = !cities.isEmpty() && !cities.contains("All Cities");
        boolean eventTypesActive = !eventTypes.isEmpty() && !eventTypes.contains("All Event Types");
        boolean interestsActive = !interests.isEmpty() && !interests.contains("All Interests");

        // Check date filters
        boolean datesActive = hasStartDate || hasEndDate;

        // Check if sliders are not at their default max/min (e.g., 0 to 10000)
        boolean priceActive = priceRangeSlider.getLowValue() > 0.01 || priceRangeSlider.getHighValue() < priceRangeSlider.getMax();
        boolean currentActive = currentAttendeesSlider.getLowValue() > 0.01 || currentAttendeesSlider.getHighValue() < currentAttendeesSlider.getMax();
        boolean maxActive = maxAttendeesSlider.getLowValue() > 0.01 || maxAttendeesSlider.getHighValue() < maxAttendeesSlider.getMax();

        return citiesActive || eventTypesActive || interestsActive || datesActive || priceActive || currentActive || maxActive;
    }

    /**
     * @brief Asynchronously loads event cards based on the current search term and active filters.
     */
    private void updateFilteredResults() {
        // 1. Reset generic "no events" label
        if (noEventsLabel != null) {
            noEventsLabel.setVisible(false);
        }

        // 2. Collect filter values
        String searchTerm = searchTextField.getText().trim();
        List<String> cities = selectCity.getCheckModel().getCheckedItems();
        List<String> eventTypes = selectEventType.getCheckModel().getCheckedItems();
        List<String> interests = selectInterests.getCheckModel().getCheckedItems();
        LocalDateTime startDate = startingDate.getValue() != null ? startingDate.getValue().atStartOfDay() : null;
        LocalDateTime endDate = endingDate.getValue() != null ? endingDate.getValue().atStartOfDay() : null;

        boolean isLlmSearchCandidate = !searchTerm.isEmpty() || isAnyFilterActive(cities, eventTypes, interests, startDate != null, endDate != null);

        // 3. Logic Branching
        if (isLlmSearchCandidate) {

            // CHECK 1: Is the Service Available?
            if (llmSearchOrchestrator == null) {
                eventCardsFilteredSearch.setVisible(true);
                displayEmptyMessage("Search is unavailable.\n(AI Service disabled)", eventCardsFilteredSearch);
                return;
            }

            // CHECK 2: Proceed with Search
            String hybridQuery = buildHybridQuery(searchTerm, cities, eventTypes, interests, startDate, endDate);

            // Show Spinner
            toggleLoading(eventCardsFilteredSearch, true);

            backgroundExecutor.submit(() -> {
                List<Event> foundEvents = searchLlmEvents(hybridQuery);

                Platform.runLater(() -> {
                    if (foundEvents.isEmpty()) {
                        // Search finished but no results found
                        eventCardsFilteredSearch.getChildren().clear();
                        eventCardsFilteredSearch.setVisible(false);
                        if (noEventsLabel != null) {
                            noEventsLabel.setVisible(true);
                        }
                    } else {
                        // Results found
                        eventCardsFilteredSearch.setVisible(true);
                        displayEvents(foundEvents, eventCardsFilteredSearch);
                    }
                });
            });

        } else {
            // No search active -> Show generic empty state
            eventCardsFilteredSearch.setVisible(false);
            if (noEventsLabel != null) {
                noEventsLabel.setVisible(true);
            }
        }
    }

    /**
     * @brief Toggles a loading spinner inside a FlowPane (for search results).
     */
    private void toggleLoading(FlowPane flowPane, boolean isLoading) {
        if (flowPane == null) return;

        if (isLoading) {
            flowPane.getChildren().clear(); // Clear old results
            flowPane.setVisible(true);      // Ensure pane is visible to show spinner

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setMaxSize(50, 50);
            spinner.setStyle("-fx-progress-color: #5a43f0;");

            // Center the spinner using a StackPane wrapper
            StackPane wrapper = new StackPane(spinner);
            wrapper.setPrefSize(flowPane.getPrefWidth() > 0 ? flowPane.getPrefWidth() : 800, 150);
            wrapper.setAlignment(javafx.geometry.Pos.CENTER);

            flowPane.getChildren().add(wrapper);

        } else {
            // No explicit remove needed; displayEvents() or displayEmptyMessage()
            // will clear the children when they run.
        }
    }

    private List<Event> searchLlmEvents(String hybridQuery) {
        if (llmSearchOrchestrator == null) {
            System.err.println("LLM Search Orchestrator is not initialized.");
            return Collections.emptyList();
        }
        return llmSearchOrchestrator.searchAndMapEvents(hybridQuery);
    }

    /**
     * @brief Constructs a structured query for the retriever based on user profile preferences.
     * Format: "City|Keywords|Context"
     */
    private String buildProfileQuery(String city, List<String> interests, List<String> categories, int limit) {
        // Part 1: City
        String cityString = (city != null && !city.isEmpty()) ? city : "";

        // Part 2: Keywords
        List<String> allKeywords = new ArrayList<>();
        if (interests != null) {
            allKeywords.addAll(interests);
        }
        if (categories != null) {
            allKeywords.addAll(categories);
        }
        String keywordString = String.join(",", allKeywords);
        if (keywordString.isEmpty()) {
            keywordString = "events"; // Generic fallback
        }

        // Part 3: Context for the LLM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateInfo = "Today is " + formatter.format(LocalDateTime.now()) + ". Find up to " + limit + " events.";

        return cityString + "|" + keywordString + "|" + dateInfo;
    }

    /**
     * @brief Constructs a structured query for the retriever based on active search filters.
     * Format: "City|Keywords|Context"
     */
    private String buildHybridQuery(String searchTerm, List<String> cities, List<String> categories, List<String> interests, LocalDateTime startDate, LocalDateTime endDate) {
        // Part 1: City
        String cityString = "";
        if (cities != null && !cities.isEmpty() && !cities.contains("All Cities")) {
            cityString = cities.get(0); // Use the first selected city for the API call
        }

        // Part 2: Keywords
        List<String> allKeywords = new ArrayList<>();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            allKeywords.add(searchTerm);
        }
        if (categories != null && !categories.contains("All Event Types")) {
            allKeywords.addAll(categories);
        }
        if (interests != null && !interests.contains("All Interests")) {
            allKeywords.addAll(interests);
        }
        String keywordString = String.join(",", allKeywords);
        if (keywordString.isEmpty()) {
            keywordString = "events"; // A generic fallback
        }

        // Part 3: Context for the LLM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateInfo = "Today is " + formatter.format(LocalDateTime.now());
        if (startDate != null) {
            dateInfo += ", find events after " + startDate.format(formatter);
        }
        if (endDate != null) {
            dateInfo += ", find events before " + endDate.format(formatter);
        }
        // Add price info if relevant
        double lowPrice = priceRangeSlider.getLowValue();
        double highPrice = priceRangeSlider.getHighValue();
        if (lowPrice > 0.01 || highPrice < priceRangeSlider.getMax() - 0.01) {
            dateInfo += ", with prices between " + (int)lowPrice + " and " + (int)highPrice;
        }

        return cityString + "|" + keywordString + "|" + dateInfo;
    }


    /**
     * @brief Filters the list of all available events based on search term and filter controls.
     */
    @FXML
    public List<Event> searchEvents(String searchTerm, List<String> cities, List<String> eventTypes, List<String> interests, LocalDateTime afterDate, LocalDateTime beforeDate) {
        List<Event> allEvents = eventService.getAllEvents();

        final String lowerCaseSearchTerm = searchTerm != null ? searchTerm.toLowerCase() : "";

        // Determine if any specific city/event type/interest filter is active
        boolean citiesFiltered = !cities.isEmpty() && !cities.contains("All Cities");
        boolean eventTypesFiltered = !eventTypes.isEmpty() && !eventTypes.contains("All Event Types");
        boolean interestsFiltered = !interests.isEmpty() && !interests.contains("All Interests");

        // Get slider values
        double lowPrice = priceRangeSlider.getLowValue();
        double highPrice = priceRangeSlider.getHighValue();
        double lowCurrent = currentAttendeesSlider.getLowValue();
        double highCurrent = currentAttendeesSlider.getHighValue();
        double lowMax = maxAttendeesSlider.getLowValue();
        double highMax = maxAttendeesSlider.getHighValue();

        return allEvents.stream()
                .filter(event -> {
                    // 1. Text search logic (Title, Description, Location)
                    boolean matchesSearchTerm = event.getTitle().toLowerCase().contains(lowerCaseSearchTerm) ||
                            event.getDescription().toLowerCase().contains(lowerCaseSearchTerm) ||
                            event.getLocation().toLowerCase().contains(lowerCaseSearchTerm);

                    // If NO search term, matchesSearchTerm is always true.
                    if(lowerCaseSearchTerm.isEmpty()) {
                        matchesSearchTerm = true;
                    }

                    // --- Filtering Logic (Detailed) ---

                    // If search/filters are inactive, we assume the carousels are visible, and this method
                    // should only be called if a search/filter is active (handled by handleSearchOrFilterChange).
                    if (!matchesSearchTerm && lowerCaseSearchTerm.isEmpty() && !isAnyFilterActive(cities, eventTypes, interests, afterDate != null, beforeDate != null)) {
                        return false;
                    }


                    // 2. City Filter: Match city OR no specific city filter is active.
                    boolean matchesCity = !citiesFiltered || cities.stream().anyMatch(c -> c.equalsIgnoreCase(event.getCity()));

                    // 3. Event Type Filter: Match event type OR no specific event type filter is active.
                    boolean matchesEventType = !eventTypesFiltered || eventTypes.stream().anyMatch(c -> c.equalsIgnoreCase(event.getEventType()));

                    // 4. Interests Filter: Match interests OR no specific interest filter is active.
                    boolean matchesInterests = !interestsFiltered || event.getInterests().stream().anyMatch(interests::contains);

                    // 5. Date range filters
                    boolean matchesDateRange = true;
                    // Combine LocalDate and LocalTime to get LocalDateTime for event start
                    LocalDateTime eventStartDateTime = event.getDate().atTime(event.getStartTime());

                    if (afterDate != null) {
                        matchesDateRange = !eventStartDateTime.isBefore(afterDate);
                    }
                    if (beforeDate != null) {
                        // Check if the event's start date/time is before or on the beforeDate
                        matchesDateRange = matchesDateRange && !eventStartDateTime.isAfter(beforeDate);
                    }

                    // 6. Slider filters
                    boolean matchesPrice = event.getPrice() >= lowPrice && event.getPrice() <= highPrice;
                    boolean matchesCurrentAttendees = event.getUsers().size() >= lowCurrent && event.getUsers().size() <= highCurrent;
                    boolean matchesMaxAttendees = event.getMaxAttendees() != null && event.getMaxAttendees() >= lowMax && event.getMaxAttendees() <= highMax;

                    // Combine all filters (All must be TRUE)
                    return matchesSearchTerm && matchesCity && matchesEventType && matchesInterests && matchesDateRange && matchesPrice && matchesCurrentAttendees && matchesMaxAttendees;
                })
                .collect(Collectors.toList());
    }

    /**
     * @brief Toggles the visibility and state of the filters modal.
     */
    @FXML
    public void toggleFiltersModal() {
        boolean isVisible = filtersModal.isVisible();
        filtersModal.setVisible(!isVisible);
        filtersModal.setDisable(isVisible);

        // Adjust the height of the row constraint to show/hide the modal
        if (isVisible) {
            filtersRow.setMaxHeight(0);
            filtersRow.setPrefHeight(0);
        } else {
            filtersRow.setMaxHeight(150);
            filtersRow.setPrefHeight(150);
        }
        handleSearchOrFilterChange(); // Rerun search after toggling filters
    }

    /**
     * @brief Initializes all filter controls, sets default values, and connects sliders to text fields.
     */
    @FXML
    public void initializeFiltersModal() {
        // Set slider properties (max values and default ranges)
        priceRangeSlider.setMax(10000);
        currentAttendeesSlider.setMax(10000);
        maxAttendeesSlider.setMax(10000);

        priceRangeSlider.setHighValue(10000);
        currentAttendeesSlider.setHighValue(10000);
        maxAttendeesSlider.setHighValue(10000);

        priceRangeSlider.setLowValue(0);
        currentAttendeesSlider.setLowValue(0);
        maxAttendeesSlider.setLowValue(0);

        // Populate choice boxes
        selectCity.getItems().addAll("All Cities", "New York", "London", "Tokyo", "Barcelona", "Seville", "Madrid", "Lisbon", "Coimbra");
        selectEventType.getItems().addAll("All Event Types");
        selectEventType.getItems().addAll(events);
        selectInterests.getItems().addAll("All Interests");
        selectInterests.getItems().addAll(interests);

        // Set default values (All selected)
        selectCity.getCheckModel().check("All Cities");
        selectEventType.getCheckModel().check("All Event Types");
        selectInterests.getCheckModel().check("All Interests");

        // Add listeners for exclusive selection logic (e.g., selecting "All" unchecks others)
        addExclusiveSelectionLogic(selectCity, "All Cities");
        addExclusiveSelectionLogic(selectEventType, "All Event Types");
        addExclusiveSelectionLogic(selectInterests, "All Interests");

        // Set up text formatters (Integer Filter)
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (Pattern.matches("[0-9]*", newText)) {
                return change;
            }
            return null;
        };
        currentAttendeesLow.setTextFormatter(new TextFormatter<>(integerFilter));
        currentAttendeesHigh.setTextFormatter(new TextFormatter<>(integerFilter));
        maxAttendeesLow.setTextFormatter(new TextFormatter<>(integerFilter));
        maxAttendeesHigh.setTextFormatter(new TextFormatter<>(integerFilter));

        // Set up text formatters (Price Filter)
        UnaryOperator<TextFormatter.Change> priceFilter = change -> {
            String newText = change.getControlNewText();
            // Allows "Free" or numeric input
            if (newText.equalsIgnoreCase("Free") || Pattern.matches("[0-9]*", newText)) {
                return change;
            }
            return null;
        };
        priceRangeLow.setTextFormatter(new TextFormatter<>(priceFilter));
        priceRangeHigh.setTextFormatter(new TextFormatter<>(priceFilter));

        // Initialize TextFields with slider values
        priceRangeLow.setText("Free");
        priceRangeHigh.setText(String.valueOf((int) priceRangeSlider.getMax()));
        currentAttendeesLow.setText(String.valueOf((int) currentAttendeesSlider.getLowValue()));
        currentAttendeesHigh.setText(String.valueOf((int) currentAttendeesSlider.getMax()));
        maxAttendeesLow.setText(String.valueOf((int) maxAttendeesSlider.getLowValue()));
        maxAttendeesHigh.setText(String.valueOf((int) maxAttendeesSlider.getMax()));

        // Connect sliders to text fields
        priceRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() == 0) {
                priceRangeLow.setText("Free");
            } else {
                priceRangeLow.setText(String.valueOf(newVal.intValue()));
            }
        });
        priceRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> priceRangeHigh.setText(String.valueOf(newVal.intValue())));
        currentAttendeesSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> currentAttendeesLow.setText(String.valueOf(newVal.intValue())));
        currentAttendeesSlider.highValueProperty().addListener((obs, oldVal, newVal) -> currentAttendeesHigh.setText(String.valueOf(newVal.intValue())));
        maxAttendeesSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> maxAttendeesLow.setText(String.valueOf(newVal.intValue())));
        maxAttendeesSlider.highValueProperty().addListener((obs, oldVal, newVal) -> maxAttendeesHigh.setText(String.valueOf(newVal.intValue())));

        // Connect text fields to sliders
        priceRangeLow.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                double value = newVal.equalsIgnoreCase("Free") ? 0 : Double.parseDouble(newVal);
                priceRangeSlider.setLowValue(value);
            }
        });
        priceRangeHigh.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.equalsIgnoreCase("Free")) {
                priceRangeSlider.setHighValue(Double.parseDouble(newVal));
            }
        });
        currentAttendeesLow.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                currentAttendeesSlider.setLowValue(Double.parseDouble(newVal));
            }
        });
        currentAttendeesHigh.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                currentAttendeesSlider.setHighValue(Double.parseDouble(newVal));
            }
        });
        maxAttendeesLow.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                maxAttendeesSlider.setLowValue(Double.parseDouble(newVal));
            }
        });
        maxAttendeesHigh.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                maxAttendeesSlider.setHighValue(Double.parseDouble(newVal));
            }
        });
    }

    /**
     * @brief Implements the logic to ensure that if the 'All' option is selected,
     * all others are deselected, and vice versa.
     */
    private void addExclusiveSelectionLogic(CheckComboBox<String> checkComboBox, String allOption) {
        final boolean[] updating = {false};
        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {
            if (updating[0]) {
                return;
            }
            updating[0] = true;

            while (c.next()) {
                if (c.wasAdded()) {
                    if (c.getAddedSubList().contains(allOption)) {
                        // If 'All' is added, clear all other checks
                        checkComboBox.getCheckModel().clearChecks();
                        checkComboBox.getCheckModel().check(allOption);
                        updating[0] = false;
                        return;
                    } else {
                        // If any other item is added, uncheck 'All' if it's checked
                        if (checkComboBox.getCheckModel().isChecked(allOption)) {
                            checkComboBox.getCheckModel().clearCheck(allOption);
                        }
                    }
                }
            }

            // Ensure at least one item is checked (default to 'All' if none are left)
            if (checkComboBox.getCheckModel().getCheckedItems().isEmpty()) {
                checkComboBox.getCheckModel().check(allOption);
            }

            updating[0] = false;
        });
    }

    /**
     * @brief Creates and displays a formatted message when a carousel section is empty due to missing user profile data.
     * @param message The message string (e.g., "City not inserted.").
     * @param targetFlowPane The FlowPane where the message will be displayed.
     */
    private void displayEmptyMessage(String message, FlowPane targetFlowPane) {
        targetFlowPane.getChildren().clear();

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #999999;");

        // Wrap the label in a StackPane to ensure it's centered in the FlowPane/ScrollView
        StackPane wrapper = new StackPane(messageLabel);
        // Set a fixed width/height so the ScrollPane doesn't collapse entirely
        wrapper.setPrefSize(400, 200);
        FlowPane.setMargin(wrapper, new Insets(20, 10, 0, 10));

        targetFlowPane.getChildren().add(wrapper);
        // Ensure the FlowPane width is set correctly to show the message
        targetFlowPane.setPrefWidth(420);
    }

    @FXML
    private void onCreateEventClicked() {
        navigationService.navigateTo("CreateEvent");
    }

    private void setupProfileMenu() {
        // Load profile image
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            profileButton.setText(currentUser.getName());
            loadProfileImage(currentUser.getProfileImagePath());
        }

        // Create the context menu
        VBox profileDrop = new VBox();
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
}
