package com.example.controllers;

import com.example.models.CompanyProfile;
import com.example.models.Stock;
import com.example.services.FinnhubService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class StockScreenController {

    @FXML
    private TextField tickerInputField;

    @FXML
    private Label tickerLabel, companyNameLabel, industryLabel, currentPriceLabel, marketCapLabel,
            sharesOutstandingLabel, numberOfSharesLabel, averageBuyPriceLabel,
            totalValueLabel, gainLossLabel, profitLossLabel, percentOfPortfolioLabel;

    @FXML
    private TextArea companyNewsTextArea;

    private final FinnhubService finnhubService = new FinnhubService();
    private String initialTicker = null;


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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(MainPortfolioRoot));
        stage.setTitle("MainPortfolio");
        stage.show();
    }
}
