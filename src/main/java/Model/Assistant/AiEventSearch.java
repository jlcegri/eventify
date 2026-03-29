package Model.Assistant;

import java.util.List;

public class AiEventSearch {

    // Logic extracted to a testable method
    public List<EventLlmOutput> performSearch(EventSearchService service, String query) {
        System.out.println("Searching for: " + query);

        EventListContainer container = service.findEvents(query);

        if (container != null && container.events != null) {
            return container.events;
        }
        return List.of();
    }

    public static void main(String[] args) {
        // Real setup stays in main
        var retriever = LangChain4jSetup.createHybridRetriever();
        if (retriever == null) return;

        EventSearchService realService = EventSearchService.create(retriever);
        AiEventSearch app = new AiEventSearch();

        // Run logic
        List<EventLlmOutput> results = app.performSearch(realService, "Find cooking classes in Porto");

        // Print results (UI logic)
        results.forEach(event -> System.out.println(event.getTitle()));
    }
}