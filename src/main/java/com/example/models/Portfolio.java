package com.example.models;

import com.example.services.PortfolioIntegration;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String userId;
    private List<PortfolioEntry> transactions;
    private List<PortfolioEntry> holdings;
    private double balance;

    public Portfolio() {
        this.holdings = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.balance = 0.0;
    }

    public Portfolio(String userId, List<PortfolioEntry> holdings) {
        this.userId = userId;
        this.holdings = holdings;
        this.transactions = new ArrayList<>();
    }

    public Portfolio(String userId, List<PortfolioEntry> holdings, List<PortfolioEntry> transactions) {
        this.userId = userId;
        this.holdings = holdings;
        this.transactions = transactions;
    }

    public Portfolio(String userId, List<PortfolioEntry> holdings, List<PortfolioEntry> transactions, double balance) {
        this.userId = userId;
        this.holdings = holdings;
        this.transactions = transactions;
        this.balance = balance;
    }

    public void addOrUpdateEntry(PortfolioEntry entry) {
        String ticker = entry.getTickerSymbol().toUpperCase();

        for (int i = 0; i < holdings.size(); i++) {
            if (holdings.get(i).getTickerSymbol().equalsIgnoreCase(ticker)) {
                holdings.set(i, entry); // Replace existing
                return;
            }
        }
        holdings.add(entry);
    }

    public PortfolioEntry getEntryBySymbol(String symbol) {
        if (symbol == null) return null;
        for (PortfolioEntry entry : holdings) {
            if (entry.getTickerSymbol().equalsIgnoreCase(symbol)) {
                return entry;
            }
        }
        return null;
    }

    public void addTransaction(PortfolioEntry transaction) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(transaction);
    }

    public double getTotalValue(){
        if(holdings == null || holdings.isEmpty()){
            return 0.0;
        }
        double total = 0.0;
        for (PortfolioEntry entry : holdings) {
            Double currentPrice = entry.getCurrentPrice();
            if(currentPrice == null){
                total += currentPrice * entry.getTotalValue();
            }
        }
        return total;
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

    public List<PortfolioEntry> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PortfolioEntry> transactions) {
        this.transactions = transactions;
    }

    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
}