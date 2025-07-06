module com.example.models {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires firebase.admin;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;
    requires com.google.gson;
    requires org.json;
    requires java.net.http;
    requires org.slf4j;
    requires Java.WebSocket;

    // Export both packages
    exports com.example.models;
//    exports com.example.bearsfrontend;

    // Open both packages to JavaFX and Gson
    opens com.example.models to javafx.fxml, com.google.gson;
    opens com.example.bearsfrontend to javafx.fxml, com.google.gson;
    exports com.example.controllers;
    opens com.example.controllers to com.google.gson, javafx.fxml;
    exports com.example;
    opens com.example to com.google.gson, javafx.fxml;
    exports com.example.services;
    opens com.example.services to com.google.gson, javafx.fxml;
}