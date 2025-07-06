package com.example.controllers;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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
}