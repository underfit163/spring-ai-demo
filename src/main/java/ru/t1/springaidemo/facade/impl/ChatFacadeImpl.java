package ru.t1.springaidemo.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.t1.springaidemo.facade.ChatFacade;
import ru.t1.springaidemo.model.Role;
import ru.t1.springaidemo.service.ChatService;

@RequiredArgsConstructor
@Component
public class ChatFacadeImpl implements ChatFacade {

    private final ChatService chatService;
    private final ChatClient chatClient;

    @Override
    public void proceedInteraction(Long chatId, String prompt) {
        chatService.addChatEntry(chatId, prompt, Role.USER);

        String answer = chatClient.prompt().user(prompt).call().content();

        chatService.addChatEntry(chatId, answer, Role.ASSISTANT);
    }

    @Override
    public SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt) {
        chatService.addChatEntry(chatId, prompt, Role.USER);

        SseEmitter sseEmitter = new SseEmitter(0L);

        StringBuilder answer = new StringBuilder();
        chatClient.prompt().user(prompt).stream()
                .chatResponse()
                .subscribe(response -> processToken(response, sseEmitter, answer),
                        sseEmitter::completeWithError,
                        () -> chatService.addChatEntry(chatId, answer.toString(), Role.ASSISTANT));

        return sseEmitter;
    }

    @SneakyThrows
    private static void processToken(ChatResponse response, SseEmitter sseEmitter, StringBuilder answer) {
        AssistantMessage token = response.getResult().getOutput();

        sseEmitter.send(token);
        answer.append(token.getText());
    }

}
