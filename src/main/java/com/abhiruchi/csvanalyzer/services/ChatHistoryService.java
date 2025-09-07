package com.abhiruchi.csvanalyzer.services;

import com.abhiruchi.csvanalyzer.model.ChatMessage;
import com.abhiruchi.csvanalyzer.model.User;
import com.abhiruchi.csvanalyzer.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryService {
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatHistoryService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @SneakyThrows
    public void saveMessage(User user, String role, String contentType, Object content) {
        ChatMessage message = new ChatMessage();
        message.setUser(user);
        message.setRole(role);
        message.setContentType(contentType);
        message.setTimestamp(LocalDateTime.now());

        // If content is not a string (i.e., a chart map), convert to JSON string
        if (content instanceof String) {
            message.setContent((String) content);
        } else {
            message.setContent(objectMapper.writeValueAsString(content));
        }

        chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(User user) {
        return chatMessageRepository.findByUserOrderByTimestampAsc(user);
    }
}