package com.example.csc325capstone;

import com.google.cloud.firestore.annotation.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class PortfolioEntry {

    private String tickerSymbol;
    private int totalShares;
    private double averageBuyPrice;
    private double currentMarketPrice;
    private double unrealizedGainLoss;
    private double totalValue;

    public PortfolioEntry() {}

    public PortfolioEntry(String tickerSymbol, int totalShares, double averageBuyPrice, double currentMarketPrice, double unrealizedGainLoss, double totalValue) {
        this.tickerSymbol = tickerSymbol;
        this.totalShares = totalShares;
        this.averageBuyPrice = averageBuyPrice;
        this.currentMarketPrice = currentMarketPrice;
        this.unrealizedGainLoss = unrealizedGainLoss;
        this.totalValue = totalValue;

    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tickerSymbol", tickerSymbol);
        map.put("totalShares", totalShares);
        map.put("averageBuyPrice", averageBuyPrice);
        map.put("currentMarketPrice", currentMarketPrice);
        map.put("unrealizedGainLoss", unrealizedGainLoss);
        map.put("totalValue", totalValue);
        return map;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }
    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public double getCurrentMarketPrice() {
        return currentMarketPrice;
    }

    public void setCurrentMarketPrice(double currentMarketPrice) {
        this.currentMarketPrice = currentMarketPrice;
    }

    public double getUnrealizedGainLoss() {
        return unrealizedGainLoss;
    }

    public void setUnrealizedGainLoss(double unrealizedGainLoss) {
        this.unrealizedGainLoss = unrealizedGainLoss;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
}