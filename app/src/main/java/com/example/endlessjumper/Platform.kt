package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Platform(
    var x: Float,
    var y: Float,
    val width: Float = 200f,
    val height: Float = 30f
) {
    private val platformType = Random.nextInt(3)

    private val craters = List(3) {
        Pair(
            Random.nextFloat() * width * 0.8f + width * 0.1f,
            Random.nextFloat() * height * 0.6f + height * 0.2f
        )
    }

    fun update() {
        // No animation
    }

    fun draw(canvas: Canvas) {
        when (platformType) {
            0 -> drawAsteroid(canvas)
            1 -> drawSpaceStation(canvas)
            2 -> drawCrystal(canvas)
        }
    }

    private fun drawAsteroid(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.rgb(90, 80, 70)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(120, 110, 100)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawRoundRect(x, y, x + width, y + height, 15f, 15f, paint)
        canvas.drawRoundRect(x, y, x + width, y + height, 15f, 15f, borderPaint)

        val craterPaint = Paint().apply {
            color = Color.rgb(60, 55, 50)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        craters.forEach { (cx, cy) ->
            canvas.drawCircle(x + cx, y + cy, 5f, craterPaint)
        }
    }

    private fun drawSpaceStation(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.rgb(50, 60, 90)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(0, 200, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawRoundRect(x, y, x + width, y + height, 10f, 10f, paint)
        canvas.drawRoundRect(x, y, x + width, y + height, 10f, 10f, borderPaint)

        val lightPaint = Paint().apply {
            color = Color.rgb(0, 255, 200)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val lightCount = (width / 40f).toInt()
        for (i in 0 until lightCount) {
            canvas.drawCircle(x + 20f + i * 40f, y + height / 2, 4f, lightPaint)
        }
    }

    private fun drawCrystal(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.rgb(180, 220, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(100, 180, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, paint)
        canvas.drawRoundRect(x, y, x + width, y + height, 8f, 8f, borderPaint)

        val facetPaint = Paint().apply {
            color = Color.rgb(220, 240, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(x + width * 0.3f, y, x + width * 0.3f, y + height, facetPaint)
        canvas.drawLine(x + width * 0.7f, y, x + width * 0.7f, y + height, facetPaint)
    }

    fun isPlayerLanding(player: Player): Boolean {
        val playerBottom = player.getBottom()
        val playerPreviousBottom = playerBottom - player.velocityY

        if (player.velocityY <= 0) return false

        val playerLeft = player.getLeft()
        val playerRight = player.getRight()

        val horizontalOverlap = playerRight > x && playerLeft < x + width
        val verticalCollision = playerPreviousBottom <= y && playerBottom >= y

        return horizontalOverlap && verticalCollision
    }

    fun getTop() = y
    fun getBottom() = y + height
    fun getLeft() = x
    fun getRight() = x + width
}