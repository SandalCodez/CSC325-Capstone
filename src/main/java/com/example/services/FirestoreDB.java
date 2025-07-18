package com.example.services;

import com.example.models.Portfolio;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import java.io.IOException;

public class FirestoreDB {

    private Firestore firestore;
    private  Portfolio portfolio;
    public PortfolioIntegration portfolioIntegration;
    private FinnhubService finnhubService;


    public Firestore getFirestore() {
        return this.firestore;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public PortfolioIntegration getPortfolioIntegration() {
        return portfolioIntegration;
    }

    public void setPortfolioIntegration(PortfolioIntegration portfolioIntegration) {
        this.portfolioIntegration = portfolioIntegration;
    }

    public Firestore connect() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(getClass().getResourceAsStream("/files/bearsKey.json")))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase is initialized");
            this.firestore = FirestoreClient.getFirestore();
        } catch (IOException ex) {
            System.out.println("Firebase is not initialized"); if (this.portfolio == null) {
                this.portfolio = new Portfolio();
            }

            if (this.portfolioIntegration == null) {
                this.portfolioIntegration = new PortfolioIntegration(firestore, finnhubService, portfolio);
            }
            ex.printStackTrace();
        }
        return firestore;
    }
}