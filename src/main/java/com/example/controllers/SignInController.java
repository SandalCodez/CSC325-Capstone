
package com.example.controllers;

import com.example.models.Portfolio;
import com.example.models.User;
import com.example.services.*;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SignInController {

    @FXML
    private TextField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private Label signInErrorLabel;
    @FXML
    private Button devSignIn;

    private FirestoreDB db;
    private UserAuth userAuth;
    private FirebaseAuthService firebaseAuthService;
    private PortfolioIntegration portfolioIntegration;
    private Portfolio portfolio;


    public SignInController() {}

    public SignInController(FirestoreDB db) {
        this.db = db;
    }

    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, PortfolioIntegration portfolioIntegration) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.portfolioIntegration = portfolioIntegration;
    }

    public void setFirestoreDB(FirestoreDB db) {
        this.db = db;
    }

//===================================DELETE THIS========================================
    //*** Developer bypass
    //**
    //**
    @FXML
    void psuedoLogin(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/MainPortfolio.fxml"));
        Parent MainPortfolioRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(MainPortfolioRoot));
        stage.setTitle("MainPortfolio");
        stage.show();
    }
    //**
    //**
    //*** Developer bypass
    //=================================================================================


    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String email = usernameField.getText();
        String password = passwordField.getText();

        if(email.isEmpty() || password.isEmpty()) {
            // turn this into a label
            signInErrorLabel.setText("Username and Password are empty");
            return;

        }
            try {
                User user = userAuth.loginUser(email, password);

                // Get the UID from userAuth
                String uid = userAuth.getCurrentUserUid();

                // Store in session with both User object and UID
                UserSession.getInstance().setCurrentUser(user, uid, userAuth);

                System.out.println(("Login successful! Welcome " + user.getfName()+ ", success"));
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/MainPortfolio.fxml"));
                Parent MainPortfolioRoot = fxmlLoader.load();

                MainPortfolioController controller = fxmlLoader.getController();

                controller.setDependencies(db, userAuth, portfolio, portfolioIntegration);

                db.setPortfolioIntegration(portfolioIntegration);

                controller.setLoggedInUser(user);

                controller.initializeData();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(MainPortfolioRoot));
                stage.setTitle("MainPortfolio");
                stage.show();

            } catch (Exception e) {
                signInErrorLabel.setText("Login failed: " + e.getMessage());
                e.printStackTrace();
            }
        }



    @FXML
    private void handleNewUsers(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/Registration.fxml"));
        Parent RegistrationRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(RegistrationRoot));
        stage.setTitle("Registration");
        stage.show();
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
    }

    @FXML
    private void clearUsername(MouseEvent event) {
        usernameField.clear();
    }

    @FXML
    private void clearPassword(MouseEvent event) {
        passwordField.clear();
    }

}

