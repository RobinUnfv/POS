module com.robin.pos {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.robin.pos to javafx.fxml;
    exports com.robin.pos;
    exports com.robin.pos.controller;
    opens com.robin.pos.controller to javafx.fxml;
}