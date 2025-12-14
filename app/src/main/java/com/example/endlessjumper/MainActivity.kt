package com.example.endlessjumper

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase App
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MainActivity", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization error: ${e.message}")
        }

        auth = FirebaseAuth.getInstance()

        // SIGN OUT any previous user first
        auth.signOut()
        Log.d("MainActivity", "Signed out previous user")

        // Initialize Firebase Manager
        firebaseManager = FirebaseManager()

        // Sign in as NEW anonymous user for this session
        firebaseManager.signInAnonymously(object : FirebaseManager.AuthCallback {
            override fun onSuccess(userId: String) {
                Log.d("MainActivity", "New session started - User ID: $userId")
                initializeGame()
            }

            override fun onFailure(error: String) {
                Log.e("MainActivity", "Sign-in failed: $error")
                // Still initialize game even if sign-in fails
                initializeGame()
            }
        })
    }

    private fun initializeGame() {
        // Create and set the game view with Firebase manager
        gameView = GameView(this, firebaseManager)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        if (::gameView.isInitialized) {
            gameView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::gameView.isInitialized) {
            gameView.resume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Sign out when app is destroyed (user exits)
        auth.signOut()
        Log.d("MainActivity", "Session ended - User signed out")
    }
}