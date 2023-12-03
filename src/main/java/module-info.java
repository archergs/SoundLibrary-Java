module com.archergs.soundlibrary {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    requires org.json;
    requires java.desktop;
    requires mp3agic;

    opens com.archergs.soundlibrary to javafx.fxml;
    exports com.archergs.soundlibrary;
}