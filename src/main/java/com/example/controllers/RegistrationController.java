package com.example.controllers;

import com.example.models.Portfolio;
import com.example.models.PortfolioEntry;
import com.example.models.User;
import com.example.services.*;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private FinnhubService finnhubService = new FinnhubService();
    private final ObservableList<PortfolioEntry> portfolioData = FXCollections.observableArrayList();
    private UserSession userSession;
    private User loggedInUser;
    private UserAuth userAuth;
    private FirestoreDB db;
    private PortfolioIntegration portfolioIntegration;
    private Portfolio portfolio;
    private String uid;


    @FXML private StackPane rootPane;
    @FXML private Group scalingPane;
    @FXML private ImageView bgImageView;
    double baseWidth = 600;
    double baseHeight = 400;

    public void setDependencies(FirestoreDB db, UserAuth userAuth, Portfolio portfolio, FinnhubService finnhubService, PortfolioIntegration portfolioIntegration, User loggedInUser, String uid) {
        this.db = db;
        this.userAuth = userAuth;
        this.portfolio = portfolio;
        this.finnhubService = finnhubService;
        this.portfolioIntegration = portfolioIntegration;
        this.loggedInUser = loggedInUser;
        this.uid = uid;
    }


    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {

        loggedInUser.logout();


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));
        Parent SignInRoot = fxmlLoader.load();
        SignInController controller = fxmlLoader.getController();

        FirestoreDB dbToPass = (db != null) ? db : new FirestoreDB();
        if (dbToPass != db && dbToPass.getFirestore() == null) {
            dbToPass.connect();
        }

        UserAuth userAuthToPass = (userAuth != null) ? userAuth : new UserAuth(dbToPass.getFirestore());
        Portfolio portfolioToPass = (portfolio != null) ? portfolio : new Portfolio();
        FinnhubService finnhubToPass = (finnhubService != null) ? finnhubService : new FinnhubService();

        controller.setSplashDependencies(db, userAuth, portfolio, finnhubService);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(SignInRoot));
        stage.setTitle("SignIn");
        stage.show();
    }

    @FXML
    void registerClick(ActionEvent event) throws Exception {
        try {
            registerError.setText("");
            confirmEmailError.setText("");
            confPassError.setText("");
            String email = emailField.getText();
            String emailConfirm = confirmEmailField.getText();
            String fName = fNameField.getText();
            String lName = lNameField.getText();
            String password = passwordField.getText();

            Firestore firestoreDB = FirestoreClient.getFirestore();
            UserAuth userAuth = new UserAuth(firestoreDB);

            LocalDate today = LocalDate.now();

            if ((email.isEmpty() || fName.isEmpty() || lName.isEmpty() || password.isEmpty())) {
                registerError.setText("REGISTRATION FAILED\nPlease fill all the fields");
                return;
            }
            if (!(email.equals(emailConfirm))) {
                confirmEmailError.setText("Please confirm your email.");
                registerError.setText("REGISTRATION FAILED");
                return;
            }
            if (!(password.equals(confirmPasswordField.getText()))) {
                confPassError.setText("Passwords do not match.");
                registerError.setText("REGISTRATION FAILED");
                return;
            }
            userAuth.registerUser(emailField.getText(), passwordField.getText(), fNameField.getText(), lNameField.getText(), today);

            registerError.setText("Registration Successful");
            System.out.println("Register: " + fNameField.getText() + " " + lNameField.getText() + " " + emailField.getText() + " " + passwordField.getText());

        } catch (Exception e) {
            registerError.setText("REGISTRATION FAILED\nPlease check your fields");
            if(e.getMessage().contains("EMAIL_EXISTS")) {
                registerError.setText("REGISTRATION FAILED\nEmail already exists");

            }

        }
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





