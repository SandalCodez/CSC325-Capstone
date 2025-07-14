package com.example.controllers;

import com.example.models.CompanyProfile;
import com.example.models.Portfolio;
import com.example.models.Stock;
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

    private final FinnhubService finnhubService = new FinnhubService();
    private String initialTicker = null;
    private PortfolioIntegration portfolioIntegration;
    private FirestoreDB db;
    private Portfolio portfolio;
    private UserAuth userAuth;

    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, PortfolioIntegration portfolioIntegration) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.portfolioIntegration = portfolioIntegration;
    }

    @FXML
    private TextField tickerInputField;

    @FXML
    private Label tickerLabel, companyNameLabel, industryLabel, currentPriceLabel, marketCapLabel,
            sharesOutstandingLabel, numberOfSharesLabel, averageBuyPriceLabel,
            totalValueLabel, gainLossLabel, profitLossLabel, percentOfPortfolioLabel;

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
        if (initialTicker != null) {
            loadStockData(initialTicker);
        }
    }

    @FXML
    private void handleSearch() {
        String symbol = tickerInputField.getText().trim().toUpperCase();
        if (!symbol.isEmpty()) {
            loadStockData(symbol);
        }
    }

    private void loadStockData(String symbol) {
        try {
            Stock stock = finnhubService.getQuoteForTicker(symbol);
            CompanyProfile profile = finnhubService.getCompanyProfile(symbol);
            List<String> news = finnhubService.getCompanyNews(symbol);

            tickerLabel.setText(symbol);
            companyNameLabel.setText(profile.getName());
            industryLabel.setText(profile.getIndustry());

            finnhubService.getCurrentPriceWithFallback(symbol, price -> {
                Platform.runLater(() -> currentPriceLabel.setText(String.format("%.2f", price)));
            });

            marketCapLabel.setText(String.format("%.2f", profile.getMarketCap()));
            sharesOutstandingLabel.setText(String.valueOf(profile.getSharesOutstanding()));

            numberOfSharesLabel.setText("—");
            averageBuyPriceLabel.setText("—");
            totalValueLabel.setText("—");
            gainLossLabel.setText("—");
            profitLossLabel.setText("—");
            percentOfPortfolioLabel.setText("—");

            StringBuilder newsText = new StringBuilder();
            for (String headline : news) {
                newsText.append("- ").append(headline).append("\n");
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
        controller.setDependencies(db, userAuth, portfolio, portfolioIntegration);
        controller.loadRealPortfolioData();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(MainPortfolioRoot));
        stage.setTitle("MainPortfolio");
        stage.show();
    }

    @FXML
    private void handleBuyButtonClick() {
        try {
            String ticker = tickerLabel.getText().trim().toUpperCase();

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

                    portfolioIntegration.buyStock(ticker, quantity, price, date);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Successfully bought " + quantity + " shares of " + ticker + "!");
                    alert.showAndWait();

                    refreshStockScreen(); // optionally refresh UI
                } catch (NumberFormatException e) {
                    showError("Invalid quantity. Please enter a positive integer.");
                }
            }

        } catch (Exception e) {
            Alert error = new Alert(Alert.AlertType.ERROR, "Failed to buy stock: " + e.getMessage());
            e.printStackTrace();
            error.showAndWait();
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


}
