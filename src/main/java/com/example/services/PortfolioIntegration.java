package com.example.services;

import com.example.models.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.*;

public class PortfolioIntegration {

    private final Firestore db;
    private final FinnhubService finnhubService;
    private final UserSession userSession;
    private final Portfolio portfolio;

    public PortfolioIntegration(Firestore db, Portfolio portfolio) {
        this.db = db;
        this.finnhubService = new FinnhubService();
        this.userSession = UserSession.getInstance();
        this.portfolio = portfolio;
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
                documentToPortfolio(this.portfolio, doc); // ✅ update shared instance
                return this.portfolio;
            } else {
                this.portfolio.setUserId(userId);
                savePortfolio(this.portfolio); // ✅ save the same instance
                return this.portfolio;
            }

        } catch (Exception e) {
            throw new Exception("Failed to get portfolio: " + e.getMessage());
        }
    }

    public void buyStock(String tickerSymbol, int quantity, double pricePerShare, Date buyDate) throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        if (quantity <= 0) throw new Exception("Quantity must be positive");
        if (pricePerShare <= 0) throw new Exception("Price must be positive");

        Stock stockData = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        if (stockData == null) {
            throw new Exception("Invalid ticker symbol: " + tickerSymbol);
        }

        PortfolioEntry existingEntry = this.portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equals(tickerSymbol.toUpperCase()))
                .findFirst()
                .orElse(null);

        PortfolioEntry updatedEntry;

        if (existingEntry != null) {
            int newTotalShares = existingEntry.getTotalShares() + quantity;
            double totalCost = (existingEntry.getTotalShares() * existingEntry.getBuyPrice()) + (quantity * pricePerShare);
            double newAveragePrice = totalCost / newTotalShares;
            updatedEntry = PortfolioEntry.fromStock(stockData, newTotalShares, newAveragePrice);
        } else {
            updatedEntry = PortfolioEntry.fromStock(stockData, quantity, pricePerShare);
        }

        this.portfolio.addOrUpdateEntry(updatedEntry);
        saveTransaction(tickerSymbol.toUpperCase(), quantity, pricePerShare, true, buyDate);
        savePortfolio(this.portfolio);

        System.out.printf("Bought %d shares of %s at $%.2f\n", quantity, tickerSymbol, pricePerShare);
    }

    public void sellStock(String tickerSymbol, int quantity, double pricePerShare, Date sellDate) throws Exception {
        if (!userSession.isLoggedIn()) {
            throw new Exception("User must be logged in");
        }

        // Find the holding in the existing portfolio
        PortfolioEntry existingEntry = this.portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equals(tickerSymbol.toUpperCase()))
                .findFirst()
                .orElse(null);

        if (existingEntry == null) {
            throw new Exception("No position found for " + tickerSymbol);
        }

        if (existingEntry.getTotalShares() < quantity) {
            throw new Exception("Insufficient shares. Available: " + existingEntry.getTotalShares());
        }

        // Get current stock data
        Stock stockData = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        int newTotalShares = existingEntry.getTotalShares() - quantity;

        if (newTotalShares == 0) {
            // Sold all shares, remove the holding
            portfolio.removeEntry(tickerSymbol.toUpperCase());
        } else {
            // Update remaining holding with adjusted shares and same buy price
            PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                    stockData,
                    newTotalShares,
                    existingEntry.getBuyPrice()
            );
            portfolio.addOrUpdateEntry(updatedEntry);
        }

        // Record the sale as a transaction
        saveTransaction(tickerSymbol.toUpperCase(), quantity, pricePerShare, false, sellDate);

        // Save updated portfolio to Firestore
        savePortfolio(portfolio);

        // Console log
        System.out.printf("Sold %d shares of %s at $%.2f\n", quantity, tickerSymbol, pricePerShare);
    }

    public void refreshPortfolioPrices() throws Exception {
        if (this.portfolio.getHoldings().isEmpty()) {
            System.out.println("No holdings to update");
            return;
        }

        // Gather ticker symbols to query Finnhub in batch
        List<String> tickers = new ArrayList<>();
        for (PortfolioEntry entry : this.portfolio.getHoldings()) {
            tickers.add(entry.getTickerSymbol());
        }

        // Fetch updated quotes from Finnhub
        Map<String, Stock> stockData = finnhubService.getQuotesForTickers(tickers);

        List<PortfolioEntry> updatedHoldings = new ArrayList<>();

        for (PortfolioEntry entry : this.portfolio.getHoldings()) {
            Stock stock = stockData.get(entry.getTickerSymbol());
            if (stock != null) {
                PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                        stock,
                        entry.getTotalShares(),
                        entry.getBuyPrice()  // Keep same buy price
                );
                updatedHoldings.add(updatedEntry);
            } else {
                // If API failed or ticker is invalid, retain old entry
                updatedHoldings.add(entry);
            }
        }

        // Save the updated holdings
        this.portfolio.setHoldings(updatedHoldings);
        savePortfolio(this.portfolio);

        System.out.println("Portfolio prices updated");
    }


    public Map<String, Object> getPortfolioSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("userName", userSession.getUserFullName());

        List<PortfolioEntry> holdings = this.portfolio.getHoldings();

        // Total Value = sum of (current price * shares)
        double totalValue = holdings.stream()
                .mapToDouble(entry -> entry.getCurrentPrice() * entry.getTotalShares())
                .sum();
        summary.put("totalValue", totalValue);

        // Unrealized Gain/Loss = (currentPrice - buyPrice) * shares
        double totalUnrealizedGainLoss = holdings.stream()
                .mapToDouble(entry -> (entry.getCurrentPrice() - entry.getBuyPrice()) * entry.getTotalShares())
                .sum();
        summary.put("totalUnrealizedGainLoss", totalUnrealizedGainLoss);

        // Number of holdings
        summary.put("numberOfHoldings", holdings.size());

        // Total invested = sum of (buy price * shares)
        double totalInvested = holdings.stream()
                .mapToDouble(entry -> entry.getBuyPrice() * entry.getTotalShares())
                .sum();
        summary.put("totalInvested", totalInvested);

        // % Gain/Loss = (UnrealizedGainLoss / Total Invested) * 100
        double percentageGainLoss = totalInvested > 0
                ? (totalUnrealizedGainLoss / totalInvested) * 100
                : 0.0;
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

        List<Map<String, Object>> transactionsData = new ArrayList<>();
        for (PortfolioEntry tx : portfolio.getTransactions()) {
            transactionsData.add(tx.toMap());
        }
        portfolioData.put("transactions", transactionsData);

        portfolioData.put("lastUpdated", new Date());

        db.collection("users")
                .document(userId)
                .collection("portfolio")
                .document("main")
                .set(portfolioData)
                .get();
    }

    private void saveTransaction(String tickerSymbol, int quantity, double pricePerShare, boolean isBuy, Date buyDate) throws Exception {
        String userId = userSession.getUserUid();

        // Use existing portfolio object
        Optional<PortfolioEntry> existingEntry = this.portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equalsIgnoreCase(tickerSymbol))
                .findFirst();

        int newTotalShares;
        double newAverage;
        PortfolioEntry updatedEntry;

        if (existingEntry.isPresent()) {
            PortfolioEntry entry = existingEntry.get();

            newTotalShares = isBuy
                    ? entry.getTotalShares() + quantity
                    : entry.getTotalShares() - quantity;

            double updatedCost = isBuy
                    ? (entry.getTotalShares() * entry.getBuyPrice()) + (quantity * pricePerShare)
                    : (entry.getTotalShares() * entry.getBuyPrice()) - (quantity * pricePerShare);

            newAverage = newTotalShares == 0 ? 0 : updatedCost / newTotalShares;

            updatedEntry = new PortfolioEntry(
                    tickerSymbol.toUpperCase(),
                    newTotalShares,
                    newAverage,
                    buyDate
            );
        } else {
            // First-time buy
            updatedEntry = new PortfolioEntry(
                    tickerSymbol.toUpperCase(),
                    quantity,
                    pricePerShare,
                    buyDate
            );
        }

        // Update or insert holding
        this.portfolio.addOrUpdateEntry(updatedEntry);

        // Record the transaction
        PortfolioEntry transaction = new PortfolioEntry(
                tickerSymbol.toUpperCase(),
                quantity,
                pricePerShare,
                buyDate
        );
        this.portfolio.addTransaction(transaction);

        // Save transaction to Firestore (subcollection)
        Map<String, Object> txData = transaction.toMap();
        db.collection("users")
                .document(userId)
                .collection("portfolio")
                .document("main")
                .collection("transactions")
                .add(txData);
    }

    private void documentToPortfolio(Portfolio portfolio, DocumentSnapshot doc) {
        portfolio.setUserId((String) doc.get("userId"));

        List<Map<String, Object>> holdingsData = (List<Map<String, Object>>) doc.get("holdings");
        if (holdingsData != null) {
            List<PortfolioEntry> holdings = new ArrayList<>();
            for (Map<String, Object> entryData : holdingsData) {
                PortfolioEntry entry = PortfolioEntry.fromMap(entryData);
                holdings.add(entry);
            }
            portfolio.setHoldings(holdings);
        }

        List<Map<String, Object>> transactionsData = (List<Map<String, Object>>) doc.get("transactions");
        if (transactionsData != null) {
            List<PortfolioEntry> transactions = new ArrayList<>();
            for (Map<String, Object> txData : transactionsData) {
                PortfolioEntry tx = PortfolioEntry.fromMap(txData);
                transactions.add(tx);
            }
            portfolio.setTransactions(transactions);
        }
    }
}
