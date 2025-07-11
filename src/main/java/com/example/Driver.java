package com.example;

import com.example.services.FirestoreDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Driver extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Driver.class.getResource("/com/example/bearsfrontend/splash.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 600 , 400);

        FirestoreDB db = new FirestoreDB();
        db.connect();


        Scene scene = new Scene(fxmlLoader.load(), 700 , 480);

        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


