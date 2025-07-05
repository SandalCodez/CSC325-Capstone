package com.example.controllers;

import com.example.services.FirestoreDB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

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
    void registerClick(ActionEvent event) {
        FirestoreDB db = new FirestoreDB();
        db.connect();
        emailField.getText();
        fNameField.getText();
        lNameField.getText();
        passwordField.getText();
        System.out.println("Register: " + fNameField.getText() + " " + lNameField.getText() + " " + emailField.getText() + " " + passwordField.getText());
    }

    }





