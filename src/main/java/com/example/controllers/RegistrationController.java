package com.example.controllers;

import com.example.services.FirestoreDB;
import com.example.services.UserAuth;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

public class RegistrationController {
    @FXML
    private Label confPassError;
    @FXML
    private Label confirmEmailError;
    @FXML
    private Label emailError;
    @FXML
    private Label fNameError;
    @FXML
    private Label lNameError;
    @FXML
    private Label registerError;
    @FXML
    private TextField emailField;
    @FXML
    private TextField confirmEmailField;

    @FXML
    private TextField fNameField;

    @FXML
    private TextField lNameField;

    @FXML
    private TextField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML private StackPane rootPane;
    @FXML private Group scalingPane;
    @FXML private ImageView bgImageView;
    double baseWidth = 600;
    double baseHeight = 400;


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
        String email = emailField.getText();
        String emailConfirm = confirmEmailField.getText();
        String fName = fNameField.getText();
        String lName = lNameField.getText();
        String password = passwordField.getText();

        Firestore firestoreDB = FirestoreClient.getFirestore();
      UserAuth userAuth = new UserAuth(firestoreDB);

      LocalDate today = LocalDate.now();

      if((email.isEmpty()||fName.isEmpty()||lName.isEmpty()||password.isEmpty())) {
          registerError.setText("Please fill all the fields");
          return;
      }
      if(!(email.equals(emailConfirm))){
              confirmEmailError.setText("Please confirm your email.");
              return;
      }
      if(!(password.equals(confirmPasswordField))){
          confPassError.setText("Passwords do not match.");
      }
          userAuth.registerUser(emailField.getText(), passwordField.getText(), fNameField.getText(), lNameField.getText(), today);

        emailField.getText();
        fNameField.getText();
        lNameField.getText();
        passwordField.getText();
        System.out.println("Register: " + fNameField.getText() + " " + lNameField.getText() + " " + emailField.getText() + " " + passwordField.getText());
    }

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





