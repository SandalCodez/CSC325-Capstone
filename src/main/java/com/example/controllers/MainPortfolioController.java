package com.example.controllers;

import com.example.models.User;
import com.example.services.*;
import com.example.models.PortfolioEntry;
import com.example.models.Portfolio;
import com.example.models.Transaction;
import com.example.services.FinnhubService;
import com.example.services.PortfolioIntegration;
import com.example.services.FirestoreDB;
import com.example.services.UserSession;
import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.Map;
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
    private TextField StockSearchField;

    @FXML
    private TextField userInput;

    @FXML
    private TextArea chatArea;

    @FXML
    private Button sendBtn;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label totalGainLossLabel;

    @FXML
    private Label percentageGainLossLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Button addFundsButton;

    private final FinnhubService finnhubService = new FinnhubService();
    private final ObservableList<PortfolioEntry> portfolioData = FXCollections.observableArrayList();
    private PortfolioIntegration portfolioService;
    private UserSession userSession;
    private User loggedInUser;
    private UserAuth userAuth;

    public void initialize() {
        try {
            // Initialize Firebase and portfolio service
            FirestoreDB firestoreDB = new FirestoreDB();
            Firestore db = firestoreDB.connect();
            portfolioService = new PortfolioIntegration(db);
            userSession = UserSession.getInstance();
            userAuth = UserSession.getInstance().getUserAuth();
            loggedInUser = UserSession.getInstance().getCurrentUser();

            // Set up table columns
            setupTableColumns();

            // Update balance label
            if (loggedInUser != null) {
                balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
            } else {
                balanceLabel.setText("Not Available");
            }

            // Load real portfolio data if user is logged in
            if (userSession.isLoggedIn()) {
                loadRealPortfolioData();
                loadPortfolioSummary();
            } else {
                // Fallback to test data if no user is logged in
                loadTestData();
            }

            portfolioTable.setItems(portfolioData);
            loadMarketNews();

        } catch (Exception e) {
            System.err.println("Error initializing portfolio: " + e.getMessage());
            e.printStackTrace();
            // Fallback to test data
            setupTableColumns();
            loadTestData();
            portfolioTable.setItems(portfolioData);
            loadMarketNews();
        }
    }

    private void setupTableColumns() {
        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("tickerSymbol"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        sharesColumn.setCellValueFactory(new PropertyValueFactory<>("totalShares"));
        avgBuyColumn.setCellValueFactory(new PropertyValueFactory<>("averageBuyPrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentMarketPrice"));
        unrealizedGainColumn.setCellValueFactory(new PropertyValueFactory<>("unrealizedGainLoss"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

        // Format currency columns
        avgBuyColumn.setCellFactory(column -> new TableCell<PortfolioEntry, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        currentPriceColumn.setCellFactory(column -> new TableCell<PortfolioEntry, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        unrealizedGainColumn.setCellFactory(column -> new TableCell<PortfolioEntry, Double>() {
            @Override
            protected void updateItem(Double gain, boolean empty) {
                super.updateItem(gain, empty);
                if (empty || gain == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", gain));
                    // Color code gains/losses
                    if (gain > 0) {
                        setStyle("-fx-text-fill: green;");
                    } else if (gain < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        totalValueColumn.setCellFactory(column -> new TableCell<PortfolioEntry, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", value));
                }
            }
        });
    }

    private void loadRealPortfolioData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Refresh prices first
                    portfolioService.refreshPortfolioPrices();

                    // Get updated portfolio
                    Portfolio portfolio = portfolioService.getUserPortfolio();

                    Platform.runLater(() -> {
                        portfolioData.clear();
                        portfolioData.addAll(portfolio.getHoldings());
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error loading portfolio: " + e.getMessage());
                        // Show error in UI if needed
                        showAlert("Error", "Failed to load portfolio: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        new Thread(loadTask).start();
    }

    private void loadPortfolioSummary() {
        Task<Void> summaryTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Map<String, Object> summary = portfolioService.getPortfolioSummary();

                    Platform.runLater(() -> {
                        // Update summary labels if they exist in your FXML
                        if (totalValueLabel != null) {
                            totalValueLabel.setText(String.format("$%.2f", summary.get("totalValue")));
                        }
                        if (totalGainLossLabel != null) {
                            Double gainLoss = (Double) summary.get("totalUnrealizedGainLoss");
                            totalGainLossLabel.setText(String.format("$%.2f", gainLoss));
                            totalGainLossLabel.setStyle(gainLoss >= 0 ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                        }
                        if (percentageGainLossLabel != null) {
                            percentageGainLossLabel.setText(String.format("%.2f%%", summary.get("percentageGainLoss")));
                        }
                    });

                } catch (Exception e) {
                    System.err.println("Error loading portfolio summary: " + e.getMessage());
                }
                return null;
            }
        };

        new Thread(summaryTask).start();
    }

    @FXML
    private void handleRefreshPortfolio() {
        if (userSession.isLoggedIn()) {
            loadRealPortfolioData();
            loadPortfolioSummary();
        }
    }

    // Handle double-click on portfolio entry to view stock details
    @FXML
    private void handlePortfolioRowClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            PortfolioEntry selectedEntry = portfolioTable.getSelectionModel().getSelectedItem();
            if (selectedEntry != null) {
                try {
                    navigateToStockScreen(selectedEntry.getTickerSymbol());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void handleBackToLogIn(ActionEvent event) throws IOException {
        userSession.logout(); // Clear the session
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
        navigateToStockScreen(null);
    }

    private void navigateToStockScreen(String ticker) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/StockScreen.fxml"));
        Parent StockScreenRoot = fxmlLoader.load();

        // Pass ticker to StockScreenController if provided
        if (ticker != null) {
            StockScreenController controller = fxmlLoader.getController();
            controller.setInitialTicker(ticker);
        }

        Stage stage = (Stage) portfolioTable.getScene().getWindow();
        stage.setScene(new Scene(StockScreenRoot));
        stage.setTitle("StockScreen");
        stage.show();
    }

    @FXML
    private void clearStockSearch(MouseEvent event) {
        StockSearchField.clear();
    }

    @FXML
    private void handleEnter(ActionEvent event) throws IOException {
        String input = StockSearchField.getText().trim().toUpperCase();
        if (!input.isEmpty()) {
            navigateToStockScreen(input);
        }
    }

    private void loadMarketNews() {
        Task<Void> newsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    List<String> newsHeadlines = finnhubService.getMarketNews();
                    StringBuilder newsText = new StringBuilder();
                    for (String headline : newsHeadlines) {
                        newsText.append(headline).append("\n\n");
                    }

                    Platform.runLater(() -> {
                        marketNewsArea.setText(newsText.toString());
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        marketNewsArea.setText("Error loading market news: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        new Thread(newsTask).start();
    }

    private void loadTestData() {
        portfolioData.add(new PortfolioEntry(
                "AAPL", "Apple Inc", 10, 150.0, 180.0, 300.0, 1800.0));

        portfolioData.add(new PortfolioEntry(
                "TSLA", "Tesla Inc", 8, 700.0, 750.0, 400.0, 6000.0));
    }

    @FXML
    protected void onSend() {
        String userMsg = userInput.getText();
        if (!userMsg.isBlank()) {
            chatArea.appendText("You: " + userMsg + "\n");
            userInput.clear();

            // Loading message
            chatArea.appendText("AI: ...thinking...\n");

            new Thread(() -> {
                try {
                    // Enhanced ChatGPT integration with portfolio context
                    String portfolioContext = getPortfolioContextForAI();
                    String enhancedPrompt = portfolioContext + "\n\nUser question: " + userMsg;

                    String aiReply = ChatGPTClient.ask(enhancedPrompt);
                    Platform.runLater(() -> {
                        // Remove the "thinking" message and add the real response
                        String currentText = chatArea.getText();
                        String withoutThinking = currentText.replace("AI: ...thinking...\n", "");
                        chatArea.setText(withoutThinking);
                        chatArea.appendText("AI: " + aiReply.trim() + "\n\n");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        String currentText = chatArea.getText();
                        String withoutThinking = currentText.replace("AI: ...thinking...\n", "");
                        chatArea.setText(withoutThinking);
                        chatArea.appendText("AI: (Error: " + ex.getMessage() + ")\n\n");
                    });
                }
            }).start();
        }
    }

    private String getPortfolioContextForAI() {
        try {
            if (!userSession.isLoggedIn()) {
                return "User is not logged in. No portfolio data available.";
            }

            Map<String, Object> summary = portfolioService.getPortfolioSummary();
            Portfolio portfolio = portfolioService.getUserPortfolio();

            StringBuilder context = new StringBuilder();
            context.append("Portfolio Context for ").append(userSession.getUserFullName()).append(":\n");
            context.append("Total Value: $").append(String.format("%.2f", summary.get("totalValue"))).append("\n");
            context.append("Total Unrealized P&L: $").append(String.format("%.2f", summary.get("totalUnrealizedGainLoss"))).append("\n");
            context.append("Percentage P&L: ").append(String.format("%.2f%%", summary.get("percentageGainLoss"))).append("\n");
            context.append("Number of Holdings: ").append(summary.get("numberOfHoldings")).append("\n\n");

            context.append("Current Holdings:\n");
            for (PortfolioEntry entry : portfolio.getHoldings()) {
                context.append("- ").append(entry.getTickerSymbol())
                        .append(" (").append(entry.getCompanyName()).append("): ")
                        .append(entry.getTotalShares()).append(" shares, ")
                        .append("P&L: $").append(String.format("%.2f", entry.getUnrealizedGainLoss())).append("\n");
            }

            return context.toString();

        } catch (Exception e) {
            return "Error retrieving portfolio context: " + e.getMessage();
        }
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
        }
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