
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
    private FinnhubService finnhubService;
    private User loggedInUser;
    private String uid;

    public SignInController() {}

    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, FinnhubService finnhubService, PortfolioIntegration portfolioIntegration, User loggedInUser, String uid) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
        this.portfolioIntegration = portfolioIntegration;
        this.loggedInUser = loggedInUser;
        this.uid = uid;
    }

    public void setSplashDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, FinnhubService finnhubService, PortfolioIntegration portfolioIntegration) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
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
                this.loggedInUser = userAuth.loginUser(email, password);
                System.out.println("loggedInUser UID before set: " + this.loggedInUser.getUid() );

                this.userAuth.setUser(this.loggedInUser);
                this.loggedInUser.setUserUid(userAuth.getCurrentUserUid());

                System.out.println("loggedInUser UID AFTER set: " + this.loggedInUser.getUid() );


                // Get the UID from userAuth
                this.uid = userAuth.getCurrentUserUid();

                System.out.println(("Login successful! Welcome " + loggedInUser.getfName()+ ", success"));
                System.out.println("Balance after login: " + portfolio.getBalance());
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/MainPortfolio.fxml"));
                Parent MainPortfolioRoot = fxmlLoader.load();

                MainPortfolioController controller = fxmlLoader.getController();

                controller.setDependencies(db, userAuth, portfolio, finnhubService, portfolioIntegration, loggedInUser, uid);

                db.setPortfolioIntegration(portfolioIntegration);

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
    public SignInController(FirestoreDB db) {
        this.db = db;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

}

