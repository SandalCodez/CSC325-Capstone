module com.example.csc325capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires google.cloud.firestore;
    requires com.google.gson;
    requires org.json;
    requires javax.websocket.api;


    opens com.example.csc325capstone to javafx.fxml;
    exports com.example.csc325capstone;
}