package ru.t1.springaidemo.config;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    private static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";

    private ChatClient chatClient;
    @Getter
    private final int order;

    public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryAdvisorBuilder().chatClient(ChatClient.builder(chatModel)
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.0) // не хотим вариативности, хотим всегда получать один тот же ответ
                        .topK(1) // сколько токенов берем, чтобы выбрать следующий токен
                        .topP(0.1) // на какой вероятности заканчиваем искать следующие токены
                        .repeatPenalty(1.0) // дефолтное значение, не штрафуем за повторение
                        .build())
                .build());
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String enrichedQuestion = chatClient
                .prompt()
                .user("extender query")
                .call()
                .content();

        return chatClientRequest.mutate().context(ENRICHED_QUESTION, enrichedQuestion).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

}
