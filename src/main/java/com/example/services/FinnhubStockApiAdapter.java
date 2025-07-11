package com.example.services;

import com.example.models.Stock;
import com.example.models.CompanyProfile;

import java.util.List;
import java.util.ArrayList;

public class FinnhubStockApiAdapter implements StockApiService {

    private final FinnhubService finnhubService;

    public FinnhubStockApiAdapter() {
        this.finnhubService = new FinnhubService();
    }

    @Override
    public Stock getStockData(String tickerSymbol) throws Exception {
        try {
            Stock stock = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
            if (stock == null) {
                throw new Exception("Stock data not found for ticker: " + tickerSymbol);
            }
            return stock;
        } catch (Exception e) {
            throw new Exception("Failed to fetch stock data for " + tickerSymbol + ": " + e.getMessage(), e);
        }
    }

    @Override
    public CompanyProfile getCompanyProfile(String tickerSymbol) throws Exception {
        try {
            CompanyProfile profile = finnhubService.getCompanyProfile(tickerSymbol.toUpperCase());
            if (profile == null || "N/A".equals(profile.getName())) {
                throw new Exception("Company profile not found for ticker: " + tickerSymbol);
            }
            return profile;
        } catch (Exception e) {
            throw new Exception("Failed to fetch company profile for " + tickerSymbol + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> searchStocks(String query) throws Exception {
        List<String> results = new ArrayList<>();
        try {
            String upperQuery = query.toUpperCase();
            Stock stock = finnhubService.getQuoteForTicker(upperQuery);
            if (stock != null && stock.getCurrentPrice() > 0) {
                results.add(upperQuery);
            }
        } catch (Exception e) {
            // If the ticker doesn't exist, that's fine - just return empty results
        }
        return results;
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            Stock testStock = finnhubService.getQuoteForTicker("AAPL");
            return testStock != null;
        } catch (Exception e) {
            return false;
        }
    }

    // Additional Finnhub-specific methods
    public List<String> getMarketNews() {
        return finnhubService.getMarketNews();
    }

    public List<String> getCompanyNews(String symbol) {
        return finnhubService.getCompanyNews(symbol);
    }

    public boolean isMarketOpen() {
        return finnhubService.isMarketOpen();
    }
}