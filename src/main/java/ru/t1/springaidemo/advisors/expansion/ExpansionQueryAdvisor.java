package ru.t1.springaidemo.advisors.expansion;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import java.util.Map;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    @Builder.Default
    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                Instruction: Расширь поисковый запрос, добавив наиболее релевантные термины.
                
                СПЕЦИАЛИЗАЦИЯ ПО SPRING FRAMEWORK:
                - Жизненный цикл Spring бинов: конструктор → BeanPostProcessor → PostConstruct → прокси → ContextListener
                - Технологии: Dynamic Proxy, CGLib, reflection, аннотации, XML конфигурация
                - Компоненты: BeanFactory, ApplicationContext, BeanDefinition, MBean, JMX
                - Паттерны: dependency injection, AOP, профилирование, перехват методов

                ПРАВИЛА:
                1. Сохрани ВСЕ слова из исходного вопроса
                2. Добавь МАКСИМУМ ПЯТЬ наиболее важных термина
                3. Выбирай самые специфичные и релевантные слова
                4. Результат - простой список слов через пробел

                СТРАТЕГИЯ ВЫБОРА:
                - Приоритет: специализированные термины
                - Избегай общих слов
                - Фокусируйся на ключевых понятиях

                ПРИМЕРЫ:
                "что такое спринг" → "что такое спринг фреймворк Java"
                "как создать файл" → "как создать файл документ программа"

                Question: {question}
                Expanded query:
                """).build();

    public static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";
    public static final String ORIGINAL_QUESTION = "ORIGINAL_QUESTION";
    public static final String EXPANSION_RATIO = "EXPANSION_RATIO";

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
                .user(template.render(Map.of("question", userQuestion)))
                .call()
                .content();

        double ratio = enrichedQuestion.length() / (double) userQuestion.length();// вычисляет коэффициент увеличения длины вопроса после его обогащения с помощью LLM

        return chatClientRequest.mutate()
                .context(ORIGINAL_QUESTION, userQuestion)
                .context(ENRICHED_QUESTION, enrichedQuestion)
                .context(EXPANSION_RATIO, ratio)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

}
