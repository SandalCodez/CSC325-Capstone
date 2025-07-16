package com.example.controllers;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML private StackPane rootPane;
    @FXML private Group scalingPane;
    @FXML private ImageView bgImageView;
    double baseWidth = 1200;
    double baseHeight = 800;

    @FXML
    public void initialize() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            try {
                Parent signInRoot = FXMLLoader.load(getClass().getResource("/com/example/bearsfrontend/SignIn.fxml"));
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setScene(new Scene(signInRoot));
                stage.setTitle("Sign In");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();

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

