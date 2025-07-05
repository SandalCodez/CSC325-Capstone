module com.example.csc325capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires firebase.admin;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires google.cloud.storage;
    requires org.slf4j;
    requires google.cloud.firestore;

    opens com.example.csc325capstone to javafx.fxml;
    exports com.example.csc325capstone;
    requires java.desktop;
    requires google.cloud.firestore;
    requires com.google.gson;
    requires org.json;
    requires javax.websocket.api;

    opens com.example.bearsfrontend to javafx.fxml;
    exports com.example.bearsfrontend;
}