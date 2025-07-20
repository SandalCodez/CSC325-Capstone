package com.example.services;

import com.example.models.*;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentSnapshot;
import org.slf4j.ILoggerFactory;

import java.util.Date;
import java.util.*;

public class PortfolioIntegration {

    private final Firestore db;
    private final FinnhubService finnhubService;
    private User loggedInUser;
    private final Portfolio portfolio;
    private UserAuth userAuth;
    private Runnable onSellCompleteCallback;
    private String uid;


    public PortfolioIntegration(Firestore db, FinnhubService finnhubService, User loggedInUser, Portfolio portfolio) {
        this.db = db;
        this.finnhubService = new FinnhubService();
        this.loggedInUser = loggedInUser;
        this.portfolio = portfolio;
    }

    public PortfolioIntegration(Firestore db, FinnhubService finnhubService, Portfolio portfolio) {
        this.db = db;
        this.finnhubService = new FinnhubService();
        this.portfolio = portfolio;
    }

    public PortfolioIntegration(Firestore db, FinnhubService finnhubService, UserAuth userAuth, User loggedInUser, Portfolio portfolio) {
        this.db = db;
        this.finnhubService = finnhubService;
        this.userAuth = userAuth;
        this.loggedInUser = loggedInUser;
        this.portfolio = portfolio;
    }



    public Portfolio getUserPortfolio() throws Exception {
        if (this.loggedInUser == null) {
            throw new Exception("User must be logged in");
        }

        String userId = this.loggedInUser.getUserUid();

        try {
            DocumentSnapshot doc = db.collection("users")
                    .document(userId)
                    .collection("portfolio")
                    .document("main")
                    .get()
                    .get();

            if (doc.exists()) {
                documentToPortfolio(this.portfolio, doc); //
                return this.portfolio;
            } else {
                this.portfolio.setUserId(userId);
                savePortfolio(this.portfolio);
                return this.portfolio;
            }

        } catch (Exception e) {
            throw new Exception("Failed to get portfolio: " + e.getMessage());
        }
    }

    public void buyStock(String tickerSymbol, String companyName, int quantity, double pricePerShare, Date buyDate) throws Exception {
        if (this.loggedInUser == null) {
            throw new Exception("User must be logged in");
        }

        System.out.println("DEBUG Portfolio Integration balance " + loggedInUser.getAccountBalance());
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
        double totalCost;

        if (existingEntry != null) {
            int newTotalShares = existingEntry.getTotalShares() + quantity;
            totalCost =  + quantity * pricePerShare;
            double newAveragePrice = ((existingEntry.getTotalShares() * existingEntry.getBuyPrice()) + (quantity * pricePerShare)) / newTotalShares;
            updatedEntry = PortfolioEntry.fromStock(tickerSymbol, companyName, newTotalShares, newAveragePrice, buyDate, stockData.getCurrentPrice());
        } else {
            int newTotalShares = quantity;
            totalCost = (quantity * pricePerShare);
            double newAveragePrice = pricePerShare;
            updatedEntry = PortfolioEntry.fromStock(tickerSymbol, companyName, newTotalShares, newAveragePrice, buyDate, stockData.getCurrentPrice());
        }

        System.out.println();
        double currentBalance = loggedInUser.getAccountBalance();
        System.out.println("Total cost debug: " + totalCost);
        System.out.println("Current balance debug infinity: " + currentBalance);
        if (currentBalance < totalCost) {
            throw new Exception("Insufficient Funds");
        }
        double newBalance = loggedInUser.getAccountBalance() - totalCost;
        loggedInUser.setAccountBalance(newBalance);
        saveTransaction(tickerSymbol.toUpperCase(), quantity, pricePerShare, true, buyDate);
        savePortfolio(this.portfolio);

        String uid = this.loggedInUser.getUserUid();
        userAuth.updateUserBalance(uid, newBalance);
        System.out.printf("Bought %d shares of %s at $%.2f\n", quantity, tickerSymbol, pricePerShare);
    }

    public void sellStock(String tickerSymbol, int quantity, double pricePerShare, Date sellDate) throws Exception {
        System.out.println("SELL START: quantity = " + quantity + ", stock ticker = " + tickerSymbol);

        if (this.loggedInUser == null) {
            throw new Exception("User must be logged in");
        }

        Stock stockData = finnhubService.getQuoteForTicker(tickerSymbol.toUpperCase());
        if (stockData == null) {
            throw new Exception("Invalid ticker symbol: " + tickerSymbol);
        }

        PortfolioEntry existingEntry = this.portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equalsIgnoreCase(tickerSymbol))
                .findFirst()
                .orElse(null);

        if (existingEntry == null) {
            throw new Exception("No position found for " + tickerSymbol);
        }

        if (existingEntry.getTotalShares() < quantity) {
            throw new Exception("Insufficient shares. Available: " + existingEntry.getTotalShares());
        }

        int newTotalShares = existingEntry.getTotalShares() - quantity;

        if (newTotalShares == 0) {
            // Sold all shares, remove the holding
            portfolio.removeEntry(tickerSymbol.toUpperCase());
        } else {
            // Still have shares left, update the holding
            PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                    tickerSymbol.toUpperCase(),
                    existingEntry.getCompanyName(),
                    newTotalShares,
                    existingEntry.getBuyPrice(),
                    existingEntry.getBuyDate(),
                    stockData.getCurrentPrice()
            );
            portfolio.addOrUpdateEntry(updatedEntry);
        }

        // Record the sale as a transaction
        saveSellTransaction(tickerSymbol.toUpperCase(), existingEntry.getCompanyName(), quantity, pricePerShare,  sellDate);

        // Update user's account balance (they received money)
        double proceeds = quantity * pricePerShare;
        double updatedBalance = loggedInUser.getAccountBalance() + proceeds;

        loggedInUser.setAccountBalance(updatedBalance);
        userAuth.updateUserBalance(loggedInUser.getUserUid(), updatedBalance);
        System.out.println("Updated shares for: " + existingEntry.getTickerSymbol() + ": " + existingEntry.getTotalShares());
        savePortfolio(this.portfolio);

        if (onSellCompleteCallback != null) {
            onSellCompleteCallback.run(); // example usage
        }
    }

    public void setOnSellCompleteCallback(Runnable callback) {
        this.onSellCompleteCallback = callback;
    }


    public void refreshPortfolioPrices() throws Exception {
        if (this.portfolio.getHoldings().isEmpty()) {
            return;
        }

        // Preserve the current balance before updating holdings
        double existingBalance = this.portfolio.getBalance();

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
                String companyName = stock.getCompanyName();

                PortfolioEntry updatedEntry = PortfolioEntry.fromStock(
                        entry.getTickerSymbol(),
                        companyName,
                        entry.getTotalShares(),
                        entry.getBuyPrice(),
                        entry.getBuyDate(),
                        stock.getCurrentPrice()
                );

                updatedHoldings.add(updatedEntry);
            }
        }

        // Update portfolio holdings
        this.portfolio.setHoldings(updatedHoldings);

        // Restore the previously loaded balance
        this.portfolio.setBalance(existingBalance);

        // Optional: Log for verification
        System.out.println("Portfolio prices updated, balance preserved: " + existingBalance);
    }


    public Map<String, Object> getPortfolioSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        summary.put("userName", this.loggedInUser.getUserFullName());

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
        if (this.loggedInUser == null) {
            throw new Exception("User must be logged in");
        }

        String userId = this.loggedInUser.getUserUid();
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
    private void savePortfolio(Portfolio portfolio, String tickerSymbol) throws Exception {
        String userId = this.loggedInUser.getUserUid();

        Stock stock =finnhubService.getQuoteForTicker(tickerSymbol);
        double currentPrice = stock.getCurrentPrice();
        String companyName = finnhubService.getCompanyName(tickerSymbol);

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

    private void savePortfolio(Portfolio portfolio) throws Exception {
        String userId = this.loggedInUser.getUserUid();

        Map<String, Object> portfolioData = new HashMap<>();
        portfolioData.put("userId", portfolio.getUserId());

        List<Map<String, Object>> holdingsData = new ArrayList<>();
        System.out.println("Holdings before save:");
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
        String userId = this.loggedInUser.getUserUid();
        Stock stock = finnhubService.getQuoteForTicker(tickerSymbol);
        double currentPrice = stock.getCurrentPrice();

        Optional<PortfolioEntry> existingEntryOpt = this.portfolio.getHoldings().stream()
                .filter(entry -> entry.getTickerSymbol().equalsIgnoreCase(tickerSymbol))
                .findFirst();

        int newTotalShares;
        double newAverage;
        PortfolioEntry updatedEntry;

        if (existingEntryOpt.isPresent()) {
            PortfolioEntry entry = existingEntryOpt.get();

            newTotalShares = isBuy
                    ? entry.getTotalShares() + quantity
                    : entry.getTotalShares() - quantity;

            double updatedCost = isBuy
                    ? (entry.getTotalShares() * entry.getBuyPrice()) + (quantity * pricePerShare)
                    : (entry.getTotalShares() * entry.getBuyPrice()) - (quantity * pricePerShare);

            newAverage = newTotalShares == 0 ? 0 : updatedCost / newTotalShares;


            String companyName = finnhubService.getCompanyName(tickerSymbol);

            updatedEntry = PortfolioEntry.fromStock(
                    tickerSymbol.toUpperCase(),
                    companyName,
                    newTotalShares,
                    newAverage,
                    buyDate,
                    currentPrice
            );

        } else {
            // First-time buy
            String companyName = finnhubService.getCompanyName(tickerSymbol);

            updatedEntry = PortfolioEntry.fromStock(
                    tickerSymbol.toUpperCase(),
                    companyName,
                    quantity,
                    pricePerShare,
                    buyDate,
                    pricePerShare
            );
        }

        // Add or update the holding
        this.portfolio.addOrUpdateEntry(updatedEntry);

        // Record the transaction
        PortfolioEntry transaction = PortfolioEntry.fromStock(
                tickerSymbol.toUpperCase(),
                updatedEntry.getCompanyName(),  // or just companyName
                quantity,
                pricePerShare,
                buyDate,
                currentPrice
        );

        this.portfolio.addTransaction(transaction);
    }

    private void saveSellTransaction(String tickerSymbol, String companyName, int quantity, double pricePerShare, Date sellDate) throws Exception {
        double currentPrice = finnhubService.getQuoteForTicker(tickerSymbol).getCurrentPrice();

        // Just log the sell transaction — do NOT modify holdings
        PortfolioEntry transaction = PortfolioEntry.fromStock(
                tickerSymbol,
                companyName,
                quantity,
                pricePerShare,   // sell price
                sellDate,
                currentPrice     // current price from API
        );

        this.portfolio.addTransaction(transaction);

        // Save only the transaction (holdings already saved in sellStock)
        savePortfolio(this.portfolio);
    }

    private void documentToPortfolio(Portfolio portfolio, DocumentSnapshot doc) {
        portfolio.setUserId((String) doc.get("userId"));

        List<?> holdingsData = (List<?>) doc.get("holdings");
        if (holdingsData != null) {
            List<PortfolioEntry> holdings = new ArrayList<>();
            for (Object obj : holdingsData) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> entryData = (Map<String, Object>) obj;
                    PortfolioEntry entry = PortfolioEntry.fromMap(entryData);
                    holdings.add(entry);
                }
            }
            portfolio.setHoldings(holdings);
        }

        List<?> transactionsData = (List<?>) doc.get("transactions");
        if (transactionsData != null) {
            List<PortfolioEntry> transactions = new ArrayList<>();
            for (Object obj : transactionsData) {
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> txData = (Map<String, Object>) obj;
                    PortfolioEntry tx = PortfolioEntry.fromMap(txData);
                    transactions.add(tx);
                }
            }
            portfolio.setTransactions(transactions);
        }
    }
}
