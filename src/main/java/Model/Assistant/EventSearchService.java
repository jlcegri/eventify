package Model.Assistant;

import dev.langchain4j.rag.content.retriever.ContentRetriever;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.util.List;

/**
 * Interface defining the AI service for searching for events.
 * The @AiService will automatically use the ContentRetriever bound to it.
 */
public interface EventSearchService {

    /**
     * Creates an instance of the EventSearchService, binding the LLM and the retriever.
     */
    static EventSearchService create(ContentRetriever contentRetriever) {
        // 1. Create the base LLM
        // Note: You must ensure LangChain4jSetup.createModel() returns a valid model
        // and has the GEMINI_API_KEY set.
        return AiServices.builder(EventSearchService.class)
                .chatModel(LangChain4jSetup.createModel())
                // 2. Bind the WebSearchContentRetriever here. This is the crucial step.
                .contentRetriever(contentRetriever)
                .build();
    }

    // System instruction guiding the model's persona and objective
    @SystemMessage({
            "You are an expert event researcher and assistant. Your goal is to find up-to-date, real-world events for the current year or the future.",
            "ALWAYS use the provided search results to answer the user's query.",
            "Extract details for all relevant events (maximum 20) and place them inside the 'events' list.",
            "The 'date' field MUST be in YYYY-MM-DD format. The 'time' field MUST be in HH:MM (24-hour) format.",
            "If a specific, valid date for the current year or a future year cannot be found in the text, you MUST set the 'date' and 'time' fields to null.",
            "Do not use past dates or placeholder dates like '0000-00-00'. If the event date is not clearly specified, use null.",
            "You MUST extract the city. If the city is not directly mentioned, try to infer it from the venue (e.g., 'MEO Arena' implies 'Lisbon'). If you cannot determine the city, set the 'city' field to null.",
            "Also, find a relevant, publicly accessible image URL for each event. If no suitable image is found, the 'imageURL' field should be null.",
            "If you cannot find any relevant events in the search results, return an empty list of events. DO NOT provide any textual explanation or apology."
    })

    // The main method for the user to call
    EventListContainer findEvents(String query);
}