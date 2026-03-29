package Model.Assistant;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;

public class LangChain4jSetup {
    public static GoogleAiGeminiChatModel createModel() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("GEMINI_API_KEY environment variable not set. Using 'demo' key which may be rate-limited.");
            apiKey = "demo"; // Fallback for basic testing
        }

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey("AIzaSyCXIUtaHRgf6fhioAfzbhdd8_-qfT5Uz0M")
                .modelName("gemini-2.5-flash") // Choose your model
                .logRequests(false)
                .logResponses(false)
                .build();
        return model;
    }

    /**
     * Creates and returns the Content Retriever that handles the real-time web search.
     * This uses RAG (Retrieval-Augmented Generation) pattern.
     */
    public static ContentRetriever createWebSearchRetriever() {
        String searchApiKey = System.getenv("GOOGLE_CUSTOM_SEARCH_API_KEY");
        String csi = System.getenv("GOOGLE_CUSTOM_SEARCH_CSI");

        if (searchApiKey == null || csi == null) {
            // NOTE: You MUST set these two environment variables for the search to work.
            System.err.println("FATAL: GOOGLE_CUSTOM_SEARCH_API_KEY or CSI not set. Web search will fail.");
            // You cannot use a "demo" key for Custom Search.
            return null;
        }

        // 1. Define the dedicated Web Search Engine (Google Custom Search in this case)
        WebSearchEngine webSearchEngine = GoogleCustomWebSearchEngine.builder()
                .apiKey("AIzaSyBvrK5IN3pfNsPqgit_c-kWWAzkdqJFOYg")
                .csi("f3666839264414eae")
                // Set maxResults to control how many search results are returned to the LLM.
                //.maxResults(5)
                .build();

        // 2. Wrap the engine in a ContentRetriever for the RAG chain
        return WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .build();
    }

    public static ContentRetriever createTicketmasterRetriever() {
        // Podes por a key aqui ou nas variáveis de ambiente como as outras
        String tmApiKey = System.getenv("TICKETMASTER_API_KEY");

        if (tmApiKey == null || tmApiKey.isEmpty()) {
            System.err.println("FATAL: TICKETMASTER_API_KEY not set.");
            return null;
        }

        return new TicketmasterContentRetriever(tmApiKey);
    }

    public static ContentRetriever createHybridRetriever() {
        // 1. Criar o Retriever da Web (Ideia 1)
        ContentRetriever webRetriever = createWebSearchRetriever();

        // 2. Criar o Retriever da Ticketmaster (Ideia 2)
        ContentRetriever tmRetriever = createTicketmasterRetriever(); // Metodo que criámos antes

        // Validação básica
        if (webRetriever == null && tmRetriever == null) {
            System.err.println("FATAL: Nenhuma fonte de dados configurada corretamente.");
            return null;
        }
        if (webRetriever == null) return tmRetriever;
        if (tmRetriever == null) return webRetriever;

        // 3. Juntar os dois
        System.out.println("Hybrid Retriever inicializado: Google + Ticketmaster");
        return new HybridContentRetriever(webRetriever, tmRetriever);
    }
}