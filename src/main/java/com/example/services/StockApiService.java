package com.example.services;

import com.example.models.Stock;
import com.example.models.CompanyProfile;

public interface StockApiService {
    Stock getStockData(String tickerSymbol) throws Exception;
    CompanyProfile getCompanyProfile(String tickerSymbol) throws Exception;
    java.util.List<String> searchStocks(String query) throws Exception;
    boolean isServiceAvailable();
}
