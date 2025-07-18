package com.example;

import com.example.controllers.MainPortfolioController;
import com.example.controllers.SignInController;
import com.example.controllers.SplashController;
import com.example.models.Portfolio;
import com.example.models.User;
import com.example.services.FinnhubService;
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

    public void start(Stage stage) throws Exception {


        FirestoreDB db = new FirestoreDB();
        db.connect();
        UserAuth userAuth = new UserAuth(db.getFirestore());
        Portfolio portfolio = new Portfolio();
        FinnhubService finnhubService = new FinnhubService();
        System.out.println("Balance before launching screen: " + portfolio.getBalance());

        FXMLLoader fxmlLoader = new FXMLLoader(Driver.class.getResource("/com/example/bearsfrontend/splash.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600 , 400);

        SplashController controller = fxmlLoader.getController();
        controller.setDependencies(db, userAuth, portfolio, finnhubService);

        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


