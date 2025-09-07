package com.abhiruchi.csvanalyzer.services;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
// import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsvProcessingService {

    private final GeminiService geminiService;
    private final VectorStore vectorStore;

    public CsvProcessingService(GeminiService geminiService, VectorStore vectorStore) {
        this.geminiService = geminiService;
        this.vectorStore = vectorStore;
    }

    public void processAndEmbedCsv(String sessionId, MultipartFile file) {
        vectorStore.clear(sessionId); // Clear previous data for the session
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            // We'll treat each row as a text chunk for simplicity
            for (String[] row : allRows) {
                String textChunk = String.join(", ", row);
                List<Double> embedding = geminiService.getEmbedding(textChunk);
                vectorStore.add(sessionId, textChunk, embedding);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV file", e);
        }
    }

    public String answerQuery(String sessionId, String query) {
        List<Double> queryEmbedding = geminiService.getEmbedding(query);
        List<VectorStore.Entry> similarEntries = vectorStore.findSimilar(sessionId, queryEmbedding, 5); // Get top 5 relevant rows

        List<String> context = similarEntries.stream()
                                            .map(VectorStore.Entry::getTextChunk)
                                            .collect(Collectors.toList());

        if (context.isEmpty()) {
            return "I don't have enough information from the CSV to answer that. Please upload a relevant file.";
        }

        return geminiService.getChatResponse(query, context);
    }
}