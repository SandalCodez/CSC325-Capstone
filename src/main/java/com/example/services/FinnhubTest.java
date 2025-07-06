package com.example.services;

import com.example.models.Stock;

import java.util.List;
import java.util.Map;

public class FinnhubTest {
    public static void main(String[] args) {
        FinnhubService service = new FinnhubService();
        List<String> tickers = List.of("AAPL", "GOOGL", "NVDA");

        Map<String, Stock> stockMap = service.getQuotesForTickers(tickers);
        for(Map.Entry<String, Stock> entry : stockMap.entrySet()){
            System.out.println("Ticker: " + entry.getKey() + ", Data" + entry.getValue());
            System.out.println();
        }

        String company = service.getCompanyName("AAPL");
        System.out.println("Company Name: " + company);
    }
}
