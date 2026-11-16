package ru.t1.springaidemo.advisors.rag;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class RagAdvisor implements BaseAdvisor {

    @Builder.Default
    private static final PromptTemplate template = PromptTemplate.builder().template("""
            CONTEXT: {context}
            Question: {question}
            """).build();

    private static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";

    private VectorStore vectorStore;

    @Builder.Default
    private SearchRequest searchRequest = SearchRequest.builder()
            .topK(1) // Сколько максимум чанков документа взять из RAG, перед этим идет сортировка чанков по релевантности (по векторной similarity)
            .similarityThreshold(0.62) // на сколько процентов должен быть похож чанк (документ), аналог topP, обрезает варианты подходящих чанков из предложенных (по умолчанию 0)
            .build();

    @Getter
    private final int order;

    public static RagAdvisorBuilder builder(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String originalUserQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String queryToRag = chatClientRequest.context().getOrDefault(ENRICHED_QUESTION, originalUserQuestion).toString();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.from(searchRequest)
                .query(queryToRag)
                .topK(searchRequest.getTopK() * 2)
                .build());
        if (CollectionUtils.isEmpty(documents)) {
            return chatClientRequest.mutate().context("CONTEXT", "ТУТ ПУСТО - ни один документ не обнаружили").build();
        }
        String llmContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String finalUserPrompt = template.render(Map.of(
                "context", llmContext,
                "question", originalUserQuestion));

        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalUserPrompt)).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}
