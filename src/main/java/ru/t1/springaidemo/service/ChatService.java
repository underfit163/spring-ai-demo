package ru.t1.springaidemo.service;

import ru.t1.springaidemo.model.Chat;
import ru.t1.springaidemo.model.Role;

import java.util.List;

public interface ChatService {

    List<Chat> getAllChats();

    Chat getChat(Long chatId);

    Chat createNewChat(String title);

    void deleteChat(Long chatId);

    void addChatEntry(Long chatId, String prompt, Role role);

}
