package Model.Assistant;

import Model.Assistant.TicketmasterContentRetriever;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketmasterContentRetrieverTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    @Mock
    private Query mockQuery;

    private TicketmasterContentRetriever retriever;
    private final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() throws Exception {
        retriever = new TicketmasterContentRetriever(API_KEY);

        java.lang.reflect.Field httpClientField = TicketmasterContentRetriever.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(retriever, mockHttpClient);
    }

    @Test
    void testRetrieve_Success_WithCityAndKeyword() throws Exception {
        // 1. Arrange
        String inputQuery = "Lisbon|Rock Music";
        String expectedJson = "{\"events\": []}";

        when(mockQuery.text()).thenReturn(inputQuery);

        // --- FIX 1: Use doReturn to avoid Generic Type confusion ---
        doReturn(200).when(mockResponse).statusCode();
        doReturn(expectedJson).when(mockResponse).body();
        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // 2. Act
        List<Content> results = retriever.retrieve(mockQuery);

        // 3. Assert
        assertFalse(results.isEmpty());

        // --- FIX 2: Use .textSegment().text() ---
        assertEquals(expectedJson, results.get(0).textSegment().text());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());

        String capturedUrl = requestCaptor.getValue().uri().toString();
        assertTrue(capturedUrl.contains("keyword=Rock+Music"));
        assertTrue(capturedUrl.contains("city=Lisbon"));
    }

    @Test
    void testRetrieve_Success_FallbackNoSeparator() throws Exception {
        // 1. Arrange
        String inputQuery = "Coldplay Concert";
        String expectedJson = "{}";

        when(mockQuery.text()).thenReturn(inputQuery);

        // --- FIX 1 ---
        doReturn(200).when(mockResponse).statusCode();
        doReturn(expectedJson).when(mockResponse).body();
        doReturn(mockResponse).when(mockHttpClient).send(any(), any());

        // 2. Act
        List<Content> results = retriever.retrieve(mockQuery);

        // 3. Assert
        // --- FIX 2 ---
        assertEquals(expectedJson, results.get(0).textSegment().text());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(requestCaptor.capture(), any());

        String capturedUrl = requestCaptor.getValue().uri().toString();
        assertTrue(capturedUrl.contains("keyword=Coldplay+Concert"));
        assertFalse(capturedUrl.contains("&city="));
    }

    @Test
    void testRetrieve_ApiError_ReturnsEmpty() throws Exception {
        // 1. Arrange
        when(mockQuery.text()).thenReturn("test");

        // --- FIX 1 ---
        doReturn(401).when(mockResponse).statusCode();
        doReturn(mockResponse).when(mockHttpClient).send(any(), any());

        // 2. Act
        List<Content> results = retriever.retrieve(mockQuery);

        // 3. Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testRetrieve_InterruptedException() throws Exception {
        // 1. Arrange
        when(mockQuery.text()).thenReturn("test");

        // --- FIX 1 ---
        doThrow(new InterruptedException()).when(mockHttpClient).send(any(), any());

        // 2. Act
        List<Content> results = retriever.retrieve(mockQuery);

        // 3. Assert
        assertTrue(results.isEmpty());
        assertTrue(Thread.currentThread().isInterrupted());
    }
}