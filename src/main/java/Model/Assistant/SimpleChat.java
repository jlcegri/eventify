package Model.Assistant;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class SimpleChat {

    // 1. Define a simple interface for our chat bot
    public interface ChatAssistant {
        String chat(String userMessage);
    }

    // 2. The logic method now accepts the interface, not the Model class.
    // This solves your import error and makes testing easy.
    public String askQuestion(ChatAssistant assistant, String userMessage) {
        System.out.println("Sending prompt to LLM: " + userMessage);
        return assistant.chat(userMessage);
    }

    public static void main(String[] args) {
        SimpleChat simpleChat = new SimpleChat();

        // 3. Create the Model
        GoogleAiGeminiChatModel model = LangChain4jSetup.createModel();

        // 4. Create the Assistant using AiServices (Just like in EventSearchService)
        ChatAssistant assistant = AiServices.builder(ChatAssistant.class)
                .chatModel(model)
                .build();

        // 5. Run it
        String response = simpleChat.askQuestion(assistant, "what year are we in?");
        System.out.println("\nLLM Response: " + response);
    }
}