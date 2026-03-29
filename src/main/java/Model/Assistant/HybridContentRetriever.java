package Model.Assistant;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Um Retriever que combina resultados de múltiplas fontes (ex: Web Search + Ticketmaster API).
 */
public class HybridContentRetriever implements ContentRetriever {

    private final List<ContentRetriever> retrievers;

    public HybridContentRetriever(ContentRetriever... retrievers) {
        this.retrievers = Arrays.asList(retrievers);
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<Content> combinedContent = new ArrayList<>();

        // Percorre todos os retrievers (Google, Ticketmaster, etc.)
        for (ContentRetriever retriever : retrievers) {
            try {
                List<Content> results = retriever.retrieve(query);
                if (results != null) {
                    combinedContent.addAll(results);
                }
            } catch (Exception e) {
                System.err.println("Aviso: Um dos retrievers falhou, mas continuamos com os outros. Erro: " + e.getMessage());
                // Não fazemos 'throw' para não parar o processo se uma fonte falhar
            }
        }

        return combinedContent;
    }
}