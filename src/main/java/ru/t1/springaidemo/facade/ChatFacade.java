package ru.t1.springaidemo.facade;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatFacade {

    void proceedInteraction(Long chatId, String prompt);

    SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt);
}
