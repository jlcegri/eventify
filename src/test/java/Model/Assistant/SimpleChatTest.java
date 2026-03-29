package Model.Assistant;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SimpleChatTest {

    @Test
    void testAskQuestion_returnsAnswer() {
        // 1. Mock the Assistant Interface (instead of the complex Model class)
        SimpleChat.ChatAssistant mockAssistant = Mockito.mock(SimpleChat.ChatAssistant.class);

        // 2. Define behavior
        when(mockAssistant.chat("Hello")).thenReturn("World");

        // 3. Run method
        SimpleChat simpleChat = new SimpleChat();
        String result = simpleChat.askQuestion(mockAssistant, "Hello");

        // 4. Assert
        assertEquals("World", result);
        verify(mockAssistant).chat("Hello");
    }
}