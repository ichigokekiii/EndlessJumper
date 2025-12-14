package com.example.endlessjumper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context, private val firebaseManager: FirebaseManager) : SurfaceView(context), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    private val paint = Paint()

    // Game state
    private var isPlaying = false
    private var isGameOver = false
    private var screenWidth = 0
    private var screenHeight = 0

    // Title screen state
    private var isInTitleScreen = true
    private var titleAlpha = 255f
    private var tapToStartAlpha = 255
    private var tapAlphaDirection = -5

    // Game objects
    private var player: Player? = null
    private val platforms = mutableListOf<Platform>()

    // Power-ups
    private val powerUps = mutableListOf<PowerUp>()
    private val powerUpManager = PowerUpManager()
    private val powerUpSpawnChance = 0.2f

    // Space background
    private var spaceBackground: SpaceBackground? = null

    // Enemies
    private val enemies = mutableListOf<Enemy>()
    private val enemySpawnChance = 0.1f
    private var enemySpawnCounter = 0

    // Camera/Scrolling
    private var cameraY = 0f
    private var targetCameraY = 0f

    // Scoring
    private var score = 0
    private var highScore = 0
    private val landedPlatforms = mutableSetOf<Platform>()

    // Platform generation
    private val minPlatformSpacing = 150f
    private val maxPlatformSpacing = 180f
    private var lastPlatformY = 0f

    // Touch control
    private var touchX = 0f
    private var isTouching = false

    init {
        holder.addCallback(this)
        isFocusable = true
        setWillNotDraw(false)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        screenWidth = width
        screenHeight = height

        holder.setFormat(android.graphics.PixelFormat.OPAQUE)

        spaceBackground = SpaceBackground(screenWidth, screenHeight)
        loadHighscoreFromFirebase()
        resetGame()
        startGame()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopGame()
    }

    private fun resetGame() {
        isGameOver = false
        isInTitleScreen = true  // Start in title screen
        titleAlpha = 255f  // Full opacity
        tapToStartAlpha = 255
        cameraY = 0f
        targetCameraY = 0f
        score = 0
        landedPlatforms.clear()
        powerUps.clear()
        powerUpManager.reset()
        enemies.clear()
        enemySpawnCounter = 0

        generateInitialPlatforms()

        // Create player AFTER platforms exist
        player = Player(screenWidth, screenHeight)

        // Position player HIGHER so they land on the platform instead of falling through
        val startPlatform = platforms.firstOrNull { it.y >= screenHeight - 200f }
        if (startPlatform != null) {
            player?.x = startPlatform.x + startPlatform.width / 2 - 30f
            player?.y = startPlatform.y - 250f  // HIGHER - 250 pixels above platform!
            player?.velocityY = 0f
        }
    }

    private fun restartGame() {
        isGameOver = false
        isInTitleScreen = false  // NO title screen on restart!
        titleAlpha = 0f  // No title
        cameraY = 0f
        targetCameraY = 0f
        score = 0
        landedPlatforms.clear()
        powerUps.clear()
        powerUpManager.reset()
        enemies.clear()
        enemySpawnCounter = 0

        generateInitialPlatforms()

        // Create player AFTER platforms exist
        player = Player(screenWidth, screenHeight)

        // Position player HIGHER so they land on the platform instead of falling through
        val startPlatform = platforms.firstOrNull { it.y >= screenHeight - 200f }
        if (startPlatform != null) {
            player?.x = startPlatform.x + startPlatform.width / 2 - 30f
            player?.y = startPlatform.y - 250f  // HIGHER - 250 pixels above platform!
            player?.velocityY = 0f
        }
    }

    private fun generateInitialPlatforms() {
        platforms.clear()

        val startPlatform = Platform(
            x = screenWidth / 2f - 100f,
            y = screenHeight - 150f,
            width = 200f
        )
        platforms.add(startPlatform)
        lastPlatformY = startPlatform.y

        while (lastPlatformY > -1000) {
            generateNewPlatform()
        }
    }

    private fun generateNewPlatform() {
        val platformWidth = Random.nextFloat() * 80 + 160
        val maxX = screenWidth - platformWidth
        val randomX = Random.nextFloat() * maxX
        val spacing = Random.nextFloat() * (maxPlatformSpacing - minPlatformSpacing) + minPlatformSpacing
        val newY = lastPlatformY - spacing

        val newPlatform = Platform(
            x = randomX,
            y = newY,
            width = platformWidth
        )

        platforms.add(newPlatform)
        lastPlatformY = newY

        if (Random.nextFloat() < powerUpSpawnChance) {
            val powerUpType = PowerUpType.values().random()
            val powerUpX = randomX + platformWidth / 2 - 40f
            val powerUpY = newY - 90f
            powerUps.add(PowerUp(powerUpX, powerUpY, powerUpType))
        }

        // Spawn enemy every few platforms
        enemySpawnCounter++
        if (enemySpawnCounter >= 3 && Random.nextFloat() < enemySpawnChance) {
            try {
                val enemyType = EnemyType.values().random()
                val enemySize = 70f
                val enemyX = Random.nextFloat() * (screenWidth - enemySize)
                val enemyY = newY - 200f

                enemies.add(Enemy(enemyX, enemyY, enemyType))
                enemySpawnCounter = 0
            } catch (e: Exception) {
                Log.e("GameView", "Error spawning enemy: ${e.message}")
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Title screen tap to start
        if (isInTitleScreen && event.action == MotionEvent.ACTION_DOWN) {
            performClick()
            isInTitleScreen = false  // Start game!
            return true
        }

        if (isGameOver) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    val touchX = event.x
                    val touchY = event.y

                    performClick()

                    // Button dimensions
                    val buttonWidth = 360f
                    val buttonHeight = 70f
                    val buttonCenterX = screenWidth / 2f

                    // Play Again button
                    val playAgainY = screenHeight / 2f + 80f
                    if (isButtonClicked(touchX, touchY, buttonCenterX - 180f, playAgainY, buttonWidth, buttonHeight)) {
                        restartGame()
                        return true
                    }

                    // Leaderboard button
                    val leaderboardY = playAgainY + 90f
                    if (isButtonClicked(touchX, touchY, buttonCenterX - 180f, leaderboardY, buttonWidth, buttonHeight)) {
                        LeaderboardDialog(context, firebaseManager).show()
                        return true
                    }

                    // Exit button
                    val exitY = leaderboardY + 90f
                    if (isButtonClicked(touchX, touchY, buttonCenterX - 180f, exitY, buttonWidth, buttonHeight)) {
                        (context as? android.app.Activity)?.finish()
                        return true
                    }
                }
            }
            return true
        }

        player?.let { p ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.x
                    isTouching = true
                    performClick()
                    val playerCenterX = p.getCenterX()
                    if (touchX < playerCenterX - 50) {
                        p.moveLeft()
                    } else if (touchX > playerCenterX + 50) {
                        p.moveRight()
                    } else {
                        p.stopMoving()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    touchX = event.x
                    isTouching = true
                    val playerCenterX = p.getCenterX()
                    if (touchX < playerCenterX - 50) {
                        p.moveLeft()
                    } else if (touchX > playerCenterX + 50) {
                        p.moveRight()
                    } else {
                        p.stopMoving()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    isTouching = false
                    p.stopMoving()
                }
            }
        }
        return true
    }

    private fun isButtonClicked(touchX: Float, touchY: Float, buttonX: Float, buttonY: Float, buttonWidth: Float, buttonHeight: Float): Boolean {
        return touchX >= buttonX && touchX <= buttonX + buttonWidth &&
                touchY >= buttonY && touchY <= buttonY + buttonHeight
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun startGame() {
        isPlaying = true
        gameThread = GameThread()
        gameThread?.start()
    }

    private fun stopGame() {
        isPlaying = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun pause() {
        stopGame()
    }

    fun resume() {
        startGame()
    }

    private fun update() {
        // Title screen - just animate background
        if (isInTitleScreen) {
            spaceBackground?.update()

            // DON'T fade title yet - only blink "TAP TO START"
            tapToStartAlpha += tapAlphaDirection
            if (tapToStartAlpha <= 100 || tapToStartAlpha >= 255) {
                tapAlphaDirection *= -1
            }

            return  // Don't run game logic yet
        }

        // Fade out title AFTER user taps (when not in title screen anymore)
        if (titleAlpha > 0) {
            titleAlpha -= 8f
            if (titleAlpha < 0) titleAlpha = 0f
        }

        if (isGameOver) return

        spaceBackground?.update()

        player?.let { p ->
            p.update()
            powerUpManager.update()
            p.applyPowerUps(powerUpManager)

            val playerScreenY = p.y - cameraY
            if (playerScreenY < screenHeight / 2) {
                targetCameraY = p.y - screenHeight / 2
            }

            val smoothSpeed = 0.15f
            cameraY += (targetCameraY - cameraY) * smoothSpeed

            for (platform in platforms) {
                if (platform.isPlayerLanding(p)) {
                    p.y = platform.getTop() - p.height
                    p.jump()

                    if (!landedPlatforms.contains(platform)) {
                        landedPlatforms.add(platform)
                        score++
                        if (score > highScore) {
                            highScore = score
                        }
                    }
                    break
                }
            }

            for (powerUp in powerUps) {
                if (powerUp.checkCollision(p)) {
                    powerUpManager.activatePowerUp(powerUp.type)
                    Log.d("GameView", "Collected power-up: ${powerUp.type}")
                }
            }

            powerUps.removeAll { it.isCollected() }

            // Update and check enemies
            try {
                val enemiesToUpdate = enemies.toList()
                for (enemy in enemiesToUpdate) {
                    enemy.update(screenWidth)

                    if (enemy.checkCollision(p)) {
                        if (powerUpManager.takeDamage()) {
                            enemy.destroy()
                            Log.d("GameView", "Shield absorbed damage!")
                        } else {
                            isGameOver = true
                            if (score > highScore) highScore = score
                            firebaseManager.updateHighscoreIfHigher(score) { success, savedScore ->
                                if (success) {
                                    Log.d("GameView", "New highscore saved to Firebase: $savedScore")
                                } else {
                                    Log.d("GameView", "Score $score not higher than existing highscore $savedScore")
                                }
                            }
                            return
                        }
                    }
                }

                enemies.removeAll { it.isDestroyed() || it.y > cameraY + screenHeight + 100 }
            } catch (e: Exception) {
                Log.e("GameView", "Enemy update error: ${e.message}")
            }

            val platformsToRemove = platforms.filter { it.y > cameraY + screenHeight + 100 }
            platforms.removeAll(platformsToRemove)
            landedPlatforms.removeAll(platformsToRemove.toSet())
            powerUps.removeAll { it.y > cameraY + screenHeight + 100 }

            while (lastPlatformY > cameraY - 500) {
                generateNewPlatform()
            }

            if (p.y - cameraY > screenHeight && !isGameOver) {
                if (powerUpManager.useShield()) {
                    p.y = cameraY + screenHeight / 2
                    p.velocityY = 0f
                    Log.d("GameView", "Shield saved the player!")
                } else {
                    isGameOver = true
                    if (score > highScore) {
                        highScore = score
                    }
                    firebaseManager.updateHighscoreIfHigher(score) { success, savedScore ->
                        if (success) {
                            Log.d("GameView", "New highscore saved to Firebase: $savedScore")
                        } else {
                            Log.d("GameView", "Score $score not higher than existing highscore $savedScore")
                        }
                    }
                }
            }
        }
    }

    private fun draw() {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas()
            if (canvas == null) return

            // FORCE CLEAR - Draw solid black over everything first
            canvas.drawColor(Color.BLACK)

            // Draw space background fresh
            spaceBackground?.draw(canvas)

            // Draw platforms at their camera-adjusted positions
            for (platform in platforms) {
                val platformScreenY = platform.y - cameraY
                if (platformScreenY > -100 && platformScreenY < screenHeight + 100) {
                    canvas.save()
                    canvas.translate(0f, -cameraY)
                    platform.draw(canvas)
                    canvas.restore()
                }
            }

            // Draw power-ups at their camera-adjusted positions
            for (powerUp in powerUps) {
                val powerUpScreenY = powerUp.y - cameraY
                if (powerUpScreenY > -100 && powerUpScreenY < screenHeight + 100) {
                    canvas.save()
                    canvas.translate(0f, -cameraY)
                    powerUp.draw(canvas)
                    canvas.restore()
                }
            }

            // Draw enemies safely
            try {
                val enemiesToDraw = enemies.toList()
                for (enemy in enemiesToDraw) {
                    if (enemy.y - cameraY in -100f..(screenHeight + 100f)) {
                        canvas.save()
                        canvas.translate(0f, -cameraY)
                        enemy.draw(canvas)
                        canvas.restore()
                    }
                }
            } catch (e: Exception) {
                Log.e("GameView", "Enemy draw error: ${e.message}")
            }

            // Draw player at camera-adjusted position
            player?.let { p ->
                canvas.save()
                canvas.translate(0f, -cameraY)
                p.draw(canvas, powerUpManager)
                canvas.restore()
            }

            // Draw UI on top (no camera offset)
            drawUI(canvas)

        } catch (e: Exception) {
            Log.e("GameView", "Draw error: ${e.message}")
        } finally {
            canvas?.let {
                try {
                    holder.unlockCanvasAndPost(it)
                } catch (e: Exception) {
                    Log.e("GameView", "Canvas post error: ${e.message}")
                }
            }
        }
    }

    private fun loadHighscoreFromFirebase() {
        firebaseManager.loadHighscore(object : FirebaseManager.HighscoreCallback {
            override fun onSuccess(loadedHighscore: Int) {
                highScore = loadedHighscore
                Log.d("GameView", "Highscore loaded: $highScore")
            }

            override fun onFailure(error: String) {
                Log.e("GameView", "Failed to load highscore: $error")
            }
        })
    }

    private fun drawUI(canvas: Canvas) {
        // TITLE SCREEN
        if (isInTitleScreen || titleAlpha > 0) {
            val alpha = titleAlpha.toInt().coerceIn(0, 255)

            if (alpha > 0) {
                // Title
                paint.color = Color.argb(alpha, 255, 255, 255)
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                paint.isFakeBoldText = true
                paint.setShadowLayer(10f, 0f, 0f, Color.argb(alpha, 50, 150, 200))
                canvas.drawText("SPACE", screenWidth / 2f, screenHeight / 3f - 50f, paint)
                canvas.drawText("JUMPER", screenWidth / 2f, screenHeight / 3f + 40f, paint)
                paint.clearShadowLayer()

                // Subtitle
                paint.textSize = 30f
                paint.color = Color.argb(alpha, 150, 150, 150)
                paint.isFakeBoldText = false
                canvas.drawText("Endless Adventure", screenWidth / 2f, screenHeight / 3f + 100f, paint)

                // TAP TO START (blinking)
                val tapAlphaFinal = ((tapToStartAlpha / 255f) * (alpha / 255f) * 255).toInt()
                paint.color = Color.argb(tapAlphaFinal, 255, 255, 255)
                paint.textSize = 40f
                paint.isFakeBoldText = true
                canvas.drawText("TAP TO START", screenWidth / 2f, screenHeight - 80f, paint)
            }

            if (isInTitleScreen) return  // Don't draw game UI yet
        }

        // GAME UI
        paint.color = Color.WHITE
        paint.textSize = 70f
        paint.textAlign = Paint.Align.LEFT
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(5f, 2f, 2f, Color.BLACK)

        canvas.drawText("Score: $score", 40f, 80f, paint)

        paint.textSize = 50f
        canvas.drawText("Highscore: $highScore", 40f, 145f, paint)

        paint.clearShadowLayer()

        if (isGameOver) {
            paint.color = Color.argb(200, 0, 0, 0)
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)

            paint.color = Color.WHITE
            paint.textSize = 100f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 250f, paint)

            paint.textSize = 60f
            canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f - 100f, paint)
            canvas.drawText("Highscore: $highScore", screenWidth / 2f, screenHeight / 2f - 20f, paint)

            // Uniform button color
            val buttonColor = Color.rgb(50, 150, 200)

            // PLAY AGAIN BUTTON
            val playAgainButtonY = screenHeight / 2f + 80f
            drawButton(
                canvas,
                "PLAY AGAIN",
                screenWidth / 2f - 180f,
                playAgainButtonY,
                360f,
                70f,
                buttonColor
            )

            // LEADERBOARD BUTTON
            val leaderboardButtonY = playAgainButtonY + 90f
            drawButton(
                canvas,
                "LEADERBOARD",
                screenWidth / 2f - 180f,
                leaderboardButtonY,
                360f,
                70f,
                buttonColor
            )

            // EXIT BUTTON
            val exitButtonY = leaderboardButtonY + 90f
            drawButton(
                canvas,
                "EXIT",
                screenWidth / 2f - 180f,
                exitButtonY,
                360f,
                70f,
                buttonColor
            )

        } else {
            drawActivePowerUps(canvas)

            paint.color = Color.WHITE
            paint.textSize = 35f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Drag to move", screenWidth / 2f, screenHeight - 50f, paint)
        }
    }

    private fun drawButton(canvas: Canvas, text: String, x: Float, y: Float, width: Float, height: Float, color: Int) {
        // Button background
        val buttonPaint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(x, y, x + width, y + height, 15f, 15f, buttonPaint)

        // Button border
        val borderPaint = Paint().apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(x, y, x + width, y + height, 15f, 15f, borderPaint)

        // Button text
        val textPaint = Paint().apply {
            this.color = Color.WHITE
            textSize = 45f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(3f, 2f, 2f, Color.BLACK)
            isAntiAlias = true
        }
        canvas.drawText(text, x + width / 2, y + height / 2 + 15f, textPaint)
    }

    private fun drawActivePowerUps(canvas: Canvas) {
        var y = 220f

        if (powerUpManager.hasShield) {
            val timeLeft = powerUpManager.getShieldTimeLeft()
            paint.color = Color.rgb(100, 149, 237)
            paint.textSize = 45f
            paint.textAlign = Paint.Align.LEFT
            paint.setShadowLayer(3f, 1f, 1f, Color.BLACK)
            canvas.drawText("🛡️ Shield: ${timeLeft}s", 40f, y, paint)
            paint.clearShadowLayer()
            y += 55f
        }

        if (powerUpManager.superJumpActive) {
            val timeLeft = powerUpManager.getSuperJumpTimeLeft()
            paint.color = Color.rgb(255, 99, 71)
            paint.textSize = 45f
            paint.textAlign = Paint.Align.LEFT
            paint.setShadowLayer(3f, 1f, 1f, Color.BLACK)
            canvas.drawText("🚀 Jump: ${timeLeft}s", 40f, y, paint)
            paint.clearShadowLayer()
            y += 55f
        }

        if (powerUpManager.speedBoostActive) {
            val timeLeft = powerUpManager.getSpeedBoostTimeLeft()
            paint.color = Color.rgb(255, 215, 0)
            paint.textSize = 45f
            paint.textAlign = Paint.Align.LEFT
            paint.setShadowLayer(3f, 1f, 1f, Color.BLACK)
            canvas.drawText("⚡ Speed: ${timeLeft}s", 40f, y, paint)
            paint.clearShadowLayer()
            y += 55f
        }

        if (powerUpManager.slowFallActive) {
            val timeLeft = powerUpManager.getSlowFallTimeLeft()
            paint.color = Color.rgb(135, 206, 250)
            paint.textSize = 45f
            paint.textAlign = Paint.Align.LEFT
            paint.setShadowLayer(3f, 1f, 1f, Color.BLACK)
            canvas.drawText("🪂 Float: ${timeLeft}s", 40f, y, paint)
            paint.clearShadowLayer()
        }
    }

    private inner class GameThread : Thread() {
        private val targetFPS = 60
        private val targetTime = 1000 / targetFPS

        override fun run() {
            while (isPlaying) {
                val startTime = System.currentTimeMillis()

                update()
                draw()

                val timeElapsed = System.currentTimeMillis() - startTime
                val sleepTime = targetTime - timeElapsed

                if (sleepTime > 0) {
                    try {
                        sleep(sleepTime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}