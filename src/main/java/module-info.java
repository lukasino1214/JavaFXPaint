module org.example.javafxpaint {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafxpaint to javafx.fxml;
    exports org.example.javafxpaint;
}