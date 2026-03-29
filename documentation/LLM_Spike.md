# 🧠 LangChain4j Spike Documentation: Gemini Integration

This document describes the successful integration spike to introduce **Large Language Model (LLM)** capabilities into the project using the **LangChain4j** library and the **Google Gemini API**.  
The main goal was to demonstrate the feasibility of creating a basic conversational AI service.

---

## 1. Project Setup and Dependencies (Maven)

The spike began with setting up a Java project using Maven for dependency and build management.

### Dependencies

| Dependency | Artifact ID | Purpose |
| :-- | :-- | :-- |
| `dev.langchain4j:langchain4j` | `langchain4j` | Provides the core API abstractions (`ChatModel`, `AiServices`). |
| `dev.langchain4j:langchain4j-google-ai-gemini` | `langchain4j-google-ai-gemini` | Integration module that provides the `GoogleAiGeminiChatModel` for connection to the **Google Gemini API**. |

### Java Version

Recommended version: **Java 21** (ou **17** estável).

---

## 2. Environment and Security Configuration

Secure access to the Gemini model was established through API key management outside of the source code.

### API Key Management

- **Key Source:** Valid key generated in Google AI Studio.
- **Environment Variable:** `GEMINI_API_KEY`
- **Access:** `System.getenv("GEMINI_API_KEY")` — prevents exposure of the key in the repository.

### Authentication Troubleshooting

**Erro:** `API key not valid`  
**Cause:**
The IDE did not inherit the environment variable.

**Resolution:**
- Fixed to use the actual variable.
- Restarted IntelliJ IDEA to load environment variables.

---

## 💻 3. Java Code Implementation

### 3.1. `LangChain4jSetup.java` — Model Instantiation

```java
package Model.Assistant;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class LangChain4jSetup {
    public static GoogleAiGeminiChatModel createModel() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("GEMINI_API_KEY not set. Using 'demo' key (rate-limited).");
            apiKey = "demo";
        }

        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
```

---

### 3.2. `SimpleChat.java` — Basic Chat Test

```java
package Model.Assistant;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class SimpleChat {
    public static void main(String[] args) {
        GoogleAiGeminiChatModel model = LangChain4jSetup.createModel();
        String userMessage = "Give me a recipe for a carrot cake";
        System.out.println("Prompt: " + userMessage);
        System.out.println("\nLLM Response: " + model.chat(userMessage));
    }
}
```

---

### 3.3. `Assistant.java` — AI Service Interface

```java
package Model.Assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {

    @SystemMessage("You are a polite, helpful coding assistant who responds concisely.")
    String chat(String userMessage);

    @UserMessage("Please write a haiku about the concept of {{topic}}.")
    String writeHaiku(@V("topic") String topic);
}
```

---

### 3.4. `AiServiceExample.java` — High-Level Example

```java
package Model.Assistant;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

public class AiServiceExample {
    public static void main(String[] args) {
        GoogleAiGeminiChatModel model = LangChain4jSetup.createModel();
        Assistant assistant = AiServices.create(Assistant.class, model);

        String chatResponse = assistant.chat("What's the best Java framework for AI?");
        System.out.println("\nChat Response: " + chatResponse);

        String haiku = assistant.writeHaiku("Java Threads");
        System.out.println("\nHaiku:\n" + haiku);
    }
}
```

---

## 🧩 4. Spike Conclusion and Future Development

The **LangChain4j + Gemini** integration was successful 🎯

### 🔍 Key Learnings
- **Abstraction:** Using `GoogleAiGeminiChatModel` e `AiServices` makes it easier to switch providers (Gemini, OpenAI, etc.).
- **Declarative Interfaces:** The `Assistant` provides a clean and intuitive way to define behavior and prompts.

### 🚀 Next Steps
- Add **ChatMemory** for continuous conversations.
- Implement **Tools / Function Calling** (`@Tool`).
- Integrate **RAG (Retrieval-Augmented Generation)** for internal data retrieval.

---

# 🌐 Real-Time Event Search Feature

## 1. Architecture: Retrieval-Augmented Generation (RAG)

1. **User Query:** Ex. “What are the biggest tech conferences next week?”
2. **Retrieval:** LangChain4j uses **Google Custom Search**.
3. **Augmentation:** Snippets are injected into the LLM's context.
4. **Generation:** The Gemini model produces an updated and contextualized response.

---

## 2. Dependencies (pom.xml)

| Dependency | Artifact ID | Purpose                                    |
| :-- | :-- |:-------------------------------------------|
| `langchain4j-google-ai-gemini` | `langchain4j-google-ai-gemini` | Modelo principal (Gemini 2.5 Flash).       |
| `langchain4j-web-search-engine-google-custom` | `langchain4j-web-search-engine-google-custom` | Implementa o **Google Custom Search API**. |
| `langchain4j` | `langchain4j` | Components base (RAG, interfaces).         |

### 💡 Example pom.xml

```
<properties>
    <langchain4j.version>1.8.0</langchain4j.version>
    <langchain4j.web.version>1.8.0-beta15</langchain4j.web.version>
</properties>

<dependencies>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-google-ai-gemini</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>

    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-web-search-engine-google-custom</artifactId>
        <version>${langchain4j.web.version}</version>
    </dependency>

    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>${langchain4j.version}</version>
    </dependency>
</dependencies>
```

---

## 3. Configuration (Environment Variables)

| Variable | Description | Source |
| :-- | :-- | :-- |
| `GOOGLE_CUSTOM_SEARCH_API_KEY` | API key for Google Custom Search JSON API | Google Cloud Console |
| `GOOGLE_CUSTOM_SEARCH_CSI` | Custom Search Engine ID | Google Programmable Search Engine |
| `GEMINI_API_KEY` | API key for the Gemini Model | Google AI Studio |

### ⚙️ Configure CSE for Global Search

1. Go to **Programmable Search Engine → Setup**.
2. In 'What to search', select 'Search the entire web'.
3. Remove specific URLs.
4. Copy the generated **CSI**.

---

## 4. Code Implementation — `LangChain4jSetup.java`

```java
public static ContentRetriever createWebSearchRetriever() {
    String searchApiKey = System.getenv("GOOGLE_CUSTOM_SEARCH_API_KEY");
    String csi = System.getenv("GOOGLE_CUSTOM_SEARCH_CSI");

    if (searchApiKey == null || csi == null) {
        System.err.println("FATAL: GOOGLE_CUSTOM_SEARCH_API_KEY or CSI not set.");
        return null;
    }

    WebSearchEngine webSearchEngine = GoogleCustomWebSearchEngine.builder()
            .apiKey(searchApiKey)
            .csi(csi)
            .maxResults(5)
            .build();

    return WebSearchContentRetriever.builder()
            .webSearchEngine(webSearchEngine)
            .build();
}
```

---

## ✅ Final Summary

- **Integration Success:** Gemini + LangChain4j fully functional.
- **Next Milestones:** Add memory, RAG chaining, and internal knowledge retrieval.
- **Security:** All credentials externalized in environment variables.  
