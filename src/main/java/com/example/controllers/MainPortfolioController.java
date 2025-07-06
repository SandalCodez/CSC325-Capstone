package com.example.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import com.example.services.ChatGPTClient;

import java.io.IOException;

public class MainPortfolioController {

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
}