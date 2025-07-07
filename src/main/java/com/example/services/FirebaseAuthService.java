package com.example.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class FirebaseAuthService {

    private final String BACKEND_URL = "http://localhost:8080/api/auth";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FirebaseAuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String signInWithEmailAndPassword(String email, String password) throws Exception {
        String url = BACKEND_URL + "/authenticate";

        String jsonPayload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Authentication failed");
        }

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        return jsonResponse.get("idToken").asText();
    }
}