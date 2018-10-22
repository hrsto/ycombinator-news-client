module controllers {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive entities;
    requires org.jsoup;

    exports com.webarity.controllers;
    exports com.webarity.controllers.events;
}