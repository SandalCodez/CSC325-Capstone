module com.example.bearsfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.bearsfrontend to javafx.fxml;
    exports com.example.bearsfrontend;
}
