package com.example.services;

import com.example.models.Stock;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class FinnhubTest {
    public static void main(String[] args) {
        FinnhubService service = new FinnhubService();
        List<String> tickers = List.of("AAPL", "GOOGL", "NVDA");

        Map<String, Stock> stockMap = service.getQuotesForTickers(tickers);
        for (Map.Entry<String, Stock> entry : stockMap.entrySet()) {
            System.out.println("Ticker: " + entry.getKey() + ", Data" + entry.getValue());
            System.out.println();
        }

        String company = service.getCompanyName("AAPL");
        System.out.println("Company Name: " + company);

        System.out.println("\n--- WebSocket Test ---");
        runWebSocketTest();
        System.out.println();
        runHistoricalPriceTest();

    }


    public static void runWebSocketTest() {
        try {
            FinnhubWebSocketClient wsClient = new FinnhubWebSocketClient("AAPL");
            wsClient.startClient();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runHistoricalPriceTest() {
        try {
            FinnhubService service = new FinnhubService();

            LocalDate date = LocalDate.of(2025, 4, 9);

            long fromUnix = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            long toUnix = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();


            JsonObject result = service.getHistoricalPrices("AAPL", fromUnix, toUnix);

            System.out.println("--- Historical Price Test ---");
            System.out.println(result.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
