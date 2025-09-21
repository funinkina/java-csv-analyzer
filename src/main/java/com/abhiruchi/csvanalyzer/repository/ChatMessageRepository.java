package com.abhiruchi.csvanalyzer.repository;

import com.abhiruchi.csvanalyzer.model.ChatMessage;
import com.abhiruchi.csvanalyzer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByTimestampAsc(User user);

    @Query("SELECT DISTINCT c.sessionId FROM ChatMessage c WHERE c.user = :user")
    List<String> findDistinctSessionIdByUser(@Param("user") User user);

    List<ChatMessage> findByUserAndSessionIdOrderByTimestampAsc(User user, String sessionId);
}