package ru.t1.springaidemo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.springaidemo.model.Chat;
import ru.t1.springaidemo.model.ChatEntry;
import ru.t1.springaidemo.repository.ChatRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostgresChatMemory implements ChatMemory {

    private final ChatRepository chatRepository;

    @Transactional
    @Override
    public void add(@NonNull String conversationId, List<Message> messages) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        for (Message message : messages) {
            chat.addChatEntry(ChatEntry.toChatEntry(message));
        }
        chatRepository.save(chat);
    }

    @Override
    public @NonNull List<Message> get(@NonNull String conversationId) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream()
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    public void clear(@NonNull String conversationId) {
        //not implemented
    }
}
