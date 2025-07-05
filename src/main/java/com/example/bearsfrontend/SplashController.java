package com.example.bearsfrontend;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private AnchorPane splashRoot;

    @FXML
    public void initialize() {
        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(event -> {
            try {
                Parent signInRoot = FXMLLoader.load(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));
                Stage stage = (Stage) splashRoot.getScene().getWindow();
                stage.setScene(new Scene(signInRoot));
                stage.setTitle("Sign In");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }
}
