package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class HelloApplication extends Application {
    public static final double SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
    public static final double LEVEL_WIDTH = SCREEN_WIDTH, LEVEL_HEIGHT = SCREEN_HEIGHT;

    public Pane pane = new Pane();
    public static Scene scene;

    public Canvas bgCanvas = new Canvas(LEVEL_WIDTH, LEVEL_HEIGHT);
    public GraphicsContext bgContext = bgCanvas.getGraphicsContext2D();

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));

        scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
       // addAll();

       /* AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTemps = (now - lastTime) * 1e-9;
                lastTime = now;
            }
        };*/
        //timer.start();

        /*scene.setOnKeyPressed(e -> {
            Input.setKeyPressed(e.getCode(), true);
        });*/

        /*scene.setOnKeyReleased(e -> {
            Input.setKeyPressed(e.getCode(), false);
        });*/

        stage.setTitle("PlaceHolder");
        stage.setScene(scene);
        stage.show();
    }

    /*public void addAll() {
        pane.getChildren().add(bgCanvas);

        int[] origin = {200, 100};
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                bgContext.setStroke(Color.BLACK);
                bgContext.strokeRect(20*x+origin[0], 20*y+origin[1], 20, 20);
            }
        }
    }
     */

    static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/fxml/" + fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
    }
}
