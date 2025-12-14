package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

enum class PowerUpType {
    SHIELD,
    SUPER_JUMP,
    SPEED_BOOST,
    SLOW_FALL
}

class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType
) {
    private val size = 80f  // Increased from 50f to 80f
    private var collected = false

    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f  // Increased from 4f
        color = Color.WHITE
    }

    // Glow effect paint
    private val glowPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas) {
        if (collected) return

        paint.color = when (type) {
            PowerUpType.SHIELD -> Color.rgb(100, 149, 237)
            PowerUpType.SUPER_JUMP -> Color.rgb(255, 99, 71)
            PowerUpType.SPEED_BOOST -> Color.rgb(255, 215, 0)
            PowerUpType.SLOW_FALL -> Color.rgb(135, 206, 250)
        }

        // Draw glow effect (outer circle with alpha)
        glowPaint.color = when (type) {
            PowerUpType.SHIELD -> Color.argb(80, 100, 149, 237)
            PowerUpType.SUPER_JUMP -> Color.argb(80, 255, 99, 71)
            PowerUpType.SPEED_BOOST -> Color.argb(80, 255, 215, 0)
            PowerUpType.SLOW_FALL -> Color.argb(80, 135, 206, 250)
        }
        canvas.drawCircle(x + size / 2, y + size / 2, size / 2 + 10f, glowPaint)

        // Draw power-up circle
        canvas.drawCircle(x + size / 2, y + size / 2, size / 2, paint)
        canvas.drawCircle(x + size / 2, y + size / 2, size / 2, borderPaint)

        // Draw icon/emoji - bigger size
        val iconPaint = Paint().apply {
            textSize = 50f  // Increased from 35f
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
        }

        val icon = when (type) {
            PowerUpType.SHIELD -> "🛡"
            PowerUpType.SUPER_JUMP -> "🚀"
            PowerUpType.SPEED_BOOST -> "⚡"
            PowerUpType.SLOW_FALL -> "🪂"
        }

        canvas.drawText(icon, x + size / 2, y + size / 2 + 17f, iconPaint)
    }

    fun checkCollision(player: Player): Boolean {
        if (collected) return false

        val playerCenterX = player.getCenterX()
        val playerCenterY = player.getCenterY()
        val powerUpCenterX = x + size / 2
        val powerUpCenterY = y + size / 2

        val dx = playerCenterX - powerUpCenterX
        val dy = playerCenterY - powerUpCenterY
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        val collisionRadius = size / 2 + player.width / 2

        if (distance < collisionRadius) {
            collected = true
            return true
        }

        return false
    }

    fun isCollected() = collected

    fun getLeft() = x
    fun getRight() = x + size
    fun getTop() = y
    fun getBottom() = y + size
}