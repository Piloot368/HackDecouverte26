package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class Grid {
    private Random random = new Random();
    public static final int NBcases = 20, LENcases = 20;
    public static final Point2D ORIGIN = new Point2D(200, 100);
    private int[][] grid = new int[NBcases][NBcases];
    private int virusBuffer = 4;

    public void drawGrid(GraphicsContext context) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                context.setStroke(Color.BLACK);
                context.strokeRect(LENcases*x+ ORIGIN.getX(), LENcases*y+ ORIGIN.getY(), LENcases, LENcases);
            }
        }
    }

    public void createRandomInfectedCell(GraphicsContext context) {
        int randomIndexX = random.nextInt(virusBuffer, Grid.NBcases);
        int randomIndexY = random.nextInt(virusBuffer, Grid.NBcases);
        context.setFill(Color.RED);
        context.fillRect(LENcases*randomIndexX+ORIGIN.getX(), LENcases*randomIndexY+ORIGIN.getY(), LENcases, LENcases);
    }
}
