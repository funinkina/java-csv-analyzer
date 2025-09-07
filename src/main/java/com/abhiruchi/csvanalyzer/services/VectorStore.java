package com.abhiruchi.csvanalyzer.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VectorStore {

    @Data
    @AllArgsConstructor
    public static class Entry {
        private String textChunk;
        private List<Double> embedding;
    }

    private final ConcurrentHashMap<String, List<Entry>> store = new ConcurrentHashMap<>();

    public void add(String sessionId, String textChunk, List<Double> embedding) {
        store.computeIfAbsent(sessionId, k -> new ArrayList<>())
            .add(new Entry(textChunk, embedding));
    }

    public List<Entry> findSimilar(String sessionId, List<Double> queryEmbedding, int limit) {
        List<Entry> entries = store.get(sessionId);
        if (entries == null) {
            return new ArrayList<>();
        }

        entries.sort((e1, e2) -> {
            double sim1 = cosineSimilarity(queryEmbedding, e1.getEmbedding());
            double sim2 = cosineSimilarity(queryEmbedding, e2.getEmbedding());
            return Double.compare(sim2, sim1); // Sort in descending order of similarity
        });

        return entries.stream().limit(limit).toList();
    }
    
    public void clear(String sessionId) {
        store.remove(sessionId);
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}