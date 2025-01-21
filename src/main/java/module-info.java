module org.example.demo4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.javafx to javafx.fxml;
    exports com.javafx;
}