package com.example.controllers;

import com.example.services.ChatGPTClient;
import com.example.models.PortfolioEntry;
import com.example.services.FinnhubService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainPortfolioController {

    @FXML
    private TableView<PortfolioEntry> portfolioTable;

    @FXML
    private TableColumn<PortfolioEntry, String> tickerColumn, companyColumn;

    @FXML
    private TableColumn<PortfolioEntry, Integer> sharesColumn;

    @FXML
    private TableColumn<PortfolioEntry, Double> avgBuyColumn, currentPriceColumn, unrealizedGainColumn, totalValueColumn;

    @FXML
    private TextArea marketNewsArea;

    private final FinnhubService finnhubService = new FinnhubService();
    private final ObservableList<PortfolioEntry> portfolioData = FXCollections.observableArrayList();


    public void initialize() {

        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("tickerSymbol"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        sharesColumn.setCellValueFactory(new PropertyValueFactory<>("totalShares"));
        avgBuyColumn.setCellValueFactory(new PropertyValueFactory<>("averageBuyPrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentMarketPrice"));
        unrealizedGainColumn.setCellValueFactory(new PropertyValueFactory<>("unrealizedGainLoss"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        loadTestData();
        portfolioTable.setItems(portfolioData);
        loadMarketNews();
    }


    @FXML
    private void handleBackToLogIn(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));
        Parent SignInRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(SignInRoot));
        stage.setTitle("SignIn");
        stage.show();
    }

    @FXML
    private void handleToSignOut(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/SignOut.fxml"));
        Parent SignOutRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(SignOutRoot));
        stage.setTitle("SignOut");
        stage.show();
    }


    @FXML
    private void handleToStockScreen(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/StockScreen.fxml"));
        Parent StockScreenRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(StockScreenRoot));
        stage.setTitle("StockScreen");
        stage.show();
    }

    @FXML
    private TextField StockSearchField;

    @FXML
    private void clearStockSearch(MouseEvent event) {
        StockSearchField.clear();

    }

    @FXML
    private void handleEnter(ActionEvent event) throws IOException {
        String input = StockSearchField.getText();
        if (!input.isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/StockScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("StockScreen");
            stage.show();
        }
    }

    private void loadMarketNews() {
        List<String> newsHeadlines = finnhubService.getMarketNews();
        StringBuilder newsText = new StringBuilder();
        for (String headline : newsHeadlines) {
            newsText.append(headline).append("\n\n");
        }
        marketNewsArea.setText(newsText.toString());
    }

    private void loadTestData() {

        portfolioData.add(new PortfolioEntry(
                "AAPL", "Apple Inc", 10, 150.0, 180.0, 300.0, 1800.0));

        portfolioData.add(new PortfolioEntry(
                "TSLA", "Tesla Inc", 8, 700.0, 750.0, 400.0, 6000.0));
    }

    @FXML private VBox chatHistoryBox;
    @FXML private TextField userInput;
    @FXML private Button sendBtn;


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

            Label thinkingLabel = new Label("AI: ...thinking...");
            thinkingLabel.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
            thinkingLabel.setWrapText(true);
            thinkingLabel.setMaxWidth(200);
            chatHistoryBox.getChildren().add(thinkingLabel);

            new Thread(() -> {
                try {
                    String aiReply = ChatGPTClient.ask(userMsg);
                    javafx.application.Platform.runLater(() -> {
                        chatHistoryBox.getChildren().remove(thinkingLabel);
                        Label botLabel = new Label("AI: " + aiReply.trim());
                        botLabel.setStyle("-fx-background-color: #4f8cff; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
                        botLabel.setWrapText(true);
                        botLabel.setMaxWidth(200);
                        chatHistoryBox.getChildren().add(botLabel);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        chatHistoryBox.getChildren().remove(thinkingLabel);
                        Label errorLabel = new Label("AI: (Error: " + ex.getMessage() + ")");
                        errorLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 10;");
                        errorLabel.setWrapText(true);
                        errorLabel.setMaxWidth(200);
                        chatHistoryBox.getChildren().add(errorLabel);
                    });
                }
            }).start();
        }
    }



}