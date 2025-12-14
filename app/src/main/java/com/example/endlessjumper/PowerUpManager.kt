package com.example.endlessjumper

class PowerUpManager {

    // Active power-up states
    var hasShield = false
        private set
    private var shieldEndTime = 0L

    var superJumpActive = false
        private set
    private var superJumpEndTime = 0L

    var speedBoostActive = false
        private set
    private var speedBoostEndTime = 0L

    var slowFallActive = false
        private set
    private var slowFallEndTime = 0L

    // Power-up durations (in milliseconds)
    private val SHIELD_DURATION = 15000L        // 15 seconds
    private val SUPER_JUMP_DURATION = 5000L     // 5 seconds
    private val SPEED_BOOST_DURATION = 5000L    // 5 seconds
    private val SLOW_FALL_DURATION = 7000L      // 7 seconds

    fun activatePowerUp(type: PowerUpType) {
        val currentTime = System.currentTimeMillis()

        when (type) {
            PowerUpType.SHIELD -> {
                hasShield = true
                shieldEndTime = currentTime + SHIELD_DURATION
            }
            PowerUpType.SUPER_JUMP -> {
                superJumpActive = true
                superJumpEndTime = currentTime + SUPER_JUMP_DURATION
            }
            PowerUpType.SPEED_BOOST -> {
                speedBoostActive = true
                speedBoostEndTime = currentTime + SPEED_BOOST_DURATION
            }
            PowerUpType.SLOW_FALL -> {
                slowFallActive = true
                slowFallEndTime = currentTime + SLOW_FALL_DURATION
            }
        }
    }

    fun update() {
        val currentTime = System.currentTimeMillis()

        // Check if timed power-ups have expired
        if (hasShield && currentTime >= shieldEndTime) {
            hasShield = false
        }

        if (superJumpActive && currentTime >= superJumpEndTime) {
            superJumpActive = false
        }

        if (speedBoostActive && currentTime >= speedBoostEndTime) {
            speedBoostActive = false
        }

        if (slowFallActive && currentTime >= slowFallEndTime) {
            slowFallActive = false
        }
    }

    // Use shield (for damage protection)
    fun useShield(): Boolean {
        return if (hasShield) {
            hasShield = false
            true // Shield was used successfully
        } else {
            false // No shield available
        }
    }

    // Damage the player (will be called by enemies later)
    fun takeDamage(): Boolean {
        return if (hasShield) {
            // Shield absorbs the hit
            hasShield = false
            true // Damage was absorbed
        } else {
            // No shield - player takes damage
            false // Damage not absorbed
        }
    }

    fun reset() {
        hasShield = false
        superJumpActive = false
        speedBoostActive = false
        slowFallActive = false
    }

    // Get remaining time for active power-ups (in seconds)
    fun getShieldTimeLeft(): Int {
        return if (hasShield) {
            ((shieldEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
        } else 0
    }

    fun getSuperJumpTimeLeft(): Int {
        return if (superJumpActive) {
            ((superJumpEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
        } else 0
    }

    fun getSpeedBoostTimeLeft(): Int {
        return if (speedBoostActive) {
            ((speedBoostEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
        } else 0
    }

    fun getSlowFallTimeLeft(): Int {
        return if (slowFallActive) {
            ((slowFallEndTime - System.currentTimeMillis()) / 1000).toInt().coerceAtLeast(0)
        } else 0
    }
}