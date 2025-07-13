package com.example;

import com.example.controllers.MainPortfolioController;
import com.example.controllers.SignInController;
import com.example.controllers.SplashController;
import com.example.models.Portfolio;
import com.example.services.FirestoreDB;
import com.example.services.PortfolioIntegration;
import com.example.services.UserAuth;
import com.google.cloud.firestore.Firestore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Driver extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FirestoreDB db = new FirestoreDB();
        db.connect();
        UserAuth userAuth = new UserAuth(db.getFirestore());
        Portfolio portfolio = new Portfolio();
        PortfolioIntegration portfolioIntegration = new PortfolioIntegration(db.getFirestore(), portfolio);

        FXMLLoader fxmlLoader = new FXMLLoader(Driver.class.getResource("/com/example/bearsfrontend/splash.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600 , 400);

        SplashController controller = fxmlLoader.getController();
        controller.setDependencies(db, userAuth, portfolio, portfolioIntegration);

        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


