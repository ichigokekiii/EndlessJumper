package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Player(
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    val width = 80f
    val height = 80f

    var x = screenWidth / 2f - width / 2f
    var y = screenHeight / 2f

    var velocityY = 0f
    private var velocityX = 0f

    private val baseGravity = 1.2f
    private val baseJumpForce = -30f
    private val maxFallSpeed = 30f
    private val baseMoveSpeed = 15f
    private val friction = 0.85f

    private var currentGravity = baseGravity
    private var currentJumpForce = baseJumpForce
    private var currentMoveSpeed = baseMoveSpeed

    var isJumping = false

    // Trail effect variables - elongated capsules
    private val trailPositions = mutableListOf<TrailPoint>()
    private val maxTrailLength = 8

    // Squash/stretch animation
    private var squashFactor = 1f
    private var targetSquashFactor = 1f

    data class TrailPoint(val x: Float, val y: Float, val width: Float, val height: Float, var alpha: Int)

    fun update() {
        velocityY += currentGravity

        if (velocityY > maxFallSpeed) {
            velocityY = maxFallSpeed
        }

        y += velocityY
        velocityX *= friction
        x += velocityX

        if (x < 0) {
            x = 0f
            velocityX = 0f
        }
        if (x > screenWidth - width) {
            x = screenWidth - width
            velocityX = 0f
        }

        // Smooth squash/stretch animation
        squashFactor += (targetSquashFactor - squashFactor) * 0.3f
    }

    fun jump() {
        velocityY = currentJumpForce
        isJumping = true

        // Trigger subtle squash animation on jump
        targetSquashFactor = 0.92f

        // Reset back to normal
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            targetSquashFactor = 1f
        }, 100)
    }

    fun moveLeft() {
        velocityX = -currentMoveSpeed
    }

    fun moveRight() {
        velocityX = currentMoveSpeed
    }

    fun stopMoving() {
    }

    fun applyPowerUps(powerUpManager: PowerUpManager) {
        currentGravity = baseGravity
        currentJumpForce = baseJumpForce
        currentMoveSpeed = baseMoveSpeed

        if (powerUpManager.superJumpActive) {
            currentJumpForce = baseJumpForce * 1.4f
        }

        if (powerUpManager.speedBoostActive) {
            currentMoveSpeed = baseMoveSpeed * 1.6f
        }

        if (powerUpManager.slowFallActive) {
            currentGravity = baseGravity * 0.6f
        }
    }

    private fun updateTrail(hasPowerUp: Boolean) {
        if (hasPowerUp && Math.abs(velocityY) > 2f) {
            // Add elongated capsule trail
            val trailWidth = width * 0.85f
            val trailHeight = height * 1.2f

            trailPositions.add(0, TrailPoint(
                x + width / 2,
                y + height / 2,
                trailWidth,
                trailHeight,
                180
            ))

            // Limit trail length
            if (trailPositions.size > maxTrailLength) {
                trailPositions.removeAt(trailPositions.size - 1)
            }

            // Fade out trail gradually
            trailPositions.forEachIndexed { index, point ->
                point.alpha = ((maxTrailLength - index).toFloat() / maxTrailLength * 180).toInt()
            }
        } else {
            // Gradually clear trail
            if (trailPositions.isNotEmpty()) {
                trailPositions.removeAt(trailPositions.size - 1)
            }
        }
    }

    fun draw(canvas: Canvas, powerUpManager: PowerUpManager? = null) {
        // Check if we should show trail (Super Jump or Slow Fall)
        val hasJumpPowerUp = powerUpManager?.superJumpActive == true ||
                powerUpManager?.slowFallActive == true

        // Update trail
        updateTrail(hasJumpPowerUp)

        // Draw trail ONLY when jump/float power-up is active
        if (trailPositions.isNotEmpty()) {
            val trailPaint = Paint().apply {
                style = Paint.Style.FILL
            }

            trailPositions.forEachIndexed { index, point ->
                // Different colors based on power-up
                val baseColor = if (powerUpManager?.superJumpActive == true) {
                    Color.rgb(255, 120, 80)  // Orange/coral for Super Jump
                } else if (powerUpManager?.slowFallActive == true) {
                    Color.rgb(150, 220, 255)  // Light blue for Slow Fall
                } else {
                    Color.rgb(200, 200, 220)  // Neutral gray-blue when fading
                }

                trailPaint.color = Color.argb(
                    point.alpha,
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)
                )

                // Draw elongated rounded rectangle (capsule shape)
                val halfWidth = point.width / 2
                val halfHeight = point.height / 2

                canvas.drawRoundRect(
                    point.x - halfWidth,
                    point.y - halfHeight,
                    point.x + halfWidth,
                    point.y + halfHeight,
                    25f,
                    25f,
                    trailPaint
                )
            }
        }

        // Shield with pulsing effect
        if (powerUpManager?.hasShield == true) {
            val pulsate = 15f + (Math.sin(System.currentTimeMillis() / 200.0) * 3f).toFloat()

            val shieldGlowPaint = Paint().apply {
                color = Color.argb(50, 0, 255, 255)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(x + width / 2, y + height / 2, width / 2 + pulsate + 8f, shieldGlowPaint)

            val shieldPaint = Paint().apply {
                color = Color.argb(120, 0, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(x + width / 2, y + height / 2, width / 2 + pulsate, shieldPaint)
        }

        // Apply subtle squash/stretch ONLY when jumping
        val stretchFactor = if (squashFactor < 0.99f) {
            1f / squashFactor
        } else {
            1f
        }

        // Save canvas for squash/stretch
        canvas.save()

        if (squashFactor != 1f) {
            canvas.translate(x + width / 2, y + height / 2)
            canvas.scale(squashFactor, stretchFactor)
            canvas.translate(-(x + width / 2), -(y + height / 2))
        }

        // Body
        val bodyPaint = Paint().apply {
            color = Color.rgb(240, 240, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x, y, x + width, y + height, 20f, 20f, bodyPaint)

        // Stripe
        val stripePaint = Paint().apply {
            color = Color.rgb(255, 100, 100)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x + 10f, y + height * 0.3f, x + width - 10f, y + height * 0.4f, stripePaint)

        // Visor
        val visorPaint = Paint().apply {
            color = Color.rgb(20, 30, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x + 15f, y + 10f, x + width - 15f, y + height * 0.55f, 15f, 15f, visorPaint)

        // Reflection
        val reflectionPaint = Paint().apply {
            color = Color.argb(100, 150, 200, 255)
            style = Paint.Style.FILL
        }
        canvas.drawOval(x + 20f, y + 15f, x + 45f, y + 30f, reflectionPaint)

        // Eyes - look in direction of movement
        val eyePaint = Paint().apply {
            color = Color.rgb(100, 255, 255)
            style = Paint.Style.FILL
        }

        val pupilOffsetX = when {
            velocityX < -5f -> -1.5f
            velocityX > 5f -> 1.5f
            else -> 0f
        }

        canvas.drawCircle(x + width * 0.35f + pupilOffsetX, y + height * 0.3f, 5f, eyePaint)
        canvas.drawCircle(x + width * 0.65f + pupilOffsetX, y + height * 0.3f, 5f, eyePaint)

        // Restore canvas
        canvas.restore()
    }

    fun getLeft() = x
    fun getRight() = x + width
    fun getTop() = y
    fun getBottom() = y + height
    fun getCenterX() = x + width / 2f
    fun getCenterY() = y + height / 2f
}