module com.robin.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires ojdbc8;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.base;

    requires java.logging;


    opens com.robin.pos to javafx.fxml;
    exports com.robin.pos;
    exports com.robin.pos.controller;
    opens com.robin.pos.controller to javafx.fxml;
}