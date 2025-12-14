package com.example.endlessjumper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.random.Random

enum class EnemyType {
    ASTEROID,
    ALIEN_SHIP,
    SPACE_MINE
}

class Enemy(
    var x: Float,
    var y: Float,
    val type: EnemyType = EnemyType.values().random()
) {
    val size = when (type) {
        EnemyType.ASTEROID -> 90f
        EnemyType.ALIEN_SHIP -> 130f
        EnemyType.SPACE_MINE -> Random.nextFloat() * 60f + 60f
    }

    private var destroyed = false
    private var rotation = 0f

    private var fallSpeed = when (type) {
        EnemyType.ASTEROID -> Random.nextFloat() * 4f + 2f
        else -> 0f
    }

    private var horizontalSpeed = when (type) {
        EnemyType.ALIEN_SHIP -> Random.nextFloat() * 2f + 2.5f
        EnemyType.ASTEROID -> (Random.nextFloat() - 0.5f) * 1.5f
        EnemyType.SPACE_MINE -> 0f
    }
    private var moveDirection = if (Random.nextBoolean()) 1f else -1f

    private val asteroidShape = Path()
    private val rockPoints = List(8) {
        val angle = (it * 45f) * Math.PI / 180f
        val radius = size / 2 * (0.6f + Random.nextFloat() * 0.4f)
        Pair(
            (Math.cos(angle) * radius).toFloat(),
            (Math.sin(angle) * radius).toFloat()
        )
    }

    init {
        if (type == EnemyType.ASTEROID) {
            asteroidShape.moveTo(rockPoints[0].first, rockPoints[0].second)
            for (i in 1 until rockPoints.size) {
                asteroidShape.lineTo(rockPoints[i].first, rockPoints[i].second)
            }
            asteroidShape.close()
        }
    }

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    fun update(screenWidth: Int) {
        if (type == EnemyType.ASTEROID) {
            y += fallSpeed
        }

        when (type) {
            EnemyType.ASTEROID -> rotation += 4f
            EnemyType.SPACE_MINE -> rotation += 1.5f
            EnemyType.ALIEN_SHIP -> {}
        }

        when (type) {
            EnemyType.ALIEN_SHIP -> {
                x += horizontalSpeed * moveDirection

                val leftEdge = 10f
                val rightEdge = screenWidth - size - 10f

                if (x <= leftEdge) {
                    x = leftEdge
                    moveDirection = 1f
                } else if (x >= rightEdge) {
                    x = rightEdge
                    moveDirection = -1f
                }
            }
            EnemyType.ASTEROID -> {
                x += horizontalSpeed
            }
            EnemyType.SPACE_MINE -> {}
        }
    }

    fun draw(canvas: Canvas) {
        if (destroyed) return

        canvas.save()
        canvas.translate(x + size / 2, y + size / 2)

        when (type) {
            EnemyType.ASTEROID -> {
                canvas.rotate(rotation)
                drawAsteroid(canvas)
            }
            EnemyType.ALIEN_SHIP -> drawAlienShip(canvas)
            EnemyType.SPACE_MINE -> {
                canvas.rotate(rotation)
                drawSpaceMine(canvas)
            }
        }

        canvas.restore()
    }

    private fun drawAsteroid(canvas: Canvas) {
        // Different color from platforms - darker, more brown/orange tint
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(110, 85, 60)  // Darker brown with orange tint
        canvas.drawPath(asteroidShape, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(140, 110, 80)  // Lighter brown outline
        paint.strokeWidth = 4f
        canvas.drawPath(asteroidShape, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(80, 60, 45)  // Dark brown craters
        canvas.drawCircle(-15f, -15f, 10f, paint)
        canvas.drawCircle(12f, 10f, 8f, paint)
        canvas.drawCircle(-10f, 18f, 9f, paint)
        canvas.drawCircle(15f, -8f, 7f, paint)

        paint.color = Color.rgb(65, 50, 35)  // Very dark brown
        canvas.drawCircle(-8f, -10f, 5f, paint)
        canvas.drawCircle(10f, 15f, 5f, paint)
    }

    private fun drawAlienShip(canvas: Canvas) {
        // Transparent green glow
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(60, 100, 255, 150)
        canvas.drawCircle(0f, 0f, size / 2 + 15f, paint)

        // Main body
        paint.color = Color.rgb(100, 255, 150)
        canvas.drawOval(-size / 2, -size / 3, size / 2, size / 3, paint)

        // Dome window
        paint.color = Color.rgb(50, 150, 200)
        canvas.drawCircle(0f, -12f, size / 3, paint)

        // Window reflection
        paint.color = Color.argb(180, 150, 220, 255)
        canvas.drawCircle(-12f, -18f, size / 6, paint)

        // Body outline
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(80, 200, 120)
        paint.strokeWidth = 4f
        canvas.drawOval(-size / 2, -size / 3, size / 2, size / 3, paint)

        // Eyes
        paint.style = Paint.Style.FILL

        paint.color = Color.argb(150, 255, 100, 100)
        canvas.drawCircle(-18f, -12f, 12f, paint)
        canvas.drawCircle(18f, -12f, 12f, paint)

        paint.color = Color.rgb(255, 50, 50)
        canvas.drawCircle(-18f, -12f, 8f, paint)
        canvas.drawCircle(18f, -12f, 8f, paint)

        paint.color = Color.rgb(180, 0, 0)
        canvas.drawCircle(-18f, -12f, 4f, paint)
        canvas.drawCircle(18f, -12f, 4f, paint)

        // Bottom lights
        val lightSize = size / 18f

        paint.color = Color.rgb(0, 255, 220)
        canvas.drawCircle(-size / 4, size / 8, lightSize, paint)
        canvas.drawCircle(0f, size / 6, lightSize, paint)
        canvas.drawCircle(size / 4, size / 8, lightSize, paint)

        paint.color = Color.argb(80, 0, 255, 220)
        canvas.drawCircle(-size / 4, size / 8, lightSize * 2f, paint)
        canvas.drawCircle(0f, size / 6, lightSize * 2f, paint)
        canvas.drawCircle(size / 4, size / 8, lightSize * 2f, paint)
    }

    private fun drawSpaceMine(canvas: Canvas) {
        // LESS TRANSPARENT darkish red portal
        val pulse = 1f + Math.sin(System.currentTimeMillis() / 300.0).toFloat() * 0.25f
        val alphaPulse = (Math.sin(System.currentTimeMillis() / 500.0) * 0.4 + 0.6).toFloat()  // 0.6 to 1.0 - LESS transparent!
        val rotation = (System.currentTimeMillis() / 50.0).toFloat() % 360f

        canvas.save()
        canvas.rotate(rotation)

        // Outermost portal energy (more visible)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((100 * alphaPulse).toInt(), 180, 0, 0)  // Dark red, MORE opaque
        canvas.drawCircle(0f, 0f, size / 2 * pulse + size / 5, paint)

        // Second layer (crimson)
        paint.color = Color.argb((140 * alphaPulse).toInt(), 200, 50, 50)  // MORE opaque
        canvas.drawCircle(0f, 0f, size / 2 * pulse + size / 8, paint)

        // Third layer (red energy)
        paint.color = Color.argb((180 * alphaPulse).toInt(), 255, 80, 80)  // MORE opaque
        canvas.drawCircle(0f, 0f, size / 2 * pulse, paint)

        canvas.restore()

        // Center portal vortex (darker red)
        paint.color = Color.argb((200 * alphaPulse).toInt(), 200, 30, 30)
        canvas.drawCircle(0f, 0f, size / 3 * pulse, paint)

        // Inner vortex (brighter red center)
        paint.color = Color.argb((220 * alphaPulse).toInt(), 255, 100, 100)
        canvas.drawCircle(0f, 0f, size / 5 * pulse, paint)

        // Portal "eye" center (orange-red)
        paint.color = Color.argb((240 * alphaPulse).toInt(), 255, 150, 100)
        canvas.drawCircle(0f, 0f, size / 10 * pulse, paint)

        // Swirling energy rings (more visible)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size / 20f

        canvas.save()
        canvas.rotate(-rotation * 1.5f)

        // Outer ring
        paint.color = Color.argb((120 * alphaPulse).toInt(), 200, 80, 80)  // MORE opaque
        canvas.drawCircle(0f, 0f, size / 2.5f * pulse, paint)

        // Middle ring
        paint.color = Color.argb((150 * alphaPulse).toInt(), 255, 100, 100)  // MORE opaque
        canvas.drawCircle(0f, 0f, size / 3.5f * pulse, paint)

        canvas.restore()

        // Portal sparks/particles (red/orange)
        paint.style = Paint.Style.FILL
        for (i in 0 until 6) {
            val angle = (i * 60f + rotation) * Math.PI / 180f
            val distance = size / 3 * pulse
            val sparkX = (Math.cos(angle) * distance).toFloat()
            val sparkY = (Math.sin(angle) * distance).toFloat()

            paint.color = Color.argb((180 * alphaPulse).toInt(), 255, 120, 80)  // MORE opaque
            canvas.drawCircle(sparkX, sparkY, size / 25f, paint)
        }
    }

    fun checkCollision(player: Player): Boolean {
        if (destroyed) return false

        val playerCenterX = player.getCenterX()
        val playerCenterY = player.getCenterY()
        val enemyCenterX = x + size / 2
        val enemyCenterY = y + size / 2

        val dx = playerCenterX - enemyCenterX
        val dy = playerCenterY - enemyCenterY
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        return distance < (size / 2 + player.width / 3)
    }

    fun destroy() {
        destroyed = true
    }

    fun isDestroyed() = destroyed
}