package com.example.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;
import java.io.FileInputStream;
import java.util.Properties;


public class ChatGPTClient {
    private static final String API_KEY;

    static {
        Properties props = new Properties();
        try {
            props.load(ChatGPTClient.class.getClassLoader().getResourceAsStream("config.properties"));
            API_KEY = props.getProperty("OPENAI_API_KEY");
            if (API_KEY == null) throw new RuntimeException("API key not found!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load API key", e);
        }
    }

    public static String ask(String userMsg) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // Building the JSON for the request
        JSONObject body = new JSONObject()
                .put("model", "gpt-4o-mini")
                .put("messages", new JSONArray()
                        .put(new JSONObject().put("role", "system").put("content", "You are a helpful financial assistant. Your responses are in a mini chat box, respond with shorter but helpful responses. "))
                        .put(new JSONObject().put("role", "user").put("content", userMsg))
                )
                .put("max_tokens", 150);

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("[DEBUG] API Response: " + response.body());


        //Parse the JSON response to get the AI's answer
        JSONObject json = new JSONObject(response.body());
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
