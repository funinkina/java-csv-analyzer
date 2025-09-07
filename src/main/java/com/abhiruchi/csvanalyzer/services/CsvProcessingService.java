package com.abhiruchi.csvanalyzer.services;

import com.abhiruchi.csvanalyzer.controller.ChatController.QueryResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvProcessingService {

    private final GeminiService geminiService;
    private final VectorStore vectorStore;

    public CsvProcessingService(GeminiService geminiService, VectorStore vectorStore) {
        this.geminiService = geminiService;
        this.vectorStore = vectorStore;
    }

    private static final List<String> CHART_KEYWORDS = Arrays.asList(
            "chart", "graph", "plot", "visualize", "bar", "pie", "line", "doughnut");

    public List<String> processAndSuggest(String sessionId, MultipartFile file) {
        vectorStore.clear(sessionId); //
        List<String> suggestions = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> allRows = reader.readAll();
            if (allRows.isEmpty()) {
                throw new RuntimeException("CSV file is empty.");
            }

            String header = String.join(", ", allRows.get(0));
            List<String> sampleRows = allRows.stream()
                    .skip(1)
                    .limit(3)
                    .map(row -> String.join(", ", row))
                    .collect(Collectors.toList());

            suggestions = geminiService.generateSuggestions(header, sampleRows);

            for (String[] row : allRows) {
                String textChunk = String.join(", ", row);
                List<Double> embedding = geminiService.getEmbedding(textChunk);
                vectorStore.add(sessionId, textChunk, embedding);
            }

            return suggestions;

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to process CSV file", e);
        }
    }

    public QueryResponse answerQuery(String sessionId, String query) {
        List<Double> queryEmbedding = geminiService.getEmbedding(query);
        List<VectorStore.Entry> similarEntries = vectorStore.findSimilar(sessionId, queryEmbedding, 15);

        List<String> context = similarEntries.stream()
                .map(VectorStore.Entry::getTextChunk)
                .collect(Collectors.toList());

        if (context.isEmpty()) {
            return new QueryResponse("text", "I don't have enough information from the CSV to answer that.");
        }

        boolean isChartRequest = CHART_KEYWORDS.stream().anyMatch(query.toLowerCase()::contains);

        if (isChartRequest) {
            Map<String, Object> chartConfig = geminiService.getChartJsConfig(query, context);
            return new QueryResponse("chart", chartConfig);
        } else {
            String textAnswer = geminiService.getChatResponse(query, context);
            return new QueryResponse("text", textAnswer);
        }
    }
}