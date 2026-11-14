package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML
    private Pane gamePane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        HelloApplication vue = new HelloApplication();
        gamePane = vue.pane;
    }
}
