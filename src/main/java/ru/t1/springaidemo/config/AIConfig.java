package ru.t1.springaidemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.springaidemo.repository.ChatRepository;
import ru.t1.springaidemo.service.PostgresChatMemory;

@RequiredArgsConstructor
@Configuration
public class AIConfig {

    private final ChatRepository chatRepository;
    private final VectorStore vectorStore;

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            """
                    {query}
                    
                    Контекст:
                    ---------------------
                    {question_answer_context}
                    ---------------------
                    
                    Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить."""
    );

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        getHistoryAdvisor(),
                        SimpleLoggerAdvisor.builder().build(),
                        getRagAdvisor(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.3) // рекомендуемая температура (стабильность и вариативность)
                        .topP(0.7) // рекомендуемая вероятность, обрезает варианты подходящих токенов из предложенных (вероятность того, что токен (слово) подходит под указанную вероятность)
                        .topK(20) // сколько максимум вариантов взять (корреляция с topP (должна в сумме получится вероятность topP из взятых вариантов))
                        .repeatPenalty(1.1) // нужно ли штрафовать за повторения (дефолт 1 - будет повторяться)
                        .build())
                .build();
    }

    private Advisor getHistoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(8)
                .chatRepository(chatRepository)
                .build();
    }

    private Advisor getRagAdvisor() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(SearchRequest.builder()
                        .topK(4) // Сколько максимум чанков документа взять из RAG, перед этим идет сортировка чанков по релевантности (по векторной similarity)
                        .similarityThreshold(0.67) // на сколько процентов должен быть похож чанк (документ), аналог topP, обрезает варианты подходящих чанков из предложенных (по умолчанию 0)
                        .build())
                .build();
    }

}
