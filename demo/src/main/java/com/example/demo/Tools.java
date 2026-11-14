package com.example.demo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Light;
import javafx.scene.image.Image;

public class Tools {
    public int price;
    public Image image;
    public Image iconImage;

    public Tools(int price, Image image, Image iconImage) {
        this.price = price;
        this.image = image;
        this.iconImage = iconImage;
    }

    public void draw(GraphicsContext context, Point2D position) {
        context.drawImage(image, position.getX(), position.getY());
    }
}
