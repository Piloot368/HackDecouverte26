package com.example.kotlinfx_gradle

//import com.example.kotlinfx_gradle.GameController.*
import com.example.kotlinfx_gradle.GameController.Companion.FPS
import com.example.kotlinfx_gradle.GameController.Companion.tools
import com.example.kotlinfx_gradle.Plan
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import java.io.IOException
import java.lang.String
import java.util.*
import kotlin.Double
import kotlin.Long
import kotlin.Throws
import kotlin.collections.get
import kotlin.collections.plus
import kotlin.plus
import kotlin.sequences.plus
import kotlin.text.plus

class HelloApplication : Application() {
    var pane: Pane = Pane()
    var scene: Scene = Scene(pane, SCREEN_WIDTH, SCREEN_HEIGHT)
    var bgCanvas: Canvas = Canvas(SCREEN_WIDTH, SCREEN_HEIGHT)
    var bgContext: GraphicsContext = bgCanvas.getGraphicsContext2D()
    var GUICanvas: Canvas = Canvas(SCREEN_WIDTH, SCREEN_HEIGHT)
    var GUIContext: GraphicsContext = GUICanvas.getGraphicsContext2D()
    var controller: GameController = GameController()
    var random: Random = Random()

    var time: Double = 0.0
    var indexTime: Double = 1.0
    var currentPlan: Plan = Plan.ACCUEIL

    @Throws(IOException::class)
    override fun start(stage: Stage) {
        loadNewScreen(currentPlan)

        val timer: AnimationTimer = object : AnimationTimer() {
            var lastTime: Long = System.nanoTime()

            override fun handle(now: Long) {
                if (currentPlan === Plan.GAMEPLAY) {
                    val deltaTemps = (now - lastTime) * 1e-9
                    time += deltaTemps

                    if (time > indexTime * FPS) {
                        indexTime++
                        controller.drawGrid(bgContext)
                        controller.nextTick(bgContext)
                        loadGUI()
                    }
                }

                lastTime = now
            }
        }
        timer.start()

        scene.setOnKeyPressed(EventHandler { event: KeyEvent? ->
            when (event!!.getCode()) {
                KeyCode.ESCAPE -> {
                    if (currentPlan === Plan.ACCUEIL) {
                        Platform.exit()
                        //return@setOnKeyPressed
                    }
                    loadNewScreen(Plan.ACCUEIL)
                }

                KeyCode.P -> {
                    //PAUSE LOGIC
                }

                else -> {}
            }
        })

        scene.setOnMousePressed(EventHandler { event: MouseEvent? ->
            controller.verifyHitboxes(Point2D(event!!.getX(), event.getY()), bgContext)
        })

        stage.setTitle("PlaceHolder")
        stage.setScene(scene)
        stage.show()
    }

    fun loadNewScreen(plan: Plan) {
        pane.getChildren().clear()
        pane.getChildren().add(bgCanvas)
        pane.getChildren().add(GUICanvas)
        currentPlan = plan
        when (currentPlan) {
            Plan.ACCUEIL -> addAccueil()
            Plan.GAMEPLAY -> addGameplay()
            else -> {}
        }
    }

    fun addAccueil() {
        val hBox = HBox()
        hBox.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT)
        hBox.setAlignment(Pos.CENTER)
        val vBox = VBox(15.0)
        vBox.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT)
        vBox.setAlignment(Pos.CENTER)

        val virus = ImageView(Image("virus.png"))
        virus.setPreserveRatio(true)
        virus.setFitHeight(200.0)
        vBox.getChildren().add(virus)

        val btnPlay = Button("Jouer !")
        vBox.getChildren().add(btnPlay)
        btnPlay.setOnAction(EventHandler { event: ActionEvent? -> loadNewScreen(Plan.GAMEPLAY) })

        hBox.getChildren().add(vBox)
        pane.getChildren().add(hBox)
    }

    fun addGameplay() {
        controller.drawGrid(bgContext)
        loadGUI()
    }

    fun loadGUI() {
        GUIContext.clearRect(0.0, 0.0, SCREEN_WIDTH, SCREEN_HEIGHT)

        GUIContext.setFill(Color.BLACK)
        GUIContext.setFont(Font.font(12.0))
        GUIContext.fillText("money : " + controller.getMoney() + "$", 20.0, 20.0)
        drawTools()
    }

    fun drawTools() {
        for (i in tools!!.indices) {
            GUIContext.drawImage(tools[i]?.iconImage, SCREEN_WIDTH - 80 - 20, (20 + 90 * i).toDouble())
            GUIContext.setFill(Color.BLACK)
            GUIContext.setFont(Font.font(12.0))
            GUIContext.fillText(String.valueOf(tools[i]?.price) + "$", SCREEN_WIDTH - 80 - 20, (110 + 90 * i).toDouble())
        }
    }

    companion object {
        const val SCREEN_WIDTH: Double = 800.0
        const val SCREEN_HEIGHT: Double = 600.0
    }
}