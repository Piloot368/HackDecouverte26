package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

import static com.example.demo.GameController.*;

public class HelloApplication extends Application {
    public static final double SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;

    public Pane pane = new Pane();
    public Scene scene = new Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT);
    public Canvas bgCanvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
    public GraphicsContext bgContext = bgCanvas.getGraphicsContext2D();
    public Canvas GUICanvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
    public GraphicsContext GUIContext = GUICanvas.getGraphicsContext2D();
    public GameController controller = new GameController();
    public Random random = new Random();

    public double time = 0;
    public double indexTime = 1;
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
                        controller.nextTick();
                        loadGUI();
                        Virus.createRandomInfectedCell(bgContext);
                    }
                }

                lastTime = now;
            }
        };
        timer.start();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE -> {
                    if (currentPlan == Plan.ACCUEIL) {
                        Platform.exit();
                        return;
                    }
                    loadNewScreen(Plan.ACCUEIL);
                }
                case P -> {
                    //PAUSE LOGIC
                }
            }
        });

        scene.setOnMousePressed(event -> {
            controller.verifyHitboxes(new Point2D(event.getX(), event.getY()), bgContext);
        });

        stage.setTitle("PlaceHolder");
        stage.setScene(scene);
        stage.show();
    }

    public void loadNewScreen(Plan plan) {
        pane.getChildren().clear();
        pane.getChildren().add(bgCanvas);
        pane.getChildren().add(GUICanvas);
        currentPlan = plan;
        switch (currentPlan) {
            case ACCUEIL -> addAccueil();
            case GAMEPLAY -> addGameplay();
        }
    }

    public void addAccueil() {
        HBox hBox = new HBox();
        hBox.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        hBox.setAlignment(Pos.CENTER);
            VBox vBox = new VBox(15);
            vBox.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            vBox.setAlignment(Pos.CENTER);

            ImageView virus = new ImageView(new Image("virus.png"));
            virus.setPreserveRatio(true);
            virus.setFitHeight(200);
            vBox.getChildren().add(virus);

            Button btnPlay = new Button("Jouer !");
            vBox.getChildren().add(btnPlay);
            btnPlay.setOnAction(event -> {loadNewScreen(Plan.GAMEPLAY);});

        hBox.getChildren().add(vBox);
        pane.getChildren().add(hBox);
    }

    public void addGameplay() {
        controller.drawGrid(bgContext);
        loadGUI();
    }

    public void loadGUI() {
        GUIContext.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        GUIContext.setFill(Color.BLACK);
        GUIContext.setFont(Font.font(12));
        GUIContext.fillText("money : "+controller.getMoney()+"$", 20, 20);
        drawTools();
    }

    public void drawTools() {
        for (int i = 0; i < tools.length; i++) {
            GUIContext.drawImage(tools[i].iconImage, SCREEN_WIDTH-80-20, 20+90*i);
            GUIContext.setFill(Color.BLACK);
            GUIContext.setFont(Font.font(12));
            GUIContext.fillText(String.valueOf(tools[i].price)+"$", SCREEN_WIDTH-80-20, 110+90*i);
        }
    }
}
