package com.abhiruchi.csvanalyzer.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Double> getEmbedding(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey;

        String requestBody = "{\"model\": \"models/embedding-001\", \"content\": {\"parts\": [{\"text\": \""
                + text.replace("\"", "'") + "\"}]}}";
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
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key="
                + apiKey;

        String contextString = context.stream().collect(Collectors.joining("\n"));

        String prompt = "Based on the following data from a CSV file, please answer the user's question. Data:\\n---\\n"
                +
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

    public Map<String, Object> getChartJsConfig(String userQuery, List<String> context) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key="
                + apiKey;

        String contextString = context.stream().collect(Collectors.joining("\n"));

        String prompt = "You are a data visualization assistant. Based on the following data from a CSV file, generate a valid JSON configuration for a Chart.js chart that answers the user's question. "
                +
                "Supported chart types are 'bar', 'line', 'pie', 'doughnut', 'radar', and 'polarArea'. " +
                "The JSON should be complete and ready to be used directly by new Chart(ctx, config). " +
                "Here is an example of a bar chart format: " +
                "{\"type\":\"bar\",\"data\":{\"labels\":[\"Label1\",\"Label2\"],\"datasets\":[{\"label\":\"Dataset Label\",\"data\":[10,20],\"backgroundColor\":[\"rgba(255, 99, 132, 0.2)\"],\"borderColor\":[\"rgba(255, 99, 132, 1)\"],\"borderWidth\":1}]}} "
                +
                "Only output the JSON configuration. Do not include markdown backticks (```json), explanations, or any other text. "
                +
                "\n--- DATA ---\n" +
                contextString +
                "\n--- END DATA ---\n" +
                "User Question: " + userQuery;

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\": \""
                + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
        String response = restTemplate.postForObject(url, requestBody, String.class);

        JSONObject jsonResponse = new JSONObject(response);
        String chartConfigString = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0).getJSONObject("content")
                .getJSONArray("parts").getJSONObject(0)
                .getString("text");

        // Clean the response in case the model adds markdown
        chartConfigString = chartConfigString.replace("```json", "").replace("```", "").trim();
        try {
            // Parse the JSON string into a Java Map
            return objectMapper.readValue(chartConfigString, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Chart.js config from Gemini response", e);
        }
    }
}