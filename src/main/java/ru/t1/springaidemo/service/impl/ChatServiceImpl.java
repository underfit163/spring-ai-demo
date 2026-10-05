package ru.t1.springaidemo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.springaidemo.model.Chat;
import ru.t1.springaidemo.model.ChatEntry;
import ru.t1.springaidemo.model.Chat_;
import ru.t1.springaidemo.model.Role;
import ru.t1.springaidemo.repository.ChatRepository;
import ru.t1.springaidemo.service.ChatService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    @Override
    public List<Chat> getAllChats() {
        return chatRepository.findAll(Sort.by(Chat_.CREATED_AT).descending());
    }

    @Override
    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow();
    }

    @Override
    public Chat createNewChat(String title) {
        Chat chat = Chat.builder().title(title).build();
        return chatRepository.save(chat);
    }

    @Override
    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    @Transactional
    @Override
    public void addChatEntry(Long chatId, String prompt, Role role) {
        Chat chat = getChat(chatId);
        chat.addChatEntry(ChatEntry.builder().content(prompt).role(role).build());
    }

}
