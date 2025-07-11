package com.example.services;

import com.example.models.*;
import com.example.services.*;
import com.google.cloud.firestore.Firestore;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Complete working demo of the portfolio system - CORRECTED VERSION
 */
public class PortfolioDemo {

    public static void main(String[] args) {
        try {
            // Initialize Firebase
            FirestoreDB firestoreDB = new FirestoreDB();
            Firestore db = firestoreDB.connect();

            // Initialize services
            UserAuth userAuth = new UserAuth(db);
            PortfolioIntegration portfolioService = new PortfolioIntegration(db);
            UserSession session = UserSession.getInstance();

            System.out.println("=== Portfolio Management System Demo ===\n");

            // Demo user login (replace with actual credentials)
            demonstrateUserAuth(userAuth, session);

            // Demo portfolio operations
            demonstratePortfolioOperations(portfolioService);

            // Demo market data
            demonstrateMarketData(portfolioService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void demonstrateUserAuth(UserAuth userAuth, UserSession session) {
        try {
            System.out.println("=== User Authentication ===");

            // For demo purposes - you would get these from user input
            String email = "testuser@example.com";
            String password = "testpassword123";

            // Try to login (this will fail if user doesn't exist)
            try {
                User user = userAuth.loginUser(email, password);
                String uid = userAuth.getCurrentUserUid();

                // FIXED: Use the correct method signature from your UserSession (3 parameters)
                session.setCurrentUser(user, uid, userAuth);
                System.out.println("✓ Logged in successfully: " + session.getUserFullName());
            } catch (Exception e) {
                System.out.println("Login failed, creating new user...");

                // Register new user
                String uid = userAuth.registerUser(email, password, "Demo", "User", LocalDate.now());
                System.out.println("✓ Registered new user with UID: " + uid);

                // Now login
                User user = userAuth.loginUser(email, password);
                String currentUid = userAuth.getCurrentUserUid();

                // FIXED: Use the correct method signature (3 parameters)
                session.setCurrentUser(user, currentUid, userAuth);
                System.out.println("✓ Logged in: " + session.getUserFullName());
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstratePortfolioOperations(PortfolioIntegration portfolioService) {
        try {
            System.out.println("=== Portfolio Operations ===");

            // Check current portfolio
            Portfolio portfolio = portfolioService.getUserPortfolio();
            System.out.println("Current portfolio has " + portfolio.getHoldings().size() + " holdings");

            // Buy some stocks
            System.out.println("\nBuying stocks...");
            portfolioService.buyStock("AAPL", 10, 150.00);
            portfolioService.buyStock("GOOGL", 5, 2500.00);
            portfolioService.buyStock("MSFT", 15, 300.00);

            // Buy more of the same stock (should update average price)
            portfolioService.buyStock("AAPL", 5, 155.00);

            // Display current holdings
            portfolio = portfolioService.getUserPortfolio();
            System.out.println("\n--- Current Holdings ---");
            for (PortfolioEntry entry : portfolio.getHoldings()) {
                System.out.printf("%-6s | %3d shares | Avg: $%8.2f | Current: $%8.2f | P&L: $%8.2f%n",
                        entry.getTickerSymbol(),
                        entry.getTotalShares(),
                        entry.getAverageBuyPrice(),
                        entry.getCurrentMarketPrice(),
                        entry.getUnrealizedGainLoss()
                );
            }

            // Refresh prices with live data
            System.out.println("\nRefreshing with live prices...");
            portfolioService.refreshPortfolioPrices();

            // Show updated portfolio
            portfolio = portfolioService.getUserPortfolio();
            System.out.println("\n--- Updated Holdings ---");
            for (PortfolioEntry entry : portfolio.getHoldings()) {
                System.out.printf("%-6s | %3d shares | Avg: $%8.2f | Current: $%8.2f | P&L: $%8.2f%n",
                        entry.getTickerSymbol(),
                        entry.getTotalShares(),
                        entry.getAverageBuyPrice(),
                        entry.getCurrentMarketPrice(),
                        entry.getUnrealizedGainLoss()
                );
            }

            // Portfolio summary
            Map<String, Object> summary = portfolioService.getPortfolioSummary();
            System.out.println("\n--- Portfolio Summary ---");
            System.out.printf("User: %s%n", summary.get("userName"));
            System.out.printf("Total Value: $%.2f%n", summary.get("totalValue"));
            System.out.printf("Total Invested: $%.2f%n", summary.get("totalInvested"));
            System.out.printf("Unrealized P&L: $%.2f (%.2f%%)%n",
                    summary.get("totalUnrealizedGainLoss"), summary.get("percentageGainLoss"));

            // Sell some shares
            System.out.println("\nSelling 5 shares of AAPL...");
            portfolioService.sellStock("AAPL", 5, 152.00);

            // Show transaction history
            List<Transaction> transactions = portfolioService.getTransactionHistory();
            System.out.println("\n--- Recent Transactions ---");
            for (int i = 0; i < Math.min(5, transactions.size()); i++) {
                Transaction t = transactions.get(i);
                System.out.printf("%s %s %d shares of %s at $%.2f on %s%n",
                        t.getIsBuy() ? "BUY " : "SELL",
                        t.getIsBuy() ? "+" : "-",
                        t.getQuantity(),
                        t.getTickerSymbol(),
                        t.getPricePerShare(),
                        t.getTimestamp()
                );
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("Portfolio operation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstrateMarketData(PortfolioIntegration portfolioService) {
        try {
            System.out.println("=== Market Data ===");

            // Get real-time stock quote
            String ticker = "AAPL";
            Stock stock = portfolioService.getStockQuote(ticker);
            System.out.printf("%s (%s): $%.2f%n",
                    stock.getCompanyName(), stock.getTickerSymbol(), stock.getCurrentPrice());
            System.out.printf("Day Range: $%.2f - $%.2f%n", stock.getLow(), stock.getHigh());
            System.out.printf("Previous Close: $%.2f%n", stock.getPreviousClose());

            // Market status
            boolean isOpen = portfolioService.isMarketOpen();
            System.out.println("Market Status: " + (isOpen ? "OPEN" : "CLOSED"));

            // Market news
            System.out.println("\n--- Market News ---");
            List<String> marketNews = portfolioService.getMarketNews();
            for (int i = 0; i < Math.min(3, marketNews.size()); i++) {
                System.out.println("• " + marketNews.get(i));
            }

            // Company-specific news
            System.out.println("\n--- " + ticker + " News ---");
            List<String> companyNews = portfolioService.getCompanyNews(ticker);
            for (int i = 0; i < Math.min(2, companyNews.size()); i++) {
                System.out.println("• " + companyNews.get(i));
            }

        } catch (Exception e) {
            System.err.println("Market data error: " + e.getMessage());
        }
    }
}