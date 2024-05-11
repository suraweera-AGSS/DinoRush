package com.suraweeraagss.dinorush

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

@SuppressLint("ViewConstructor")
class GameView(
    c: Context,
    private var gameTask: GameTask,
    private val sharedPreferences: SharedPreferences
) : View(c) {
    private var myPaint: Paint? = null
    private var speed = 1
    private var time = 1
    private var score = 0
    private var manPosition = 0
    private val otherDinosaurs = ArrayList<HashMap<String, Any>>()

    private var viewWidth = 0
    private var viewHeight = 0

    init {
        myPaint = Paint()
    }

    @SuppressLint("DrawAllocation", "UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (0..2).random()
            map["startTime"] = time
            otherDinosaurs.add(map)
        }

        time += 10 + speed
        val dinosaurWidth = 200
        val dinosaurHeight = 200
        myPaint!!.style = Paint.Style.FILL
        val d = resources.getDrawable(R.drawable.man, null)

        d.setBounds(
            manPosition * viewWidth / 3,
            viewHeight - 2 - dinosaurHeight,
            manPosition * viewWidth / 3 + dinosaurWidth,
            viewHeight - 2
        )

        d.draw(canvas)
        myPaint!!.color = Color.GREEN

        // Retrieve high score from SharedPreferences
        val highScore = sharedPreferences.getInt(PREF_HIGH_SCORE_KEY, 0)

        for (i in otherDinosaurs.indices) {
            try {
                val dinosaurX = otherDinosaurs[i]["lane"] as Int * viewWidth / 3
                val dinosaurY = time - otherDinosaurs[i]["startTime"] as Int
                val d2 = resources.getDrawable(R.drawable.dinosaur, null)

                d2.setBounds(
                    dinosaurX + 25, // Adjusted to center the Dinosaur horizontally
                    dinosaurY - dinosaurHeight, // Adjusted to place the Dinosaur above the screen
                    dinosaurX + dinosaurWidth - 25, // Adjusted to center the Dinosaur horizontally
                    dinosaurY // Adjusted to place the Dinosaur above the screen
                )

                d2.draw(canvas)
                if (otherDinosaurs[i]["lane"] as Int == manPosition) {
                    val manBottom = viewHeight - 2
                    val dinosaurBottom = dinosaurY + dinosaurHeight // Adjusted to include the bottom of the Dinosaur

                    // Check if the Dinosaur's bottom edge intersects with the man's area
                    if (dinosaurBottom > manBottom && dinosaurY < manBottom) {
                        // Man killed
                        gameTask.closeGame(score)
                    }
                }

                if (dinosaurY > viewHeight + dinosaurHeight) {
                    otherDinosaurs.removeAt(i)
                    score++
                    speed = 1 + abs(score / 8)

                    // Update high score if necessary
                    if (score > highScore) {
                        sharedPreferences.edit().putInt(PREF_HIGH_SCORE_KEY, score).apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        myPaint!!.color = Color.WHITE
        myPaint!!.textSize = 40f
        canvas.drawText("Score: $score", 80f, 80f, myPaint!!)
        canvas.drawText("Speed: $speed", 380f, 80f, myPaint!!)
        canvas.drawText("High Score: $highScore", 680f, 80f, myPaint!!)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                if (x < viewWidth / 2) {
                    if (manPosition > 0) {
                        manPosition--
                    }
                }
                if (x > viewWidth / 2) {
                    if (manPosition < 2) {
                        manPosition++
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }

    companion object {
        private const val PREF_HIGH_SCORE_KEY = "high_score"
    }
}
