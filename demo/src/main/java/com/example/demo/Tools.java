package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;

public class Tools {
    public Point2D position;
    public int price;
    public Image image;

    public void draw(GraphicsContext context) {
        context.drawImage(image, position.getX(), position.getY());
    }
}
