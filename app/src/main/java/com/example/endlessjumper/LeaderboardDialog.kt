package com.example.endlessjumper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat

class LeaderboardDialog(
    context: Context,
    private val firebaseManager: FirebaseManager
) : Dialog(context) {

    private lateinit var progressBar: ProgressBar
    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var errorText: TextView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        createUI()
        loadLeaderboard()
    }

    private fun createUI() {
        // Main card container
        val mainCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, 0, 0, 0)
            // Add rounded corners
            background = ContextCompat.getDrawable(context, android.R.drawable.dialog_holo_light_frame)
        }

        // Add card params to make it not fullscreen
        val cardParams = LinearLayout.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
            (context.resources.displayMetrics.heightPixels * 0.75).toInt()  // 75% of screen height
        )
        mainCard.layoutParams = cardParams

        // Header
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            setPadding(40, 50, 40, 35)
        }

        val titleText = TextView(context).apply {
            text = "🏆 LEADERBOARD 🏆"
            textSize = 32f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        headerLayout.addView(titleText)

        val subtitleText = TextView(context).apply {
            text = "Top 10 Players"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        headerLayout.addView(subtitleText)

        mainCard.addView(headerLayout)

        // Content area
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 25, 25, 25)
        }

        // Progress bar
        progressBar = ProgressBar(context).apply {
            visibility = View.VISIBLE
        }
        val progressParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(0, 80, 0, 80)
        }
        contentLayout.addView(progressBar, progressParams)

        // Error text
        errorText = TextView(context).apply {
            text = "Failed to load leaderboard"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(30, 30, 30, 30)
        }
        contentLayout.addView(errorText)

        // ScrollView for entries
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        leaderboardContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(leaderboardContainer)
        contentLayout.addView(scrollView)

        mainCard.addView(contentLayout)

        // Close button
        val closeButton = TextView(context).apply {
            text = "✕ CLOSE"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 35)
            setOnClickListener {
                dismiss()
            }
        }
        mainCard.addView(closeButton)

        setContentView(mainCard)
    }

    private fun loadLeaderboard() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        leaderboardContainer.removeAllViews()

        firebaseManager.getLeaderboard(object : FirebaseManager.LeaderboardCallback {
            override fun onSuccess(leaderboard: List<FirebaseManager.LeaderboardEntry>) {
                progressBar.visibility = View.GONE

                if (leaderboard.isEmpty()) {
                    showEmptyState()
                } else {
                    displayLeaderboard(leaderboard)
                }
            }

            override fun onFailure(error: String) {
                progressBar.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "Failed to load\nCheck internet connection"
            }
        })
    }

    private fun showEmptyState() {
        val emptyText = TextView(context).apply {
            text = "🎮\n\nNo scores yet!\nBe the first!"
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            gravity = Gravity.CENTER
            setPadding(40, 80, 40, 80)
        }
        leaderboardContainer.addView(emptyText)
    }

    private fun displayLeaderboard(leaderboard: List<FirebaseManager.LeaderboardEntry>) {
        val currentUserId = firebaseManager.getCurrentUserId()

        leaderboard.forEachIndexed { index, entry ->
            val rank = index + 1
            val isCurrentUser = entry.userId == currentUserId

            // Entry row
            val entryLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 22, 20, 22)
                setBackgroundColor(
                    when {
                        isCurrentUser -> Color.parseColor("#E3F2FD") // Light blue
                        rank == 1 -> Color.parseColor("#FFF9C4") // Light gold
                        rank == 2 -> Color.parseColor("#F5F5F5") // Light silver
                        rank == 3 -> Color.parseColor("#FFE0B2") // Light bronze
                        else -> Color.WHITE
                    }
                )
            }

            // Rank
            val rankText = TextView(context).apply {
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                }
                textSize = 26f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(80, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            entryLayout.addView(rankText)

            // Username
            val nameLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(15, 0, 15, 0)
            }

            val usernameText = TextView(context).apply {
                text = entry.username
                textSize = 19f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }
            nameLayout.addView(usernameText)

            if (isCurrentUser) {
                val youBadge = TextView(context).apply {
                    text = "👤 You"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                    setPadding(0, 3, 0, 0)
                }
                nameLayout.addView(youBadge)
            }

            entryLayout.addView(nameLayout)

            // Score
            val scoreText = TextView(context).apply {
                text = entry.score.toString()
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                minWidth = 80
            }
            entryLayout.addView(scoreText)

            leaderboardContainer.addView(entryLayout)

            // Divider
            if (rank < leaderboard.size) {
                val divider = View(context).apply {
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                }
                leaderboardContainer.addView(divider)
            }
        }
    }
}