package com.example.kotlinfx_gradle

import com.example.kotlinfx_gradle.GameController.Companion.LENcases
import com.example.kotlinfx_gradle.GameController.Companion.ORIGIN
import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import java.util.*

class Virus {
    private val pos: Point2D
    private val buffer = 4

    fun printPos() {
        println("x : " + pos.getX() + ",y : " + pos.getY())
    }

    constructor(pos: Point2D) {
        this.pos = pos
    }

    constructor(x: Double, y: Double) {
        this.pos = Point2D(x, y)
    }

    fun draw(context: GraphicsContext) {
        context.setFill(Color.RED)
        context.fillRect(
            LENcases * pos.getX() + ORIGIN.getX(),
            LENcases * pos.getY() + ORIGIN.getY(),
            LENcases.toDouble(),
            LENcases.toDouble()
        )
    }

    companion object {
        private val random = Random()
    }
}