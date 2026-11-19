package com.example.kotlinfx_gradle

import com.example.kotlinfx_gradle.GameController.Companion.LENcases
import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

open class Tools(var price: Int, var image: Image?, var iconImage: Image?) {
    fun draw(context: GraphicsContext?, position: Point2D) {
        context!!.drawImage(image, position.getX(), position.getY(), LENcases.toDouble(), LENcases.toDouble())
    }
}