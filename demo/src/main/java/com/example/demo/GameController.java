package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameController {
    public static final int NBcases = 20, LENcases = 20;
    public static final Point2D ORIGIN = new Point2D(200, 100);
    public static final double FPS = 1;
    public static final double moneyGeneratorPerSecond = 3*FPS;

    public int money = 0;
    public Tools[] tools = {new Firewall()};
    public int[][] grid = new int[NBcases][NBcases];

    public int getMoney() {
        return money;
    }

    public Tools[] getTools() {
        return tools;
    }

    public void nextTick() {
        money += moneyGeneratorPerSecond;
    }

    public void drawGrid(GraphicsContext context) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                context.setStroke(Color.BLACK);
                context.strokeRect(LENcases*x+ ORIGIN.getX(), LENcases*y+ ORIGIN.getY(), LENcases, LENcases);
            }
        }
    }
}
