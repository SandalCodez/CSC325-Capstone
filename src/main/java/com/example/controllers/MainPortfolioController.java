package com.example.controllers;

import com.example.models.Stock;
import com.example.models.User;
import com.example.services.*;
import com.example.models.PortfolioEntry;
import com.example.models.Portfolio;
import com.example.services.FinnhubService;
import com.example.services.PortfolioIntegration;
import com.example.services.FirestoreDB;
import com.example.services.UserSession;
import com.google.cloud.firestore.Firestore;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.Port;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
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
    private TextField userInput;

    @FXML
    private VBox chatArea;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label totalGainLossLabel;

    @FXML
    private Label percentageGainLossLabel;
    @FXML
    private TextField StockSearchField;

    @FXML
    private Label balanceLabel;
    @FXML
    private Button searchBtn;

    @FXML
    private Button addFundsButton, sendBtn;

    private FinnhubService finnhubService = new FinnhubService();
    private final ObservableList<PortfolioEntry> portfolioData = FXCollections.observableArrayList();
    private UserSession userSession;
    private User loggedInUser;
    private UserAuth userAuth;
    private FirestoreDB db;
    private PortfolioIntegration portfolioIntegration;
    private Portfolio portfolio;
    private String uid;

    public void setFirestoreDB(FirestoreDB db) {
        this.db = db;
        this.portfolioIntegration = db.getPortfolioIntegration();
    }


    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, FinnhubService finnhubService, PortfolioIntegration portfolioIntegration, User loggedInUser, String uid) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
        this.portfolioIntegration = portfolioIntegration;
        this.loggedInUser = loggedInUser;
        this.uid = uid;
    }

    @FXML private StackPane rootPane;
    @FXML private Group scalingPane;
    @FXML private ImageView bgImageView;
    double baseWidth = 1200.0;
    double baseHeight = 800.0;



    @FXML
    private void clearStockSearch(MouseEvent event) {
        StockSearchField.clear();
    }

    @FXML
    public void initialize() throws ParseException {
        try {

            setupTableColumns();
            portfolioTable.setItems(portfolioData);

            rootPane.widthProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
                    double scale = newVal.doubleValue() / baseWidth;
                    scalingPane.setScaleX(scale);
                    bgImageView.setFitWidth(newVal.doubleValue());
                }
            });

            rootPane.heightProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
                    double scale = newVal.doubleValue() / baseHeight;
                    scalingPane.setScaleY(scale);
                    bgImageView.setFitHeight(newVal.doubleValue());
                }
            });


        } catch (Exception e) {
            System.err.println("Error initializing portfolio: " + e.getMessage());
            e.printStackTrace();
            // Fallback to test data
            setupTableColumns();
            loadTestData();
            portfolioTable.setItems(portfolioData);
            loadMarketNews();

            rootPane.widthProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
                    double scale = newVal.doubleValue() / baseWidth;
                    scalingPane.setScaleX(scale);
                    bgImageView.setFitWidth(newVal.doubleValue());
                }
            });

            rootPane.heightProperty().addListener(new ChangeListener<Number>() {
                public void changed(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
                    double scale = newVal.doubleValue() / baseHeight;
                    scalingPane.setScaleY(scale);
                    bgImageView.setFitHeight(newVal.doubleValue());
                }
            });


        }
    }

    public void initializeData() {
        try {
            setupTableColumns();

            if (loggedInUser != null) {
                refreshPortfolioScreen();
            } else {
                loadTestData();
            }
            portfolioTable.setItems(portfolioData);
            loadMarketNews();

            portfolioTable.setRowFactory(tv -> {
                TableRow<PortfolioEntry> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    System.out.println("Mouse clikeckd on Table");
                    System.out.println("click count:" + event.getClickCount());
                    System.out.println("Row is empty");

                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        PortfolioEntry selectedEntry = row.getItem();
                        openStockDetails(selectedEntry);
                    }
                });
                return row;
            });
        } catch (Exception e) {
            System.err.println("Error initializing portfolio: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        tickerColumn.setCellValueFactory(new PropertyValueFactory<>("tickerSymbol"));
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        sharesColumn.setCellValueFactory(new PropertyValueFactory<>("totalShares"));
        avgBuyColumn.setCellValueFactory(new PropertyValueFactory<>("buyPrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
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

    public void loadRealPortfolioData() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    PortfolioIntegration integration = db.getPortfolioIntegration();
                    if (integration == null) {
                        throw new IllegalStateException("PortfolioIntegration is not set in FirestoreDB!");
                    }

                    // Get updated portfolio
                    portfolio = integration.getUserPortfolio();

                    // Refresh prices
                    integration.refreshPortfolioPrices();

                    Platform.runLater(() -> {
                        portfolioData.clear();
                        portfolioData.addAll(portfolio.getHoldings());
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error loading portfolio: " + e.getMessage());
                        e.printStackTrace();
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
                    Map<String, Object> summary = db.getPortfolioIntegration().getPortfolioSummary();

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
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(summaryTask).start();
    }
    @FXML
    private void handleEnter(ActionEvent event) throws IOException {
        String input = StockSearchField.getText().trim().toUpperCase();
        if (!input.isEmpty()) {
            navigateToStockScreen(input);
        }
    }

    @FXML
    private void handleRefreshPortfolio() {
        if (loggedInUser != null) {
            loadRealPortfolioData();
            loadPortfolioSummary();
        }
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


    public void refreshPortfolioScreen() {
        if(loggedInUser != null) {
            System.out.println("Debug portfolio balance - refresh portfolio screen " + loggedInUser.getAccountBalance());
            loadRealPortfolioData();
            loadPortfolioSummary();
            loadBalanceLabel();
        }
    }

    private void openStockDetails(PortfolioEntry entry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/StockScreen.fxml"));
            Parent root = loader.load();

            StockScreenController controller = loader.getController();
            System.out.println("DEBUG USERAUTH2 = " + userAuth);
            controller.setContext(
                    entry,
                    db,
                    userAuth,
                    portfolio,
                    finnhubService,
                    portfolioIntegration,
                    loggedInUser,
                    uid
            );
            System.out.println("Debug userAuth = " + (userAuth==null));
            controller.updateBalanceDisplay();

            Stage stage = new Stage();
            stage.setTitle("Stock Details - " + entry.getTickerSymbol());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionally show error popup
        }
    }

    @FXML
    private void handleBackToLogIn(ActionEvent event) throws IOException {
        loggedInUser.logout();
        // Clear the session
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));

        Parent SignInRoot = fxmlLoader.load();

        SignInController controller = fxmlLoader.getController();
        controller.setSplashDependencies(db, userAuth, portfolio, finnhubService);
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
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(actionEvent -> {stage.close();});
        delay.play();

    }

    @FXML
    private void handleToStockScreen(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/StockScreen.fxml"));
        Parent stockScreenRoot = fxmlLoader.load();
        StockScreenController controller = fxmlLoader.getController();
        controller.setDependencies(this.db, this.userAuth, this.portfolio, this.finnhubService, this.portfolioIntegration, this.loggedInUser, this.uid);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(stockScreenRoot));
        stage.setTitle("Stock Screen");
        stage.show();
    }




    public void loadMarketNews() {
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

    private void loadTestData() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date aaplDate = sdf.parse("07/12/2025");
        Date tslaDate = sdf.parse("07/12/2025");

        portfolioData.add(new PortfolioEntry(
                "AAPL", "Apple Inc", 10, 150.0, aaplDate));

        portfolioData.add(new PortfolioEntry(
                "TSLA", "Tesla Inc", 8, 700.0, tslaDate));
    }

    @FXML private VBox chatHistoryBox;

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
                    // Enhanced ChatGPT integration with portfolio context
                    String portfolioContext = getPortfolioContextForAI();
                    String enhancedPrompt = portfolioContext + "\n\nUser question: " + userMsg;

                    String aiReply = ChatGPTClient.ask(enhancedPrompt);
                    Platform.runLater(() -> {
                        // Remove the "thinking" message and add the real response
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

    private String getPortfolioContextForAI() {
        try {
            if (loggedInUser == null) {
                return "User is not logged in. No portfolio data available.";
            }

            Map<String, Object> summary = portfolioIntegration.getPortfolioSummary();
            Portfolio portfolio = portfolioIntegration.getUserPortfolio();

            StringBuilder context = new StringBuilder();
            context.append("Portfolio Context for ").append(loggedInUser.getfName() + " " + loggedInUser.getlName()).append(":\n");
            context.append("Total Value: $").append(String.format("%.2f", summary.get("totalValue"))).append("\n");
            context.append("Total Unrealized P&L: $").append(String.format("%.2f", summary.get("totalUnrealizedGainLoss"))).append("\n");
            context.append("Percentage P&L: ").append(String.format("%.2f%%", summary.get("percentageGainLoss"))).append("\n");
            context.append("Number of Holdings: ").append(summary.get("numberOfHoldings")).append("\n\n");

            context.append("Current Holdings:\n");
            for (PortfolioEntry entry : portfolio.getHoldings()) {
                Stock stock = finnhubService.getQuoteForTicker(entry.getTickerSymbol());
                double pgl = (stock.getCurrentPrice() - entry.getBuyPrice()) * entry.getTotalShares();

                context.append("- ").append(entry.getTickerSymbol())
                        .append(": ")
                        .append(entry.getTotalShares()).append(" shares, ")
                        .append("P&L: $").append(String.format("%.2f", pgl))
                        .append("\n");
            }

            return context.toString();

        } catch (Exception e) {
            return "Error retrieving portfolio context: " + e.getMessage();
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
                String uid = this.uid;
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

    public void setPortfolioIntegration(PortfolioIntegration portfolioIntegration) {
        this.portfolioIntegration = portfolioIntegration;
    }

    public void loadBalanceLabel(){
        System.out.println("Debug portfolio balane - load balance label " + portfolio.getBalance());
        Platform.runLater(() -> {
            if (balanceLabel != null) {
                balanceLabel.setText(String.format("$%.2f", loggedInUser.getAccountBalance()));
            }
        });
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public User getLoggedInUser(){
        return loggedInUser;
    }

    public void setUserUid(String uid) {
        this.uid = uid;
    }
}