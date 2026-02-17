package ru.t1.springaidemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.springaidemo.model.Chat;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}