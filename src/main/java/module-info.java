module com.example.bearsfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires google.cloud.firestore;
    requires com.google.gson;
    requires org.json;
    requires javax.websocket.api;
    
    opens com.example.bearsfrontend to javafx.fxml;
    exports com.example.bearsfrontend;
}