package com.abhiruchi.csvanalyzer.controller;

import com.abhiruchi.csvanalyzer.services.CsvProcessingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final CsvProcessingService csvProcessingService;

    public ChatController(CsvProcessingService csvProcessingService) {
        this.csvProcessingService = csvProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String sessionId = UUID.randomUUID().toString();
        csvProcessingService.processAndEmbedCsv(sessionId, file);
        return ResponseEntity.ok(new UploadResponse(sessionId, "File processed successfully."));
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> handleQuery(@RequestBody QueryRequest queryRequest) {
        String answer = csvProcessingService.answerQuery(queryRequest.getSessionId(), queryRequest.getQuery());
        return ResponseEntity.ok(new QueryResponse(answer));
    }

    // --- DTOs (Data Transfer Objects) for request/response bodies ---
    @Data
    @AllArgsConstructor
    static class UploadResponse {
        private String sessionId;
        private String message;
    }

    @Data
    static class QueryRequest {
        private String sessionId;
        private String query;
    }

    @Data
    @AllArgsConstructor
    static class QueryResponse {
        private String answer;
    }
}