package Model.Assistant;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketmasterContentRetriever implements ContentRetriever {

    private final String apiKey;
    private final HttpClient httpClient;

    public TicketmasterContentRetriever(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<Content> aggregatedContent = new ArrayList<>();

        try {
            String rawText = query.text();
            String cityParam = "";
            String keywordsParam = "events"; // default

            // 1. Parse da Query com o Separador '|' (Lógica que definimos antes)
            if (rawText.contains("|")) {
                String[] parts = rawText.split("\\|");
                if (parts.length > 0 && !parts[0].isEmpty() && !parts[0].equals("null")) {
                    cityParam = parts[0].trim();
                }
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    keywordsParam = parts[1].trim();
                }
            } else {
                keywordsParam = rawText;
            }

            // 2. DIVISÃO: Separar as categorias por vírgula
            // Se keywordsParam for "Music,Sports", cria um array ["Music", "Sports"]
            String[] categories = keywordsParam.split(",");

            System.out.println("--- [Ticketmaster Multi-Search] ---");
            System.out.println("City: " + cityParam);
            System.out.println("Categories to fetch: " + categories.length + " (" + keywordsParam + ")");

            // 3. CICLO: Chamar a API para cada categoria individualmente
            for (String category : categories) {
                category = category.trim();
                if (category.isEmpty()) continue;

                // Chama o metodo auxiliar para ir buscar o JSON desta categoria
                String jsonResult = fetchEventsForCategory(cityParam, category);

                if (jsonResult != null) {
                    // Adiciona este JSON à lista final que vai para o LLM
                    aggregatedContent.add(Content.from(jsonResult));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (aggregatedContent.isEmpty()) {
            return Collections.emptyList();
        }

        return aggregatedContent;
    }

    // Metodo auxiliar para fazer o pedido HTTP
    private String fetchEventsForCategory(String city, String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);

            StringBuilder urlBuilder = new StringBuilder("https://app.ticketmaster.com/discovery/v2/events.json?");
            urlBuilder.append("apikey=").append(apiKey);
            urlBuilder.append("&keyword=").append(encodedKeyword);

            if (!city.isEmpty()) {
                urlBuilder.append("&city=").append(encodedCity);
            }

            urlBuilder.append("&size=10&sort=date,asc");

            String url = urlBuilder.toString();
            System.out.println("Fetching API for category: '" + keyword + "' -> " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Failed to fetch '" + keyword + "': " + response.statusCode());
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error fetching '" + keyword + "': " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching '" + keyword + "': " + e.getMessage());
            return null;
        }
    }
}