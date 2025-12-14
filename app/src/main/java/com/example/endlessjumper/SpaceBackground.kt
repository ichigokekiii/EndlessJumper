package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val brightness: Int,
    val twinkleSpeed: Float
) {
    private var twinklePhase = Random.nextFloat() * 2 * Math.PI.toFloat()

    fun update() {
        twinklePhase += twinkleSpeed
    }

    fun draw(canvas: Canvas, paint: Paint) {
        val alpha = (brightness * (0.5f + 0.5f * Math.sin(twinklePhase.toDouble()))).toInt()
        paint.color = Color.argb(alpha, 255, 255, 255)
        canvas.drawCircle(x, y, size, paint)
    }
}

class SpaceBackground(private val screenWidth: Int, private val screenHeight: Int) {
    private val stars = mutableListOf<Star>()
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val nebulaColors = listOf(
        Color.argb(30, 138, 43, 226),  // Blue Violet
        Color.argb(30, 75, 0, 130),    // Indigo
        Color.argb(30, 72, 61, 139),   // Dark Slate Blue
        Color.argb(30, 147, 112, 219)  // Medium Purple
    )

    init {
        generateStars()
    }

    private fun generateStars() {
        // Create different layers of stars

        // Small distant stars
        repeat(100) {
            stars.add(
                Star(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    size = Random.nextFloat() * 2f + 1f,
                    brightness = Random.nextInt(100, 200),
                    twinkleSpeed = Random.nextFloat() * 0.02f + 0.01f
                )
            )
        }

        // Medium stars
        repeat(50) {
            stars.add(
                Star(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    size = Random.nextFloat() * 3f + 2f,
                    brightness = Random.nextInt(150, 255),
                    twinkleSpeed = Random.nextFloat() * 0.03f + 0.02f
                )
            )
        }

        // Large bright stars
        repeat(20) {
            stars.add(
                Star(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    size = Random.nextFloat() * 4f + 3f,
                    brightness = 255,
                    twinkleSpeed = Random.nextFloat() * 0.04f + 0.03f
                )
            )
        }
    }

    fun update() {
        stars.forEach { it.update() }
    }

    fun draw(canvas: Canvas) {
        // Draw deep space background gradient
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, 0f, screenHeight.toFloat(),
            Color.rgb(10, 10, 30),      // Dark blue at top
            Color.rgb(5, 5, 15),        // Almost black at bottom
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)
        paint.shader = null

        // Draw nebula clouds
        drawNebula(canvas)

        // Draw stars
        stars.forEach { it.draw(canvas, paint) }
    }

    private fun drawNebula(canvas: Canvas) {
        // Draw soft glowing nebula clouds
        repeat(3) { i ->
            val nebulaPaint = Paint().apply {
                color = nebulaColors[i % nebulaColors.size]
                style = Paint.Style.FILL
                maskFilter = android.graphics.BlurMaskFilter(100f, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }

            val x = screenWidth * (0.2f + i * 0.3f)
            val y = screenHeight * (0.3f + i * 0.2f)
            canvas.drawCircle(x, y, 200f, nebulaPaint)
        }
    }
}