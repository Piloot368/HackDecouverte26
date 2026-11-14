package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, 800, 600);
    public Canvas bgCanvas = new Canvas();
    public GraphicsContext bgContext = bgCanvas.getGraphicsContext2D();

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

        stage.setTitle("PlaceHolder");
        stage.setScene(scene);
        stage.show();
    }

    public void addAll() {
        pane.getChildren().add(bgCanvas);
        bgContext.drawImage(new Image("nuit.png"), 0, 0);
    }
}
