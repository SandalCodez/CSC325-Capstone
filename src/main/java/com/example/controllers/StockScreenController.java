package com.example.controllers;

import com.example.models.CompanyProfile;
import com.example.models.Portfolio;
import com.example.models.PortfolioEntry;
import com.example.models.Stock;
import com.example.services.*;
import com.google.cloud.firestore.Firestore;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    @FXML private VBox chatHistoryBox;
    @FXML
    private TextField userInput;

    @FXML
    protected void onSend() {
        String userMsg = userInput.getText();
        if (!userMsg.isBlank()) {
            Label userLabel = new Label("You: " + userMsg);
            userLabel.setStyle("-fx-background-color: #393939; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
            userLabel.setWrapText(true);
            userLabel.setMaxWidth(200);
            chatHistoryBox.getChildren().add(userLabel);
            userInput.clear();

            // Loading message
            Label thinkingLabel = new Label("AI: ...thinking...");
            thinkingLabel.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
            thinkingLabel.setWrapText(true);
            thinkingLabel.setMaxWidth(200);
            chatHistoryBox.getChildren().add(thinkingLabel);

            new Thread(() -> {
                try {
                    // Just use the user's message—no portfolio context!
                    String aiReply = ChatGPTClient.ask(userMsg);
                    Platform.runLater(() -> {
                        chatHistoryBox.getChildren().remove(thinkingLabel);
                        Label aiLabel = new Label("AI: " + aiReply.trim());
                        aiLabel.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
                        aiLabel.setWrapText(true);
                        aiLabel.setMaxWidth(200);
                        chatHistoryBox.getChildren().add(aiLabel);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        chatHistoryBox.getChildren().remove(thinkingLabel);
                        Label errorLabel = new Label("AI: (Error: " + ex.getMessage() + ")");
                        errorLabel.setStyle("-fx-background-color: #ff4f4f; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
                        errorLabel.setWrapText(true);
                        errorLabel.setMaxWidth(300);
                        chatHistoryBox.getChildren().add(errorLabel);
                    });
                }
            }).start();
        }
    }
}
