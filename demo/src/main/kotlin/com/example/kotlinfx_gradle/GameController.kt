package com.example.kotlinfx_gradle

import com.example.kotlinfx_gradle.HelloApplication.Companion.SCREEN_WIDTH
import com.example.kotlinfx_gradle.caseType
import javafx.geometry.Point2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import java.util.*

class GameController {
    var random: Random = Random()

    var money: Double = 0.0
    var grid: Array<IntArray?> = Array<IntArray?>(NBcases) { IntArray(NBcases) }
    var picking: caseType = caseType.EMPTY
    var enemies: ArrayList<Virus?> = ArrayList<Virus?>()
    var nbEnemiesPerWave: Int = 1
    var nbEnemies: Int = 0

    fun getMoney(): Int {
        return money.toInt()
    }

    fun nextTick(context: GraphicsContext?) {
        money += moneyGeneratorPerSecond
        for (y in grid.indices) {
            for (x in grid[y]!!.indices) {
                if (grid[y]!![x] <= -1) {
                    propagateVirus(Point2D(x.toDouble(), y.toDouble()))
                }
            }
        }
        if (nbEnemies <= nbEnemiesPerWave) {
            nbEnemies++
            createRandomInfectedCell(context)
        }
        //enemies.getFirst().printPos()
    }

    fun drawGrid(context: GraphicsContext) {
        for (y in grid.indices) {
            for (x in grid[y]!!.indices) {
                context.setStroke(Color.BLACK)
                context.strokeRect(
                    LENcases * x + ORIGIN.getX(),
                    LENcases * y + ORIGIN.getY(),
                    LENcases.toDouble(),
                    LENcases.toDouble()
                )
                if (grid[y]!![x] <= -1) {
                    if (grid[y]!![x] == caseType.VIRUS.index || grid[y]!![x] == caseType.ESTABLISHED.index) {
                        context.setFill(Color.RED)
                    } else if (grid[y]!![x] == caseType.BOURGEON.index) {
                        context.setFill(Color.PINK)
                    }
                    context.fillRect(
                        LENcases * x + ORIGIN.getX(),
                        LENcases * y + ORIGIN.getY(),
                        LENcases.toDouble(),
                        LENcases.toDouble()
                    )
                }
            }
        }
    }

    fun verifyHitboxes(mousePos: Point2D, context: GraphicsContext?) {
        if (picking === caseType.EMPTY) {
            if (mousePos.getX() > SCREEN_WIDTH - 80 - 20 && mousePos.getX() < SCREEN_WIDTH - 20) {
                if (mousePos.getY() > 20 && mousePos.getY() < 80 + 20) {
                    if (tools[0]!!.price <= money) {
                        picking = caseType.get(tools[0])
                        money -= tools[0]!!.price
                    }
                }
            }
        } else {
            if (mousePos.getX() > ORIGIN.getX() && mousePos.getX() < ORIGIN.getX() + NBcases * LENcases) {
                if (mousePos.getY() > ORIGIN.getY() && mousePos.getY() < ORIGIN.getY() + NBcases * LENcases) {
                    val col = ((mousePos.getX() - ORIGIN.getX()) / LENcases).toInt()
                    val row = ((mousePos.getY() - ORIGIN.getY()) / LENcases).toInt()

                    grid[row]!![col] = picking.index
                    picking = caseType.EMPTY
                    tools[0]!!.draw(context, Point2D(col * LENcases + ORIGIN.getX(), row * LENcases + ORIGIN.getY()))
                }
            }
        }
    }

    fun createRandomInfectedCell(context: GraphicsContext?) {
        val randomIndexX = random.nextInt(NBcases / 4, 3 * NBcases / 4)
        val randomIndexY = random.nextInt(NBcases / 4, 3 * NBcases / 4)
        val virus: Virus = Virus(randomIndexX.toDouble(), randomIndexY.toDouble())
        enemies.add(virus)
        grid[randomIndexY]!![randomIndexX] = caseType.VIRUS.index
    }

    fun propagateVirus(position: Point2D) {
        val xVoisin = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        val yVoisin = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)

        for (i in yVoisin.indices) {
            if ((position.getY() + yVoisin[i]).toInt() >= 0 && (position.getY() + yVoisin[i]).toInt() < NBcases && (position.getX() + xVoisin[i]).toInt() >= 0 && (position.getX() + xVoisin[i]).toInt() < NBcases) {
                if (grid[(position.getY() + yVoisin[i]).toInt()]!![(position.getX() + xVoisin[i]).toInt()] == caseType.EMPTY.index) {
                    val progate = random.nextInt(1, PERCENTAGE_OF_PROPAGATION) == 1
                    if (progate) {
                        grid[(position.getY() + yVoisin[i]).toInt()]!![(position.getX() + xVoisin[i]).toInt()] =
                            caseType.BOURGEON.index
                    }
                } else if (grid[(position.getY() + yVoisin[i]).toInt()]!![(position.getX() + xVoisin[i]).toInt()] == caseType.BOURGEON.index) {
                    grid[(position.getY() + yVoisin[i]).toInt()]!![(position.getX() + xVoisin[i]).toInt()] =
                        caseType.ESTABLISHED.index
                }
            }
        }
    }

    companion object {
        const val NBcases: Int = 20
        const val LENcases: Int = 20
        val ORIGIN: Point2D = Point2D(200.0, 100.0)
        val FPS: Double = 1.0 / 60.0
        val moneyGeneratorPerSecond: Double = 3.0 * FPS
        val PERCENTAGE_OF_PROPAGATION: Int = 2000 + 1
        var tools: Array<Tools?> = arrayOf<Tools?>(Firewall())
    }
}