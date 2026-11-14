package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    Pane pane = new Pane();
    Scene scene = new Scene(pane, 800, 600);

    @Override
    public void start(Stage stage) throws IOException {

        scene.setOnKeyPressed(e -> {
            Input.setKeyPressed(e.getCode(), true);
        });

        scene.setOnKeyReleased(e -> {
            Input.setKeyPressed(e.getCode(), false);
        });

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
