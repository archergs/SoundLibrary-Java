package com.archergs.soundlibrary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SoundLibraryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SoundLibraryApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("SoundLibrary");
        stage.setMinHeight(500);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.show();
    }

    /*public static void main(String[] args) {
        // create download dir if it isn't there already
        new File(System.getProperty("user.home") + "/SoundLibrary/").mkdirs();

        launch();
    }*/
}