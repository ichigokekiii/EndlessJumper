package com.example.endlessjumper

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val TAG = "FirebaseManager"

    // Callback interfaces
    interface AuthCallback {
        fun onSuccess(userId: String)
        fun onFailure(error: String)
    }

    interface HighscoreCallback {
        fun onSuccess(highscore: Int)
        fun onFailure(error: String)
    }

    interface LeaderboardCallback {
        fun onSuccess(leaderboard: List<LeaderboardEntry>)
        fun onFailure(error: String)
    }

    interface SaveCallback {
        fun onSuccess()
        fun onFailure(error: String)
    }

    interface PlayerNameCallback {
        fun onSuccess(name: String)
        fun onFailure(error: String)
    }

    // Data class for leaderboard entries
    data class LeaderboardEntry(
        val userId: String = "",
        val username: String = "Anonymous",
        val score: Int = 0,
        val timestamp: Long = 0
    )

    // Sign in anonymously
    fun signInAnonymously(callback: AuthCallback) {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                Log.d(TAG, "Anonymous sign-in successful: $userId")
                callback.onSuccess(userId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Anonymous sign-in failed", e)
                callback.onFailure(e.message ?: "Authentication failed")
            }
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Check if user is signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    // Save player name
    fun savePlayerName(name: String, callback: SaveCallback) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback.onFailure("User not signed in")
            return
        }

        db.collection("highscores")
            .document(userId)
            .update("username", name)
            .addOnSuccessListener {
                Log.d(TAG, "Player name saved: $name")
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                // If document doesn't exist yet, create it with the name
                val userData = hashMapOf(
                    "userId" to userId,
                    "username" to name,
                    "score" to 0,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("highscores")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        Log.d(TAG, "Player name saved (new document): $name")
                        callback.onSuccess()
                    }
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Failed to save player name", error)
                        callback.onFailure(error.message ?: "Failed to save name")
                    }
            }
    }

    // Load player name
    fun loadPlayerName(callback: PlayerNameCallback) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback.onFailure("User not signed in")
            return
        }

        db.collection("highscores")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("username") ?: "Anonymous"
                    Log.d(TAG, "Player name loaded: $name")
                    callback.onSuccess(name)
                } else {
                    Log.d(TAG, "No player name found")
                    callback.onSuccess("Anonymous")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load player name", e)
                callback.onFailure(e.message ?: "Failed to load name")
            }
    }

    // Save highscore to Firestore (with current username)
    fun saveHighscore(score: Int, callback: ((Boolean) -> Unit)? = null) {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Cannot save highscore: User not signed in")
            callback?.invoke(false)
            return
        }

        // First, get the current username
        db.collection("highscores")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val currentUsername = if (document.exists()) {
                    document.getString("username") ?: "Anonymous"
                } else {
                    "Anonymous"
                }

                val highscoreData = hashMapOf(
                    "userId" to userId,
                    "username" to currentUsername,
                    "score" to score,
                    "timestamp" to System.currentTimeMillis()
                )

                Log.d(TAG, "Attempting to save highscore: $score for user: $userId")

                db.collection("highscores")
                    .document(userId)
                    .set(highscoreData)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Highscore saved successfully: $score")
                        callback?.invoke(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to save highscore: ${e.message}", e)
                        callback?.invoke(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load username", e)
                callback?.invoke(false)
            }
    }

    // Load user's highscore from Firestore
    fun loadHighscore(callback: HighscoreCallback) {
        val userId = getCurrentUserId()
        if (userId == null) {
            callback.onFailure("User not signed in")
            return
        }

        db.collection("highscores")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val score = document.getLong("score")?.toInt() ?: 0
                    Log.d(TAG, "Highscore loaded: $score")
                    callback.onSuccess(score)
                } else {
                    Log.d(TAG, "No highscore found for user")
                    callback.onSuccess(0)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load highscore", e)
                callback.onFailure(e.message ?: "Failed to load highscore")
            }
    }

    // Get top 10 highscores (leaderboard)
    fun getLeaderboard(callback: LeaderboardCallback) {
        db.collection("highscores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboard = documents.mapNotNull { doc ->
                    try {
                        LeaderboardEntry(
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "Anonymous",
                            score = doc.getLong("score")?.toInt() ?: 0,
                            timestamp = doc.getLong("timestamp") ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing leaderboard entry", e)
                        null
                    }
                }
                Log.d(TAG, "Leaderboard loaded: ${leaderboard.size} entries")
                callback.onSuccess(leaderboard)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to load leaderboard", e)
                callback.onFailure(e.message ?: "Failed to load leaderboard")
            }
    }

    // Update only if new score is higher
    fun updateHighscoreIfHigher(newScore: Int, callback: ((Boolean, Int) -> Unit)? = null) {
        loadHighscore(object : HighscoreCallback {
            override fun onSuccess(currentHighscore: Int) {
                if (newScore > currentHighscore) {
                    saveHighscore(newScore) { success ->
                        callback?.invoke(success, newScore)
                    }
                } else {
                    callback?.invoke(false, currentHighscore)
                }
            }

            override fun onFailure(error: String) {
                // If we can't load, try to save anyway
                saveHighscore(newScore) { success ->
                    callback?.invoke(success, newScore)
                }
            }
        })
    }
}