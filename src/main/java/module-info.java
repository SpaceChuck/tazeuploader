module com.spacechuck.tazeuploader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.spacechuck.tazeuploader to javafx.fxml;
    exports com.spacechuck.tazeuploader;
}