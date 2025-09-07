package com.abhiruchi.csvanalyzer.services;

import com.abhiruchi.csvanalyzer.controller.ChatController.QueryResponse;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.util.Arrays;
// import java.util.Arrays;
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

    public QueryResponse answerQuery(String sessionId, String query) {
        List<Double> queryEmbedding = geminiService.getEmbedding(query);
        // Get more context for charts, as they often require more data
        List<VectorStore.Entry> similarEntries = vectorStore.findSimilar(sessionId, queryEmbedding, 15);

        List<String> context = similarEntries.stream()
                .map(VectorStore.Entry::getTextChunk)
                .collect(Collectors.toList());

        if (context.isEmpty()) {
            return new QueryResponse("text", "I don't have enough information from the CSV to answer that.");
        }

        // Decide whether to generate a chart or text
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