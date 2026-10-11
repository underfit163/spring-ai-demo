package ru.t1.springaidemo.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.t1.springaidemo.facade.ChatFacade;
import ru.t1.springaidemo.model.Role;
import ru.t1.springaidemo.service.PostgresChatMemory;
import ru.t1.springaidemo.service.ChatService;

@RequiredArgsConstructor
@Component
public class ChatFacadeImpl implements ChatFacade {

    private final ChatService chatService;
    private final ChatClient chatClient;
    private final PostgresChatMemory postgresChatMemory;

    @Override
    public void proceedInteraction(Long chatId, String prompt) {
        chatService.addChatEntry(chatId, prompt, Role.USER);

        String answer = chatClient.prompt().user(prompt).call().content();

        chatService.addChatEntry(chatId, answer, Role.ASSISTANT);
    }

    @Override
    public SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt) {
        SseEmitter sseEmitter = new SseEmitter(0L);

        StringBuilder answer = new StringBuilder();
        chatClient.prompt(prompt)
                .advisors(MessageChatMemoryAdvisor.builder(postgresChatMemory)
                        .conversationId(String.valueOf(chatId))
                        .build()) // менеджерит наши запросы\ответы в бд, дополняем информацию перед промптом и сохраняет ответ
                .stream()
                .chatResponse()
                .subscribe(response -> processToken(response, sseEmitter, answer),
                        sseEmitter::completeWithError);

        return sseEmitter;
    }

    @SneakyThrows
    private static void processToken(ChatResponse response, SseEmitter sseEmitter, StringBuilder answer) {
        AssistantMessage token = response.getResult().getOutput();

        sseEmitter.send(token);
        answer.append(token.getText());
    }

}
