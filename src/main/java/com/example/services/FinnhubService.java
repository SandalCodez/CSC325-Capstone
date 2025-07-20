package com.example.services;

import com.example.models.CompanyProfile;
import com.example.models.Stock;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class FinnhubService {

    private String API_KEY;

    public FinnhubService() {
        Properties prop = new Properties();
        try (var input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(input);
            API_KEY = prop.getProperty("finnhub.api.key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Stock> getQuotesForTickers(List<String> tickers) {
        Map<String, Stock> stockMap = new HashMap<>();

        for (String ticker : tickers) {
            try {
                String quoteUrl = "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + API_KEY;
                String companyUrl = "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token=" + API_KEY;

                // Fetch quote
                JSONObject quoteJson = fetchJsonFromUrl(quoteUrl);
                double currentPrice = quoteJson.optDouble("c", 0.0);
                double volume = quoteJson.optDouble("v", 0.0);

                // Fetch company name
                JSONObject companyJson = fetchJsonFromUrl(companyUrl);
                String companyName = companyJson.optString("name", ticker);

                Stock stock = new Stock(ticker, companyName, currentPrice, volume);
                stockMap.put(ticker, stock);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return stockMap;
    }

    private JSONObject fetchJsonFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new JSONObject(response.toString());
        }
    }

    public String getCompanyName(String ticker) {
        try {
            String urlStr = "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token=" + API_KEY;
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
            return json.optString("name", ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return ticker;
        }
    }

    public JsonObject getHistoricalPrices(String symbol, long fromUnix, long toUnix) throws IOException {
        String urlStr = String.format(
                "https://finnhub.io/api/v1/stock/candle?symbol=%s&resolution=D&from=%d&to=%d&token=%s",
                symbol, fromUnix, toUnix, API_KEY
        );
        System.out.println("Fethching: " + urlStr);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream();
             InputStreamReader isr = new InputStreamReader(is)) {
            JsonParser parser = new JsonParser();
            return parser.parse(isr).getAsJsonObject();
        }
    }

    public List<String> getMarketNews() {
        List<String> newsHeadlines = new ArrayList<>();
        try {
            String urlString = "https://finnhub.io/api/v1/news?category=general&token=" + API_KEY;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < Math.min(5, jsonArray.length()); i++) { // Only show top 5 news items
                JSONObject newsItem = jsonArray.getJSONObject(i);
                String headline = newsItem.getString("headline");
                newsHeadlines.add("• " + headline);
            }
        } catch (Exception e) {
            e.printStackTrace();
            newsHeadlines.add("Error loading market news.");
        }
        return newsHeadlines;
    }

    public List<String> getCompanyNews(String symbol) {
        List<String> headlines = new ArrayList<>();
        try {
            String urlStr = "https://finnhub.io/api/v1/company-news?symbol=" + symbol +
                    "&from=2023-01-01&to=2025-12-31&token=" + API_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonArray newsArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (JsonElement element : newsArray) {
                JsonObject obj = element.getAsJsonObject();
                String headline = obj.get("headline").getAsString();
                headlines.add(headline);
            }

        } catch (Exception e) {
            e.printStackTrace();
            headlines.add("Failed to load news.");
        }
        return headlines;
    }

    public Stock getQuoteForTicker(String ticker) {
        List<String> singleTickerList = new ArrayList<>();
        singleTickerList.add(ticker);
        Map<String, Stock> result = getQuotesForTickers(singleTickerList);
        return result.get(ticker);
    }

    public CompanyProfile getCompanyProfile(String symbol) {
        try {
            String profileUrl = "https://finnhub.io/api/v1/stock/profile2?symbol=" + symbol + "&token=" + API_KEY;
            JSONObject profileJson = fetchJsonFromUrl(profileUrl);

            String name = profileJson.optString("name", "N/A");
            String industry = profileJson.optString("finnhubIndustry", "N/A");
            double marketCap = profileJson.optDouble("marketCapitalization", 0.0);
            long sharesOutstanding = (long) profileJson.optDouble("shareOutstanding", 0.0);

            return new CompanyProfile(name, industry, marketCap, sharesOutstanding);
        } catch (Exception e) {
            e.printStackTrace();
            return new CompanyProfile("N/A", "N/A", 0.0, 0L);
        }
    }

    public boolean isMarketOpen() {
        try {
            String url = "https://finnhub.io/api/v1/stock/market-status?exchange=US&token=" + API_KEY;
            JSONObject response = fetchJsonFromUrl(url);
            JSONObject json = fetchJsonFromUrl(url);
            return json.optBoolean("isOpen", false);
        } catch (Exception e) {
            System.err.println("Error checking market status: " + e.getMessage());
            return false;
        }
    }

    public void getCurrentPriceWithFallback(String symbol, Consumer<Double> onPriceUpdate) {
        if (isMarketOpen()) {
            try {
                FinnhubWebSocketClient wsClient = new FinnhubWebSocketClient(symbol) {
                    @Override
                    public void onMessage(String message) {
                        try {
                            JSONObject json = new JSONObject(message);
                            JSONArray data = json.optJSONArray("data");
                            if (data != null && data.length() > 0) {
                                double price = data.getJSONObject(0).optDouble("p", 0.0);
                                Platform.runLater(() -> onPriceUpdate.accept(price));
                            }
                        } catch (JSONException e) {
                            System.err.println("WebSocket JSON parse error: " + e.getMessage());
                        }
                    }
                };

                wsClient.startClient(); // starts the WebSocket

            } catch (URISyntaxException e) {
                // fallback to REST if WebSocket fails
                Stock stock = getQuoteForTicker(symbol);
                onPriceUpdate.accept(stock.getCurrentPrice());
            }
        } else {
            // fallback to REST
            Stock stock = getQuoteForTicker(symbol);
            onPriceUpdate.accept(stock.getCurrentPrice());
        }
    }
}