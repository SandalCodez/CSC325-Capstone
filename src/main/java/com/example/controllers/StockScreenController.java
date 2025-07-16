package com.example.controllers;

import com.example.models.*;
import com.example.services.FinnhubService;
import com.example.services.FirestoreDB;
import com.example.services.PortfolioIntegration;
import com.example.services.UserAuth;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class StockScreenController {

    private FinnhubService finnhubService = new FinnhubService();
    private String initialTicker = null;
    private PortfolioIntegration portfolioIntegration;
    private FirestoreDB db;
    private Portfolio portfolio;
    private UserAuth userAuth;
    private PortfolioEntry selectedEntry = null;
    private User loggedInUser;
    private String uid;


    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, FinnhubService finnhubService, PortfolioIntegration portfolioIntegration, User loggedInUser, String uid) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
        this.portfolioIntegration = portfolioIntegration;
        this.loggedInUser = loggedInUser;
        this.uid = uid;
        updateBalanceDisplay();
    }

    @FXML
    private TextField tickerInputField;

    @FXML
    private Label tickerLabel, companyNameLabel, industryLabel, currentPriceLabel, marketCapLabel,
            sharesOutstandingLabel, numberOfSharesLabel, averageBuyPriceLabel,
            totalValueLabel, gainLossLabel, profitLossLabel, percentOfPortfolioLabel, balanceLabel;

    @FXML
    private TextArea companyNewsTextArea;

    public void setInitialTicker(String ticker) {
        this.initialTicker = ticker;
        if (ticker != null && !ticker.isEmpty()) {
            tickerInputField.setText(ticker);
            loadStockData(ticker);
        }
    }

    @FXML
    private void initialize() {
    }


    @FXML
    private void handleSearch() {
        String symbol = tickerInputField.getText().trim().toUpperCase();
        if (!symbol.isEmpty()) {
            loadStockData(symbol);
        }
    }

    private void loadStockData(String symbol) {
        // Reset holdings area first
        populateHoldingsFields(null);

        numberOfSharesLabel.setText("--");
        averageBuyPriceLabel.setText("--");
        totalValueLabel.setText("--");
        gainLossLabel.setText("--");
        profitLossLabel.setText("--");
        percentOfPortfolioLabel.setText("--");

        try {
            Stock stock = finnhubService.getQuoteForTicker(symbol);
            CompanyProfile profile = finnhubService.getCompanyProfile(symbol);
            List<String> news = finnhubService.getCompanyNews(symbol);

            tickerLabel.setText(symbol);
            companyNameLabel.setText(profile.getName());
            industryLabel.setText(profile.getIndustry());

            marketCapLabel.setText(String.format("%.2f", profile.getMarketCap()));
            sharesOutstandingLabel.setText(String.valueOf(profile.getSharesOutstanding()));

            finnhubService.getCurrentPriceWithFallback(symbol, price -> {
                currentPriceLabel.setText(String.format("%.2f", price));

                PortfolioEntry entry = portfolio.getEntryBySymbol(symbol);
                if (entry != null) {
                    entry.setCurrentPrice(price);
                    Platform.runLater(() -> populateHoldingsFields(entry));
                }
            });

            StringBuilder newsText = new StringBuilder();
            for (String headline : news) {
                newsText.append("• ").append(headline).append("\n");
            }
            companyNewsTextArea.setText(newsText.toString());

        } catch (Exception e) {
            tickerLabel.setText("Error retrieving stock data.");
            companyNewsTextArea.setText("Check the ticker symbol and try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToMainPortfolio(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/MainPortfolio.fxml"));
        Parent MainPortfolioRoot = fxmlLoader.load();

        MainPortfolioController controller = fxmlLoader.getController();
        System.out.println("Logged in user: " + this.loggedInUser);
        controller.setLoggedInUser(this.loggedInUser);
        controller.setDependencies(db, userAuth, portfolio, finnhubService, portfolioIntegration, loggedInUser, uid);
        controller.loadRealPortfolioData();
        controller.loadBalanceLabel();
        controller.loadMarketNews();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(MainPortfolioRoot));
        stage.setTitle("MainPortfolio");
        stage.show();
    }

    @FXML
    private void handleBuyButtonClick() {
        try {
            System.out.println("DEBUG handle buystock balance: " + loggedInUser.getAccountBalance());
            String ticker = tickerLabel.getText().trim().toUpperCase();
            String companyName = companyNameLabel.getText().trim();

            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Buy Shares");
            dialog.setHeaderText("Enter number of shares to buy:");
            dialog.setContentText("Quantity:");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                try {
                    int quantity = Integer.parseInt(result.get());
                    if (quantity <= 0) throw new NumberFormatException();

                    double price = Double.parseDouble(currentPriceLabel.getText()); // or live API
                    Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

                    portfolioIntegration.buyStock(ticker, companyName, quantity, price, date);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Successfully bought " + quantity + " shares of " + ticker + "!");
                    alert.showAndWait();

                    refreshStockScreen(); // optionally refresh U
                } catch (NumberFormatException e) {
                    showError("Invalid quantity. Please enter a positive integer.");
                }
            }

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to buy stock: " + e.getMessage());
            error.showAndWait();
        }
    }

    @FXML
    private void handleSellButtonClick(ActionEvent event) {
        String ticker = tickerLabel.getText();
        PortfolioEntry entry = portfolio.getEntryBySymbol(ticker);

        if (entry == null) {
            showAlert("You don't own any shares of this stock.");
            return;
        }

        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Sell Shares");
        inputDialog.setHeaderText("Enter number of shares to sell:");
        inputDialog.setContentText("Shares:");

        Optional<String> result = inputDialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        int quantityToSell;
        try {
            quantityToSell = Integer.parseInt(result.get());
        } catch (NumberFormatException e) {
            showAlert("Invalid number entered.");
            return;
        }

        if (quantityToSell <= 0 || quantityToSell > entry.getTotalShares()) {
            showAlert("Enter a quantity between 1 and " + entry.getTotalShares());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Sell");
        confirm.setHeaderText("Are you sure you want to sell " + quantityToSell + " share(s) of " + ticker + "?");
        confirm.setContentText("This action will update your portfolio and balance.");

        Optional<ButtonType> confirmation = confirm.showAndWait();
        if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
            return;
        }

        double currentPrice;
        try {
            currentPrice = Double.parseDouble(currentPriceLabel.getText().replace("$", ""));
        } catch (Exception e) {
            showAlert("Current price is unavailable.");
            return;
        }
        Date sellDate = new Date();

        try{
            portfolioIntegration.sellStock(ticker, quantityToSell, currentPrice, sellDate);
            showAlert("Sell Successful");
        } catch (Exception e) {
            showAlert("Sell Failed: " + e.getMessage());
        }
    }


    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setContentText(message);
        error.showAndWait();
    }


    private void refreshStockScreen() {
        String ticker = tickerLabel.getText().trim().toUpperCase();
        loadStockData(ticker);
    }

    public void setContext(
            PortfolioEntry entry,
            FirestoreDB db,
            UserAuth userAuth,
            Portfolio portfolio,
            FinnhubService finnhubService,
            PortfolioIntegration portfolioIntegration,
            User loggedInUser,
            String uid
    ) {
        this.selectedEntry = entry;
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
        this.portfolioIntegration = portfolioIntegration;
        this.loggedInUser = loggedInUser;
        this.uid = uid;

        if (entry != null) {
            populateFromEntry(entry);
        }

        updateBalanceDisplay();
    }

    private void populateFromEntry(PortfolioEntry entry) {
        if (entry != null) {
            selectedEntry = entry;
            String ticker = entry.getTickerSymbol();
            tickerInputField.setText(ticker);
            loadStockData(ticker); // sets company labels
            populateHoldingsFields(entry); // sets shares/buy price/gain-loss
        }
    }

    private void populateHoldingsFields(PortfolioEntry entry) {
        if (entry == null) return;

        int shares = entry.getTotalShares();
        double buyPrice = entry.getBuyPrice();
        Double currentPrice = entry.getCurrentPrice();

        // Defensive: if missing price data, skip calculation
        if (currentPrice == null || buyPrice <= 0 || shares <= 0) {
            numberOfSharesLabel.setText("--");
            averageBuyPriceLabel.setText("--");
            totalValueLabel.setText("--");
            gainLossLabel.setText("--");
            profitLossLabel.setText("--");
            percentOfPortfolioLabel.setText("--");
            return;
        }

        // Core calculations
        double totalValue = currentPrice * shares;
        double costBasis = buyPrice * shares;
        double gainLoss = totalValue - costBasis;
        double profitLossPercent = (gainLoss / costBasis) * 100;

        // Portfolio weight (% of portfolio)
        double portfolioTotal = portfolio != null ? portfolio.getTotalValue() : 0.0;
        double percentOfPortfolio = portfolioTotal > 0 ? (totalValue / portfolioTotal) * 100 : 0.0;


        // Set labels
        numberOfSharesLabel.setText(String.valueOf(shares));
        averageBuyPriceLabel.setText(String.format("$%.2f", buyPrice));
        totalValueLabel.setText(String.format("$%.2f", totalValue));
        gainLossLabel.setText(String.format("$%.2f", gainLoss));
        profitLossLabel.setText(String.format("%.2f%%", profitLossPercent));
        percentOfPortfolioLabel.setText(String.format("%.2f%%", percentOfPortfolio));
    }

    public void updateBalanceDisplay(){
        if(loggedInUser != null && balanceLabel != null){
            balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sell Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
}
