package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static final double SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
    public static final double LEVEL_WIDTH = SCREEN_WIDTH, LEVEL_HEIGHT = SCREEN_HEIGHT;

    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT);
    public Canvas bgCanvas = new Canvas(LEVEL_WIDTH, LEVEL_HEIGHT);
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
