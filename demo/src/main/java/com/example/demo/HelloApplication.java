package com.example.demo;

import javafx.animation.AnimationTimer;
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
        addAll();

        AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTemps = (now - lastTime) * 1e-9;

                lastTime = now;
            }
        };
        timer.start();

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

    public void addAll() {
        
    }
}
