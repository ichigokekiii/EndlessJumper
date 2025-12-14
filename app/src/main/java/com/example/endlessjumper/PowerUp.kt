package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

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
    private val size = 80f
    private var collected = false

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.WHITE
        isAntiAlias = true
    }

    private val glowPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val iconPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        isAntiAlias = true
    }

    private val iconStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
        isAntiAlias = true
    }

    fun draw(canvas: Canvas) {
        if (collected) return

        val centerX = x + size / 2
        val centerY = y + size / 2

        paint.color = when (type) {
            PowerUpType.SHIELD -> Color.rgb(100, 149, 237)
            PowerUpType.SUPER_JUMP -> Color.rgb(255, 99, 71)
            PowerUpType.SPEED_BOOST -> Color.rgb(255, 215, 0)
            PowerUpType.SLOW_FALL -> Color.rgb(135, 206, 250)
        }

        // Draw glow effect
        glowPaint.color = when (type) {
            PowerUpType.SHIELD -> Color.argb(80, 100, 149, 237)
            PowerUpType.SUPER_JUMP -> Color.argb(80, 255, 99, 71)
            PowerUpType.SPEED_BOOST -> Color.argb(80, 255, 215, 0)
            PowerUpType.SLOW_FALL -> Color.argb(80, 135, 206, 250)
        }
        canvas.drawCircle(centerX, centerY, size / 2 + 10f, glowPaint)

        // Draw power-up circle background
        canvas.drawCircle(centerX, centerY, size / 2, paint)
        canvas.drawCircle(centerX, centerY, size / 2, borderPaint)

        // Draw custom icon based on type
        when (type) {
            PowerUpType.SHIELD -> drawShieldIcon(canvas, centerX, centerY)
            PowerUpType.SUPER_JUMP -> drawRocketIcon(canvas, centerX, centerY)
            PowerUpType.SPEED_BOOST -> drawLightningIcon(canvas, centerX, centerY)
            PowerUpType.SLOW_FALL -> drawParachuteIcon(canvas, centerX, centerY)
        }
    }

    private fun drawShieldIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        val shieldPath = Path().apply {
            moveTo(centerX, centerY - 22f)
            lineTo(centerX + 18f, centerY - 15f)
            lineTo(centerX + 18f, centerY + 5f)
            cubicTo(centerX + 18f, centerY + 15f, centerX + 10f, centerY + 22f, centerX, centerY + 25f)
            cubicTo(centerX - 10f, centerY + 22f, centerX - 18f, centerY + 15f, centerX - 18f, centerY + 5f)
            lineTo(centerX - 18f, centerY - 15f)
            close()
        }

        iconPaint.style = Paint.Style.FILL
        iconPaint.color = Color.WHITE
        canvas.drawPath(shieldPath, iconPaint)

        iconStrokePaint.strokeWidth = 3f
        iconStrokePaint.color = Color.rgb(100, 149, 237)
        iconStrokePaint.style = Paint.Style.STROKE
        canvas.drawPath(shieldPath, iconStrokePaint)
    }

    private fun drawRocketIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Rocket body
        val rocketPath = Path().apply {
            moveTo(centerX, centerY - 25f)
            lineTo(centerX + 8f, centerY - 15f)
            lineTo(centerX + 8f, centerY + 10f)
            lineTo(centerX, centerY + 15f)
            lineTo(centerX - 8f, centerY + 10f)
            lineTo(centerX - 8f, centerY - 15f)
            close()
        }

        iconPaint.color = Color.WHITE
        canvas.drawPath(rocketPath, iconPaint)

        // Rocket fins
        val leftFin = Path().apply {
            moveTo(centerX - 8f, centerY + 5f)
            lineTo(centerX - 15f, centerY + 15f)
            lineTo(centerX - 8f, centerY + 10f)
            close()
        }

        val rightFin = Path().apply {
            moveTo(centerX + 8f, centerY + 5f)
            lineTo(centerX + 15f, centerY + 15f)
            lineTo(centerX + 8f, centerY + 10f)
            close()
        }

        iconPaint.color = Color.rgb(255, 180, 150)
        canvas.drawPath(leftFin, iconPaint)
        canvas.drawPath(rightFin, iconPaint)

        // Rocket window
        iconPaint.color = Color.rgb(100, 200, 255)
        canvas.drawCircle(centerX, centerY - 5f, 5f, iconPaint)

        // Flame
        val flamePath = Path().apply {
            moveTo(centerX - 6f, centerY + 15f)
            lineTo(centerX, centerY + 25f)
            lineTo(centerX + 6f, centerY + 15f)
        }
        iconPaint.color = Color.rgb(255, 150, 0)
        canvas.drawPath(flamePath, iconPaint)
    }

    private fun drawLightningIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        val lightningPath = Path().apply {
            moveTo(centerX + 5f, centerY - 25f)
            lineTo(centerX - 8f, centerY - 2f)
            lineTo(centerX + 2f, centerY - 2f)
            lineTo(centerX - 5f, centerY + 25f)
            lineTo(centerX + 8f, centerY + 2f)
            lineTo(centerX - 2f, centerY + 2f)
            close()
        }

        iconPaint.color = Color.WHITE
        canvas.drawPath(lightningPath, iconPaint)

        iconStrokePaint.strokeWidth = 2f
        iconStrokePaint.color = Color.rgb(255, 215, 0)
        canvas.drawPath(lightningPath, iconStrokePaint)
    }

    private fun drawParachuteIcon(canvas: Canvas, centerX: Float, centerY: Float) {
        // Parachute canopy - semi-circle with segments
        val canopyPath = Path().apply {
            addArc(centerX - 20f, centerY - 22f, centerX + 20f, centerY + 8f, 180f, 180f)
        }

        iconPaint.style = Paint.Style.FILL
        iconPaint.color = Color.WHITE
        canvas.drawPath(canopyPath, iconPaint)

        // Parachute segments (lines)
        iconStrokePaint.strokeWidth = 2f
        iconStrokePaint.color = Color.rgb(135, 206, 250)
        canvas.drawLine(centerX - 13f, centerY - 15f, centerX - 13f, centerY + 5f, iconStrokePaint)
        canvas.drawLine(centerX, centerY - 20f, centerX, centerY + 5f, iconStrokePaint)
        canvas.drawLine(centerX + 13f, centerY - 15f, centerX + 13f, centerY + 5f, iconStrokePaint)

        // Strings
        iconStrokePaint.strokeWidth = 1.5f
        canvas.drawLine(centerX - 18f, centerY + 5f, centerX - 3f, centerY + 18f, iconStrokePaint)
        canvas.drawLine(centerX + 18f, centerY + 5f, centerX + 3f, centerY + 18f, iconStrokePaint)

        // Person/capsule
        iconPaint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY + 20f, 5f, iconPaint)
        canvas.drawRoundRect(centerX - 4f, centerY + 20f, centerX + 4f, centerY + 28f, 3f, 3f, iconPaint)
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