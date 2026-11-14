package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

import static com.example.demo.HelloApplication.*;

public class Virus {
    private static Random random = new Random();
    private int buffer = 4;

    public static void createRandomInfectedCell(GraphicsContext context) {
        int randomIndexX = random.nextInt(NBcases/4, 3*NBcases/4);
        int randomIndexY = random.nextInt(NBcases/4, 3*NBcases/4);
        context.setFill(Color.RED);
        context.fillRect(LENcases*randomIndexX+ORIGIN.getX(), LENcases*randomIndexY+ORIGIN.getY(), LENcases, LENcases);
    }
}
