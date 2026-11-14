package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class HelloApplication extends Application {
    public static final double SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
    public static final double LEVEL_WIDTH = SCREEN_WIDTH, LEVEL_HEIGHT = SCREEN_HEIGHT;
    public static final int NBcases = 20, LENcases = 20;
    public static final int[] ORIGIN = {200, 100};
    public static final double FPS = 1;

    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT);
    public Canvas bgCanvas = new Canvas(LEVEL_WIDTH, LEVEL_HEIGHT);
    public GraphicsContext bgContext = bgCanvas.getGraphicsContext2D();
    public Random random = new Random();

    public double time = 0;
    public double indexTime = 1;
    public int[][] grid = new int[NBcases][NBcases];
    public Plan currentPlan = Plan.ACCUEIL;

    @Override
    public void start(Stage stage) throws IOException {
        loadNewScreen(currentPlan);

        AnimationTimer timer = new AnimationTimer() {
            long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (currentPlan == Plan.GAMEPLAY) {
                    double deltaTemps = (now - lastTime) * 1e-9;
                    time += deltaTemps;

                    if (time > indexTime * FPS) {
                        indexTime++;
                        createRandomInfectedCell();
                    }
                }

                lastTime = now;
            }
        };
        timer.start();

        stage.setTitle("PlaceHolder");
        stage.setScene(scene);
        stage.show();
    }

    public void loadNewScreen(Plan plan) {
        pane.getChildren().clear();
        pane.getChildren().add(bgCanvas);
        currentPlan = plan;
        switch (currentPlan) {
            case ACCUEIL -> addAccueil();
            case GAMEPLAY -> drawGrid();
        }
    }

    public void addAccueil() {
        VBox vBox = new VBox();

        ImageView virus = new ImageView(new Image("virus.png"));
        virus.setPreserveRatio(true);
        virus.setFitHeight(50);
        vBox.getChildren().add(virus);

        Button btnPlay = new Button("Jouer !");
        vBox.getChildren().add(btnPlay);
        btnPlay.setOnAction(event -> {loadNewScreen(Plan.GAMEPLAY);});

        pane.getChildren().add(vBox);
    }

    private void drawGrid() {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                bgContext.setStroke(Color.BLACK);
                bgContext.strokeRect(LENcases*x+ ORIGIN[0], LENcases*y+ ORIGIN[1], LENcases, LENcases);
            }
        }
    }

    public void createRandomInfectedCell() {
        int randomIndexX = random.nextInt(NBcases/4, 3*NBcases/4);
        int randomIndexY = random.nextInt(NBcases/4, 3*NBcases/4);
        bgContext.setFill(Color.RED);
        bgContext.fillRect(LENcases*randomIndexX+ORIGIN[0], LENcases*randomIndexY+ORIGIN[1], LENcases, LENcases);
    }
}
