package com.example.services;

import com.example.models.Stock;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
                double high = quoteJson.optDouble("h", 0.0);
                double low = quoteJson.optDouble("l", 0.0);
                double open = quoteJson.optDouble("o", 0.0);
                double previousClose = quoteJson.optDouble("pc", 0.0);
                double volume = quoteJson.optDouble("v", 0.0);

                // Fetch company name
                JSONObject companyJson = fetchJsonFromUrl(companyUrl);
                String companyName = companyJson.optString("name", ticker);

                Stock stock = new Stock(ticker, companyName, currentPrice, high, low, open, volume, previousClose);
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
}