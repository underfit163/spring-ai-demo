package ru.t1.springaidemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.springaidemo.advisors.expansion.ExpansionQueryAdvisor;
import ru.t1.springaidemo.advisors.rag.RagAdvisor;
import ru.t1.springaidemo.repository.ChatRepository;
import ru.t1.springaidemo.service.PostgresChatMemory;

@RequiredArgsConstructor
@Configuration
public class AIConfig {

    private final ChatRepository chatRepository;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    private static final PromptTemplate SYSTEM_PROMPT = new PromptTemplate(
            """
                    Ты — Евгений Борисов, Java-разработчик и эксперт по Spring. Отвечай от первого лица, кратко и по делу.
                    
                    Вопрос может быть о СЛЕДСТВИИ факта из Context.
                    ВСЕГДА связывай: факт Context → вопрос.
                    
                    Нет связи, даже косвенной = "я не говорил об этом в докладах".
                    Есть связь = отвечай.
                    """
    );

/*    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            """
                    {query}
                    
                    Контекст:
                    ---------------------
                    {question_answer_context}
                    ---------------------
                    
                    Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить."""
    );*/

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(1),
                        SimpleLoggerAdvisor.builder().order(2).build(),
                        /*getRagAdvisor(3),*/
                        RagAdvisor.builder(vectorStore).order(3).build(),
                        SimpleLoggerAdvisor.builder().order(4).build())
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.3) // рекомендуемая температура (стабильность и вариативность)
                        .topP(0.7) // рекомендуемая вероятность, обрезает варианты подходящих токенов из предложенных (вероятность того, что токен (слово) подходит под указанную вероятность)
                        .topK(20) // сколько максимум вариантов токенов взять (корреляция с topP (должна в сумме получится вероятность topP из взятых вариантов))
                        .repeatPenalty(1.1) // нужно ли штрафовать за повторения (дефолт 1 - будет повторяться)
                        .build())
                .defaultSystem(SYSTEM_PROMPT.render())
                .build();
    }

    private Advisor getHistoryAdvisor(int order) {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(order).build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(8)
                .chatRepository(chatRepository)
                .build();
    }

/*    private Advisor getRagAdvisor(int order) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(SearchRequest.builder()
                        .topK(4) // Сколько максимум чанков документа взять из RAG, перед этим идет сортировка чанков по релевантности (по векторной similarity)
                        .similarityThreshold(0.67) // на сколько процентов должен быть похож чанк (документ), аналог topP, обрезает варианты подходящих чанков из предложенных (по умолчанию 0)
                        .build())
                .order(order)
                .build();
    }*/

}
