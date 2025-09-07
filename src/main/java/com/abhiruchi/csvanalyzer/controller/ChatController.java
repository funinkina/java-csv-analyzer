package com.abhiruchi.csvanalyzer.controller;

import com.abhiruchi.csvanalyzer.services.CsvProcessingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import com.abhiruchi.csvanalyzer.model.User;
import com.abhiruchi.csvanalyzer.repository.UserRepository;
import com.abhiruchi.csvanalyzer.services.ChatHistoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final CsvProcessingService csvProcessingService;
    private final ChatHistoryService chatHistoryService;
    private final UserRepository userRepository;

    public ChatController(CsvProcessingService csvProcessingService, ChatHistoryService chatHistoryService,
            UserRepository userRepository) {
        this.csvProcessingService = csvProcessingService;
        this.chatHistoryService = chatHistoryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String sessionId = UUID.randomUUID().toString();
        // The processing service will now return the suggestions
        List<String> suggestions = csvProcessingService.processAndSuggest(sessionId, file);
        return ResponseEntity.ok(new UploadResponse(sessionId, "File processed successfully.", suggestions));
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> handleQuery(@RequestBody QueryRequest queryRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        chatHistoryService.saveMessage(user, "user", "text", queryRequest.getQuery());
        QueryResponse response = csvProcessingService.answerQuery(queryRequest.getSessionId(), queryRequest.getQuery());
        chatHistoryService.saveMessage(user, "bot", response.getType(), response.getContent());
        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    static class UploadResponse {
        private String sessionId;
        private String message;
        private List<String> suggestions;
    }

    @Data
    static class QueryRequest {
        private String sessionId;
        private String query;
    }

    @Data
    @AllArgsConstructor
    public static class QueryResponse {
        private String type;
        private Object content;
    }
}