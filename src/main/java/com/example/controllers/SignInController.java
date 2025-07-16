
package com.example.controllers;

import com.example.models.User;
import com.example.services.FirebaseAuthService;
import com.example.services.UserAuth;
import com.example.services.UserSession;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
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
    Firestore firestoreDB = FirestoreClient.getFirestore();
    UserAuth userAuth = new UserAuth(firestoreDB);

    private FirebaseAuthService firebaseAuthService;
    @FXML
    private StackPane rootPane;
    @FXML
    private Group scalingPane;
    @FXML
    private ImageView bgImageView;
    double baseWidth = 1200;
    double baseHeight = 800;


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

        if (email.isEmpty() || password.isEmpty()) {
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

            System.out.println(("Login successful! Welcome " + user.getfName() + "success"));
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/MainPortfolio.fxml"));
            Parent MainPortfolioRoot = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(MainPortfolioRoot));
            stage.setTitle("MainPortfolio");
            stage.show();


        } catch (Exception e) {
            signInErrorLabel.setText("Login failed: " + e.getMessage());
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

    @FXML
    public void initialize() {
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

