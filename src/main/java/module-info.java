module com.example.csc325capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires google.cloud.firestore;


    opens com.example.csc325capstone to javafx.fxml;
    exports com.example.csc325capstone;
}