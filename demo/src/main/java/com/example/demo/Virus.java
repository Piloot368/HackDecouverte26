package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Random;

import static com.example.demo.GameController.*;

public class Virus {
    private Point2D pos;
    private static Random random = new Random();
    private int buffer = 4;

    public Virus(Point2D pos) {
        this.pos = pos;
    }

    public Virus(double x, double y) {
        this.pos = new Point2D(x, y);
    }

    public void draw(GraphicsContext context) {
        context.setFill(Color.RED);
        context.fillRect(LENcases*pos.getX()+ORIGIN.getX(), LENcases*pos.getY()+ORIGIN.getY(), LENcases, LENcases);
    }
}
