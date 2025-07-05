package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String userId;
    private List<PortfolioEntry> holdings;

    public Portfolio() {
        this.holdings = new ArrayList<>();
    }

    public Portfolio(String userId, List<PortfolioEntry> holdings) {
        this.userId = userId;
        this.holdings = holdings;
    }

    public void addOrUpdateEntry(PortfolioEntry entry) {
        for(int i = 0; i < holdings.size(); i++){
            PortfolioEntry existing = holdings.get(i);
            if(existing.getTickerSymbol().equals(entry.getTickerSymbol())){
                holdings.set(i, entry);
                return;
            }
        }
        holdings.add(entry);
    }

    public double getTotalPortfolioValue() {
        return holdings.stream()
                .mapToDouble(PortfolioEntry::getTotalValue)
                .sum();
    }

    public double getTotalUnrealizedGainLoss(){
        return holdings.stream()
                .mapToDouble(PortfolioEntry::getUnrealizedGainLoss)
                .sum();
    }

    public double getWeights(String tickerSymbol) {
        double total = getTotalPortfolioValue();
        for(PortfolioEntry entry : holdings){
            if(entry.getTickerSymbol().equals(tickerSymbol)){
                return total > 0 ? entry.getTotalValue() / total : 0;
            }
        }
        return 0;
    }

    public void removeEntry(String tickerSymbol) {
        holdings.removeIf(entry -> entry.getTickerSymbol().equals(tickerSymbol));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PortfolioEntry> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<PortfolioEntry> holdings) {
        this.holdings = holdings;
    }
}