package com.example.csc325capstone;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class registerController {

        @FXML
        private TextField emailField;

        @FXML
        private TextField firstNameField;

        @FXML
        private TextField lastNameField;

        @FXML
        private TextField passwordField;

        @FXML
        private Button registerBtn;

        @FXML
        void registerClicked(ActionEvent event) {
            emailField.getText();
            firstNameField.getText();
            lastNameField.getText();
            passwordField.getText();
            System.out.println("Register: " + firstNameField.getText() + " " + lastNameField.getText() + " " + emailField.getText() + " " + passwordField.getText());
        }

    }

