package ru.t1.springaidemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.springaidemo.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> /*ChatMemoryRepository*/ {

    /*@Override
    default List<String> findConversationIds() {
        return findAll().stream()
                .map(Chat::getId)
                .map(String::valueOf)
                .toList();
    }

    @Override
    default List<Message> findByConversationId(String conversationId) {
        if (Objects.equals(conversationId, ChatMemory.DEFAULT_CONVERSATION_ID)) return List.of();
        Chat chat = findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream()
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    default void saveAll(String conversationId, List<Message> messages) {
        if (Objects.equals(conversationId, ChatMemory.DEFAULT_CONVERSATION_ID)) return;
        Chat chat = findById(Long.valueOf(conversationId)).orElseThrow();
        messages.stream().map(ChatEntry::toChatEntry).forEach(chat::addChatEntry);
        save(chat);
    }

    @Override
    default void deleteByConversationId(String conversationId) {
        if (Objects.equals(conversationId, ChatMemory.DEFAULT_CONVERSATION_ID)) return;
        deleteById(Long.valueOf(conversationId));
    }*/

}
