package com.example.controllers;

import com.example.models.User;
import com.example.services.*;
import com.example.models.PortfolioEntry;
import com.google.cloud.firestore.Firestore;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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

    @FXML
    private Label balanceLabel;

    @FXML
    private Button addFundsButton;



    private final FinnhubService finnhubService = new FinnhubService();
    private final ObservableList<PortfolioEntry> portfolioData = FXCollections.observableArrayList();
    private User loggedInUser;
    private UserAuth userAuth;


    public void initialize() {

        userAuth = UserSession.getInstance().getUserAuth();
        loggedInUser = UserSession.getInstance().getCurrentUser();

        if (loggedInUser != null) {
            balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
        } else {
            balanceLabel.setText("Not Available");
        }

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

    @FXML
    private TextField userInput;
    @FXML
    private TextArea chatArea;
    @FXML
    private Button sendBtn;

    @FXML
    protected void onSend() {
        String userMsg = userInput.getText();
        if (!userMsg.isBlank()) {
            chatArea.appendText("You: " + userMsg + "\n");
            userInput.clear();

            //Loading message
            chatArea.appendText("AI: ...thinking...\n");

            new Thread(() -> {
                try {
                    String aiReply = ChatGPTClient.ask(userMsg);
                    javafx.application.Platform.runLater(() -> {
                        chatArea.appendText("AI: " + aiReply.trim() + "\n");
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() ->
                            chatArea.appendText("AI: (Error: " + ex.getMessage() + ")\n")
                    );
                }
            }).start();


        }

    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
    }

    @FXML
    private void handleAddFunds(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("0.0");
        dialog.setTitle("Add Funds");
        dialog.setHeaderText("Add funds to balance");
        dialog.setContentText("Please enter amount");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                double amountToAdd = Double.parseDouble(input.trim());

                if (amountToAdd < 0) {
                    showAlert("Invalid input", "Must be positive amount");
                    return;
                }

                double newBalance = loggedInUser.getAccountBalance() + amountToAdd;
                loggedInUser.setAccountBalance(newBalance);
                balanceLabel.setText(String.format("$%.2f", newBalance));

                String uid = UserSession.getInstance().getUserUid();
                userAuth.updateUserBalance(uid, newBalance);
            } catch (NumberFormatException e) {
                showAlert("Invalid input", "Please enter a valid number");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}