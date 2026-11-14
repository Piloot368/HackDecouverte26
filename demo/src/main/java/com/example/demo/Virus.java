package com.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

import static com.example.demo.HelloApplication.*;

public class Virus {
    private Random random = new Random();
    private int buffer = 4;

    public void createRandomInfectedCell(GraphicsContext context) {
        int randomIndexX = random.nextInt(4, HelloApplication.NBcases);
        int randomIndexY = random.nextInt(HelloApplication.NBcases/4, 3*HelloApplication.NBcases/4);
        context.setFill(Color.RED);
        context.fillRect(LENcases*randomIndexX+ORIGIN.getX(), LENcases*randomIndexY+ORIGIN.getY(), LENcases, LENcases);
    }
}
