package com.example.services;

import com.example.models.*;
import com.example.services.*;
import com.google.cloud.firestore.Firestore;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

            // Demo user login
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

            String email = "testuser@example.com";
            String password = "testpassword123";

            try {
                User user = userAuth.loginUser(email, password);
                session.setCurrentUser(user, userAuth.getCurrentUserUid());
                System.out.println("✓ Logged in successfully: " + session.getUserFullName());
            } catch (Exception e) {
                System.out.println("Login failed, creating new user...");

                String uid = userAuth.registerUser(email, password, "Demo", "User", LocalDate.now());
                System.out.println("✓ Registered new user with UID: " + uid);

                User user = userAuth.loginUser(email, password);
                session.setCurrentUser(user, userAuth.getCurrentUserUid());
                System.out.println("✓ Logged in: " + session.getUserFullName());
            }

            System.out.println();

        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
    }

    private static void demonstratePortfolioOperations(PortfolioIntegration portfolioService) {
        try {
            System.out.println("=== Portfolio Operations ===");

            Portfolio portfolio = portfolioService.getUserPortfolio();
            System.out.println("Current portfolio has " + portfolio.getHoldings().size() + " holdings");

            System.out.println("\nBuying stocks...");
            portfolioService.buyStock("AAPL", 10, 150.00);
            portfolioService.buyStock("GOOGL", 5, 2500.00);
            portfolioService.buyStock("MSFT", 15, 300.00);
            portfolioService.buyStock("AAPL", 5, 155.00); // Buy more AAPL

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

            System.out.println("\nRefreshing with live prices...");
            portfolioService.refreshPortfolioPrices();

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

            Map<String, Object> summary = portfolioService.getPortfolioSummary();
            System.out.println("\n--- Portfolio Summary ---");
            System.out.printf("User: %s%n", summary.get("userName"));
            System.out.printf("Total Value: $%.2f%n", summary.get("totalValue"));
            System.out.printf("Total Invested: $%.2f%n", summary.get("totalInvested"));
            System.out.printf("Unrealized P&L: $%.2f (%.2f%%)%n",
                    summary.get("totalUnrealizedGainLoss"), summary.get("percentageGainLoss"));

            System.out.println("\nSelling 5 shares of AAPL...");
            portfolioService.sellStock("AAPL", 5, 152.00);

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

            String ticker = "AAPL";
            Stock stock = portfolioService.getStockQuote(ticker);
            System.out.printf("%s (%s): $%.2f%n",
                    stock.getCompanyName(), stock.getTickerSymbol(), stock.getCurrentPrice());
            System.out.printf("Day Range: $%.2f - $%.2f%n", stock.getLow(), stock.getHigh());
            System.out.printf("Previous Close: $%.2f%n", stock.getPreviousClose());

            boolean isOpen = portfolioService.isMarketOpen();
            System.out.println("Market Status: " + (isOpen ? "OPEN" : "CLOSED"));

            System.out.println("\n--- Market News ---");
            List<String> marketNews = portfolioService.getMarketNews();
            for (int i = 0; i < Math.min(3, marketNews.size()); i++) {
                System.out.println("• " + marketNews.get(i));
            }

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