package com.example.endlessjumper

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager()

        // Sign in anonymously if not already signed in
        if (!firebaseManager.isUserSignedIn()) {
            firebaseManager.signInAnonymously(object : FirebaseManager.AuthCallback {
                override fun onSuccess(userId: String) {
                    Log.d("MainActivity", "User signed in: $userId")
                    initializeGame()
                }

                override fun onFailure(error: String) {
                    Log.e("MainActivity", "Sign-in failed: $error")
                    // Still initialize game even if sign-in fails
                    initializeGame()
                }
            })
        } else {
            initializeGame()
        }
    }

    private fun initializeGame() {
        // Create and set the game view with Firebase manager
        gameView = GameView(this, firebaseManager)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }
}