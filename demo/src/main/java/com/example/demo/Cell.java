package com.example.demo;

public class Cell {
    private CellState state = CellState.EMPTY;
    private double scale = 1.0;

    public CellState getState() {
        return state;
    }

    public void setState(CellState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.scale = 0.1;
        }
    }

    public void updateAnimation(double deltaTime) {
        if (scale < 1.0) {
            scale = Math.min(1.0, scale + deltaTime * 6.0);
        }
    }

    public double getScale() {
        return scale;
    }
}
