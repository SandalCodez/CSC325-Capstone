package com.example.csc325capstone;

import org.json.JSONObject;

import javax.websocket.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinnhubService {

    private static final String CLOUD_FUNCTION_URL = "link";

    public Map<String, Stock> getQuotesForTickers(List<String> tickers) {
        Map<String, Stock> stockMap = new HashMap<>();

        try {
            String joinedTickers = String.join(",", tickers);
            String requestedUrl = CLOUD_FUNCTION_URL + "?tickers=" + joinedTickers;

            URL url = new URL(requestedUrl);
            HttpURLConnection conn = (HttpURLConnection)  url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            JSONObject json = new JSONObject(response.toString());

            for(String ticker : tickers) {
                if(json.has(ticker)) {
                    JSONObject quote = json.getJSONObject(ticker);

                    String companyName = getCompanyName(ticker);

                    double currentPrice = quote.optDouble("c", 0.0);
                    double high = quote.optDouble("h", 0.0);
                    double low = quote.optDouble("l", 0.0);
                    double open = quote.optDouble("o", 0.0);
                    double volume = quote.optDouble("v", 0.0);
                    double previousClose = quote.optDouble("pc", 0.0);

                    Stock stock = new Stock(ticker, companyName,currentPrice, high, low, open, volume, previousClose);
                    stockMap.put(ticker, stock);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockMap;
    }

    public String getCompanyName(String ticker) {
        try {
            String urlStr = CLOUD_FUNCTION_URL + "/getCompanyName?ticker=" + ticker;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            return json.optString("name", ticker);  // fallback if name not found
        } catch (Exception e) {
            e.printStackTrace();
            return ticker;  // fallback if error
        }
    }

    @ClientEndpoint
    public class FinnhubWebSocketClient {

        private Session session;

        public void connectToTicker(String symbol) {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                String uri = "wss://ws.finnhub.io?token=YOUR_API_KEY";  // Replace with actual token or use Cloud Function if routed
                container.connectToServer(this, URI.create(uri));

                // Delay to ensure connection is ready before subscribing
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
}