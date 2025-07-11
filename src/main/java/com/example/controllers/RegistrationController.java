package com.example.controllers;

import com.example.services.FirestoreDB;
import com.example.services.UserAuth;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

public class RegistrationController {
    @FXML
    private TextField emailField;

    @FXML
    private TextField fNameField;

    @FXML
    private TextField lNameField;

    @FXML
    private TextField passwordField;

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));
        Parent SignInRoot = fxmlLoader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(SignInRoot));
        stage.setTitle("SignIn");
        stage.show();
    }

    @FXML
    void registerClick(ActionEvent event) throws Exception {
        Firestore firestoreDB = FirestoreClient.getFirestore();
      UserAuth userAuth = new UserAuth(firestoreDB);

      LocalDate today = LocalDate.now();

      userAuth.registerUser(emailField.getText(), passwordField.getText(), fNameField.getText(), lNameField.getText(), today);

        emailField.getText();
        fNameField.getText();
        lNameField.getText();
        passwordField.getText();
        System.out.println("Register: " + fNameField.getText() + " " + lNameField.getText() + " " + emailField.getText() + " " + passwordField.getText());
    }

    }





