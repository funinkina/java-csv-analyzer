package com.abhiruchi.csvanalyzer.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> getEmbedding(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey;

        String requestBody = "{\"model\": \"models/embedding-001\", \"content\": {\"parts\": [{\"text\": \"" + text.replace("\"", "'") + "\"}]}}";
        String response = restTemplate.postForObject(url, requestBody, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray embeddingArray = jsonResponse.getJSONObject("embedding").getJSONArray("values");

        List<Double> embedding = new ArrayList<>();
        for (int i = 0; i < embeddingArray.length(); i++) {
            embedding.add(embeddingArray.getDouble(i));
        }
        return embedding;
    }

    public String getChatResponse(String userQuery, List<String> context) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String contextString = context.stream().collect(Collectors.joining("\n"));

        String prompt = "Based on the following data from a CSV file, please answer the user's question. Data:\\n---\\n" +
                contextString +
                "\\n---\\nUser Question: " + userQuery +
                "\\nNote: Analyze the data and provide a direct, text-based answer. Do not mention that you are an AI or that you were given data. Just provide the answer.";

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\": \"" + prompt.replace("\"", "'") + "\"}]}]}";
        String response = restTemplate.postForObject(url, requestBody, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONArray("candidates")
                .getJSONObject(0).getJSONObject("content")
                .getJSONArray("parts").getJSONObject(0)
                .getString("text");
    }
}