package ru.t1.springaidemo.service;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import ru.t1.springaidemo.model.Chat;
import ru.t1.springaidemo.model.ChatEntry;
import ru.t1.springaidemo.repository.ChatRepository;

import java.util.List;

@Builder
public class PostgresChatMemory implements ChatMemory {

    private ChatRepository chatRepository;

    private int maxMessages;

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
        long messagesToSkip = Math.max(0, chat.getHistory().size() - maxMessages);
        return chat.getHistory().stream()
                .skip(messagesToSkip)
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    public void clear(@NonNull String conversationId) {
        chatRepository.deleteById(Long.valueOf(conversationId));
    }
}
