package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static com.example.demo.HelloApplication.SCREEN_WIDTH;

public class GameController {
    public static final int NBcases = 20, LENcases = 20;
    public static final Point2D ORIGIN = new Point2D(200, 100);
    public static final double FPS = 1;
    public static final double moneyGeneratorPerSecond = 3*FPS;
    public static Tools[] tools = {new Firewall()};

    public int money = 0;
    public int[][] grid = new int[NBcases][NBcases];
    public caseType picking = caseType.EMPTY;

    public int getMoney() {
        return money;
    }

    public void nextTick() {
        money += moneyGeneratorPerSecond;
    }

    public void drawGrid(GraphicsContext context) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                context.setStroke(Color.BLACK);
                context.strokeRect(LENcases*x+ ORIGIN.getX(), LENcases*y+ ORIGIN.getY(), LENcases, LENcases);
                if (grid[y][x] == caseType.FIREWALL.getIndex()) {
                    tools[0].draw(context, new Point2D(x*LENcases+ORIGIN.getX(), y*LENcases+ORIGIN.getY()));
                    System.out.println("SUCCESS");
                }
            }
        }
    }

    public void verifyHitboxes(Point2D mousePos) {
        if (picking == caseType.EMPTY) {
            if (mousePos.getX() > SCREEN_WIDTH - 80 - 20 && mousePos.getX() < SCREEN_WIDTH - 20) {
                if (mousePos.getY() > 20 && mousePos.getY() < 80 + 20) {
                    if (tools[0].price <= money) {
                        picking = caseType.get(tools[0]);
                        money -= tools[0].price;
                    }
                }
            }
        } else {
            if (mousePos.getX() > ORIGIN.getX() && mousePos.getX() < ORIGIN.getX()+NBcases*LENcases) {
                if (mousePos.getY() > ORIGIN.getY() && mousePos.getY() < ORIGIN.getY()+NBcases*LENcases) {
                    int col = (int) ((mousePos.getX() - ORIGIN.getX()) / LENcases);
                    int row = (int) ((mousePos.getY() - ORIGIN.getY()) / LENcases);

                    grid[row][col] = picking.getIndex();
                    picking = caseType.EMPTY;
                    System.out.println(grid);
                }
            }
        }
    }
}
