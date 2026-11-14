package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Random;

import static com.example.demo.HelloApplication.SCREEN_WIDTH;

public class GameController {
    public static final int NBcases = 20, LENcases = 20;
    public static final Point2D ORIGIN = new Point2D(200, 100);
    public static final double FPS = 1;
    public static final double moneyGeneratorPerSecond = 3*FPS;
    public static Tools[] tools = {new Firewall()};
    public Random random = new Random();

    public int money = 0;
    public int[][] grid = new int[NBcases][NBcases];
    public caseType picking = caseType.EMPTY;
    public ArrayList<Virus> enemies = new ArrayList<>();
    public int nbEnemiesPerWave = 1;
    public int nbEnemies = 0;

    public int getMoney() {
        return money;
    }

    public void nextTick(GraphicsContext context) {
        money += moneyGeneratorPerSecond;
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] <= -1) {
                    propagateVirus(new Point2D(x, y));
                }
            }
        }
        if (nbEnemies <= nbEnemiesPerWave) {
            nbEnemies++;
            createRandomInfectedCell(context);
        }
    }

    public void drawGrid(GraphicsContext context) {
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                context.setStroke(Color.BLACK);
                context.strokeRect(LENcases*x+ ORIGIN.getX(), LENcases*y+ ORIGIN.getY(), LENcases, LENcases);
                if (grid[y][x] <= -1) {
                    if (grid[y][x] == caseType.VIRUS.getIndex() || grid[y][x] == caseType.ESTABLISHED.getIndex()) {
                        context.setFill(Color.RED);
                    } else if (grid[y][x] == caseType.BOURGEON.getIndex()) {
                        context.setFill(Color.PINK);
                    }
                    context.fillRect(LENcases * x + ORIGIN.getX(), LENcases * y + ORIGIN.getY(), LENcases, LENcases);
                }
            }
        }
    }

    public void verifyHitboxes(Point2D mousePos, GraphicsContext context) {
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
                    tools[0].draw(context, new Point2D(col*LENcases+ORIGIN.getX(), row*LENcases+ORIGIN.getY()));
                }
            }
        }
    }
    public void createRandomInfectedCell(GraphicsContext context) {
        int randomIndexX = random.nextInt(NBcases/4, 3*NBcases/4);
        int randomIndexY = random.nextInt(NBcases/4, 3*NBcases/4);
        Virus virus = new Virus(randomIndexX, randomIndexY);
        enemies.add(virus);
        grid[randomIndexY][randomIndexX] = caseType.VIRUS.getIndex();
    }

    public void propagateVirus(Point2D position) {
        int[] xVoisin = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] yVoisin = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < yVoisin.length; i++) {
            if ((int) (position.getY()+yVoisin[i]) >= 0 && (int) (position.getY()+yVoisin[i]) < NBcases && (int) (position.getX()+xVoisin[i]) >= 0 && (int) (position.getX()+xVoisin[i]) < NBcases) {
                if (grid[(int) (position.getY() + yVoisin[i])][(int) (position.getX() + xVoisin[i])] != caseType.BOURGEON.getIndex()) {
                    boolean progate = random.nextInt(1, 11) == 1;
                    if (progate) {
                        grid[(int) (position.getY() + yVoisin[i])][(int) (position.getX() + xVoisin[i])] = caseType.BOURGEON.getIndex();
                    }
                } else {
                    grid[(int) (position.getY() + yVoisin[i])][(int) (position.getX() + xVoisin[i])] = caseType.ESTABLISHED.getIndex();
                }
            }
        }
    }
}
