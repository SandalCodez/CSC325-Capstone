package com.example.services;

import com.example.models.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;

import java.time.LocalDateTime;
import java.util.*;

public class PortfolioIntegration {

    private final Firestore db;
    private final FinnhubService finnhubService;
    private final UserSession userSession;

    public PortfolioIntegration(Firestore db) {
        this.db = db;
        this.finnhubService = new FinnhubService();
        this.userSession = UserSession.getInstance();
    }

    public Portfolio getUserPortfolio() throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        String userId = userSession.getUserUid();

        try {
            DocumentSnapshot doc = db.collection("users")
                    .document(userId)
                    .collection("portfolio")
                    .document("main")
                    .get()
                    .get();

            if (doc.exists()) {
                return documentToPortfolio(doc);
            } else {
                Portfolio newPortfolio = new Portfolio();
                newPortfolio.setUserId(userId);
                savePortfolio(newPortfolio);
                return newPortfolio;
            }
        } catch (Exception e) {
            throw new Exception("Failed to get portfolio: " + e.getMessage());
        }
    }

    public void buyStock(String tickerSymbol, int quantity, double pricePerShare) throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        if (quantity <= 0) throw new Exception("Quantity must be positive");
        if (pricePerShare <= 0) throw new Exception("Price must be positive");

        Stock stockData = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        if (stockData == null) {
            throw new Exception("Invalid ticker symbol: " + tickerSymbol);
        }

        Portfolio portfolio = getUserPortfolio();

        PortfolioEntry existingEntry = portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equals(tickerSymbol.toUpperCase()))
                .findFirst()
                .orElse(null);

        PortfolioEntry updatedEntry;

        if (existingEntry != null) {
            int newTotalShares = existingEntry.getTotalShares() + quantity;
            double totalCost = (existingEntry.getTotalShares() * existingEntry.getAverageBuyPrice()) +
                    (quantity * pricePerShare);
            double newAveragePrice = totalCost / newTotalShares;

            updatedEntry = PortfolioEntry.fromStock(stockData, newTotalShares, newAveragePrice);
        } else {
            updatedEntry = PortfolioEntry.fromStock(stockData, quantity, pricePerShare);
        }

        portfolio.addOrUpdateEntry(updatedEntry);
        saveTransaction(tickerSymbol.toUpperCase(), quantity, pricePerShare, true);
        savePortfolio(portfolio);

        System.out.printf("Bought %d shares of %s at $%.2f%n", quantity, tickerSymbol, pricePerShare);
    }

    public void sellStock(String tickerSymbol, int quantity, double pricePerShare) throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        Portfolio portfolio = getUserPortfolio();

        PortfolioEntry existingEntry = portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equals(tickerSymbol.toUpperCase()))
                .findFirst()
                .orElse(null);

        if (existingEntry == null) {
            throw new Exception("No position found for " + tickerSymbol);
        }

        if (existingEntry.getTotalShares() < quantity) {
            throw new Exception("Insufficient shares. Available: " + existingEntry.getTotalShares());
        }

        Stock stockData = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        int newTotalShares = existingEntry.getTotalShares() - quantity;

        if (newTotalShares == 0) {
            portfolio.removeEntry(tickerSymbol.toUpperCase());
        } else {
            PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                    stockData,
                    newTotalShares,
                    existingEntry.getAverageBuyPrice()
            );
            portfolio.addOrUpdateEntry(updatedEntry);
        }

        saveTransaction(tickerSymbol.toUpperCase(), quantity, pricePerShare, false);
        savePortfolio(portfolio);

        System.out.printf("Sold %d shares of %s at $%.2f%n", quantity, tickerSymbol, pricePerShare);
    }

    public void refreshPortfolioPrices() throws Exception {
        Portfolio portfolio = getUserPortfolio();

        if (portfolio.getHoldings().isEmpty()) {
            System.out.println("No holdings to update");
            return;
        }

        List<String> tickers = new ArrayList<>();
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            tickers.add(entry.getTickerSymbol());
        }

        Map<String, Stock> stockData = finnhubService.getQuotesForTickers(tickers);

        List<PortfolioEntry> updatedHoldings = new ArrayList<>();
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            Stock stock = stockData.get(entry.getTickerSymbol());
            if (stock != null) {
                PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                        stock,
                        entry.getTotalShares(),
                        entry.getAverageBuyPrice()
                );
                updatedHoldings.add(updatedEntry);
            } else {
                updatedHoldings.add(entry);
            }
        }

        portfolio.setHoldings(updatedHoldings);
        savePortfolio(portfolio);

        System.out.println("Portfolio prices updated");
    }

    public Map<String, Object> getPortfolioSummary() throws Exception {
        Portfolio portfolio = getUserPortfolio();

        Map<String, Object> summary = new HashMap<>();
        summary.put("userName", userSession.getUserFullName());
        summary.put("totalValue", portfolio.getTotalPortfolioValue());
        summary.put("totalUnrealizedGainLoss", portfolio.getTotalUnrealizedGainLoss());
        summary.put("numberOfHoldings", portfolio.getHoldings().size());

        double totalInvested = portfolio.getHoldings().stream()
                .mapToDouble(entry -> entry.getAverageBuyPrice() * entry.getTotalShares())
                .sum();
        summary.put("totalInvested", totalInvested);

        double percentageGainLoss = totalInvested > 0 ?
                (portfolio.getTotalUnrealizedGainLoss() / totalInvested) * 100 : 0;
        summary.put("percentageGainLoss", percentageGainLoss);

        return summary;
    }

    public List<Transaction> getTransactionHistory() throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        String userId = userSession.getUserUid();
        List<Transaction> transactions = new ArrayList<>();

        try {
            var querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get()
                    .get();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Transaction transaction = doc.toObject(Transaction.class);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading transaction history: " + e.getMessage());
        }

        return transactions;
    }

    public Stock getStockQuote(String tickerSymbol) throws Exception {
        Stock stock = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        if (stock == null) {
            throw new Exception("Stock not found: " + tickerSymbol);
        }
        return stock;
    }

    public List<String> getMarketNews() {
        return finnhubService.getMarketNews();
    }

    public List<String> getCompanyNews(String tickerSymbol) {
        return finnhubService.getCompanyNews(tickerSymbol.toUpperCase());
    }

    public boolean isMarketOpen() {
        return finnhubService.isMarketOpen();
    }

    // Private helper methods
    private void savePortfolio(Portfolio portfolio) throws Exception {
        String userId = userSession.getUserUid();

        Map<String, Object> portfolioData = new HashMap<>();
        portfolioData.put("userId", portfolio.getUserId());

        List<Map<String, Object>> holdingsData = new ArrayList<>();
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            holdingsData.add(entry.toMap());
        }
        portfolioData.put("holdings", holdingsData);
        portfolioData.put("lastUpdated", new Date());

        db.collection("users")
                .document(userId)
                .collection("portfolio")
                .document("main")
                .set(portfolioData)
                .get();
    }

    private void saveTransaction(String tickerSymbol, int quantity, double pricePerShare, boolean isBuy) throws Exception {
        String userId = userSession.getUserUid();

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                tickerSymbol,
                quantity,
                pricePerShare,
                isBuy,
                LocalDateTime.now()
        );

        db.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.getId())
                .set(transaction.toMap())
                .get();
    }

    private Portfolio documentToPortfolio(DocumentSnapshot doc) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId((String) doc.get("userId"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> holdingsData = (List<Map<String, Object>>) doc.get("holdings");

        if (holdingsData != null) {
            List<PortfolioEntry> holdings = new ArrayList<>();
            for (Map<String, Object> holdingData : holdingsData) {
                PortfolioEntry entry = new PortfolioEntry();
                entry.setTickerSymbol((String) holdingData.get("tickerSymbol"));
                entry.setCompanyName((String) holdingData.get("companyName"));
                entry.setTotalShares(((Number) holdingData.get("totalShares")).intValue());
                entry.setAverageBuyPrice(((Number) holdingData.get("averageBuyPrice")).doubleValue());
                entry.setCurrentMarketPrice(((Number) holdingData.get("currentMarketPrice")).doubleValue());
                entry.setUnrealizedGainLoss(((Number) holdingData.get("unrealizedGainLoss")).doubleValue());
                entry.setTotalValue(((Number) holdingData.get("totalValue")).doubleValue());
                holdings.add(entry);
            }
            portfolio.setHoldings(holdings);
        }

        return portfolio;
    }
}
