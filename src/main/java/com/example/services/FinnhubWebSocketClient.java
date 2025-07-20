package com.example.services;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class FinnhubWebSocketClient extends WebSocketClient {

    private static String API_KEY;

    static {
        try (InputStream input = FinnhubWebSocketClient.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            API_KEY = prop.getProperty("finnhub.api.key");
        } catch (Exception e) {
            System.err.println("Error loading API key: " + e.getMessage());
        }
    }

    public FinnhubWebSocketClient(String symbol) throws URISyntaxException {
        super(new URI("wss://ws.finnhub.io?token=" + API_KEY));
        this.symbol = symbol;
    }

    private final String symbol;

    @Override
    public void onOpen(ServerHandshake handshakedata) {

        String message = "{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}";
        send(message);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    public void startClient() {
        connect();
    }
}
