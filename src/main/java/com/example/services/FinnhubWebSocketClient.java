package com.example.services;

import javax.websocket.*;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

@ClientEndpoint
public class FinnhubWebSocketClient {
    private Session session;
    private final String API_KEY;

    public FinnhubWebSocketClient() {
        // Load API key from config
        String key = "";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            key = prop.getProperty("finnhub.api.key");
        } catch (Exception e) {
            e.printStackTrace();
        }
        API_KEY = key;
    }

    public void connectToTicker(String symbol) {
        try {
            String uri = "wss://ws.finnhub.io?token=" + API_KEY;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(uri));
            Thread.sleep(1000);

            String subscribeMessage = "{\"type\":\"subscribe\",\"symbol\":\"" + symbol + "\"}";
            session.getAsyncRemote().sendText(subscribeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to Finnhub WebSocket.");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

}
