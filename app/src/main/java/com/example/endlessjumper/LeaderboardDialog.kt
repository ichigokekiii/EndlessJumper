package com.example.endlessjumper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat

class LeaderboardDialog(
    context: Context,
    private val firebaseManager: FirebaseManager
) : Dialog(context) {

    private lateinit var progressBar: ProgressBar
    private lateinit var leaderboardContainer: LinearLayout
    private lateinit var errorText: TextView
    private lateinit var nameInput: EditText
    private lateinit var saveNameButton: Button

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        createUI()
        loadPlayerName()
        loadLeaderboard()
    }

    private fun createUI() {
        // Main card container with space theme
        val mainCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A1E")) // Dark space blue
            setPadding(0, 0, 0, 0)
        }

        val cardParams = LinearLayout.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        mainCard.layoutParams = cardParams

        // Header with gradient
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A3E")) // Darker blue
            setPadding(40, 50, 40, 35)
        }

        val titleText = TextView(context).apply {
            text = "🏆 LEADERBOARD"
            textSize = 36f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setShadowLayer(10f, 0f, 0f, Color.rgb(50, 150, 200))
        }
        headerLayout.addView(titleText)

        val subtitleText = TextView(context).apply {
            text = "Top 10 Space Jumpers"
            textSize = 16f
            setTextColor(Color.rgb(150, 200, 255))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }
        headerLayout.addView(subtitleText)

        mainCard.addView(headerLayout)

        // Player Name Section with space theme
        val nameSection = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 20, 25, 20)
            setBackgroundColor(Color.parseColor("#1A1A2E"))
        }

        val nameLabel = TextView(context).apply {
            text = "👤 Your Player Name:"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 10)
        }
        nameSection.addView(nameLabel)

        val nameInputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        nameInput = EditText(context).apply {
            hint = "Enter your name"
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.parseColor("#2A2A3E"))
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

            // Rounded corners
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#2A2A3E"))
                cornerRadius = 10f
                setStroke(2, Color.parseColor("#3296C8"))
            }
        }
        nameInputLayout.addView(nameInput)

        saveNameButton = Button(context).apply {
            text = "SAVE"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(30, 20, 30, 20)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(15, 0, 0, 0)
            }

            // Button styling
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#3296C8"))
                cornerRadius = 10f
            }

            setOnClickListener { saveName() }
        }
        nameInputLayout.addView(saveNameButton)

        nameSection.addView(nameInputLayout)
        mainCard.addView(nameSection)

        // Content area
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(25, 25, 25, 25)
            setBackgroundColor(Color.parseColor("#0A0A1E"))
        }

        // Progress bar
        progressBar = ProgressBar(context).apply {
            visibility = View.VISIBLE
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.rgb(50, 150, 200))
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
            setTextColor(Color.rgb(255, 100, 100))
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

        // Close button with space theme
        val closeButton = TextView(context).apply {
            text = "✕ CLOSE"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#3296C8"))
            gravity = Gravity.CENTER
            setPadding(0, 35, 0, 35)
            setOnClickListener {
                dismiss()
            }
        }
        mainCard.addView(closeButton)

        setContentView(mainCard)
    }

    private fun saveName() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.length > 20) {
            Toast.makeText(context, "Name too long (max 20 characters)", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseManager.savePlayerName(name, object : FirebaseManager.SaveCallback {
            override fun onSuccess() {
                Toast.makeText(context, "✅ Name saved: $name", Toast.LENGTH_SHORT).show()
                loadLeaderboard() // Refresh leaderboard to show updated name
            }

            override fun onFailure(error: String) {
                Toast.makeText(context, "❌ Failed to save name", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPlayerName() {
        firebaseManager.loadPlayerName(object : FirebaseManager.PlayerNameCallback {
            override fun onSuccess(name: String) {
                nameInput.setText(name)
            }

            override fun onFailure(error: String) {
                // No name saved yet, leave empty
            }
        })
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
            text = "🚀\n\nNo scores yet!\nBe the first space jumper!"
            textSize = 20f
            setTextColor(Color.rgb(100, 150, 200))
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

            // Entry row with space theme
            val entryLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 22, 20, 22)

                // Background colors matching space theme
                background = GradientDrawable().apply {
                    setColor(
                        when {
                            isCurrentUser -> Color.parseColor("#2A3A5E") // Highlighted blue for current user
                            rank == 1 -> Color.parseColor("#2A2A1E") // Dark gold tint
                            rank == 2 -> Color.parseColor("#1E1E2A") // Dark silver tint
                            rank == 3 -> Color.parseColor("#2A1E1E") // Dark bronze tint
                            else -> Color.parseColor("#1A1A2E") // Default dark
                        }
                    )
                    cornerRadius = 10f
                    if (isCurrentUser) {
                        setStroke(3, Color.parseColor("#3296C8")) // Blue border for current user
                    }
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 5, 0, 5)
                }
                layoutParams = params
            }

            // Rank with medals
            val rankText = TextView(context).apply {
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#$rank"
                }
                textSize = 28f
                setTypeface(null, Typeface.BOLD)
                setTextColor(
                    when (rank) {
                        1 -> Color.rgb(255, 215, 0)
                        2 -> Color.rgb(192, 192, 192)
                        3 -> Color.rgb(205, 127, 50)
                        else -> Color.WHITE
                    }
                )
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(90, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            entryLayout.addView(rankText)

            // Username section
            val nameLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(15, 0, 15, 0)
            }

            val usernameText = TextView(context).apply {
                text = entry.username
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
            }
            nameLayout.addView(usernameText)

            if (isCurrentUser) {
                val youBadge = TextView(context).apply {
                    text = "👤 You"
                    textSize = 14f
                    setTextColor(Color.rgb(100, 200, 255))
                    setPadding(0, 3, 0, 0)
                }
                nameLayout.addView(youBadge)
            }

            entryLayout.addView(nameLayout)

            // Score with space theme
            val scoreText = TextView(context).apply {
                text = entry.score.toString()
                textSize = 26f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(255, 215, 0)) // Gold color for scores
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                minWidth = 100
            }
            entryLayout.addView(scoreText)

            leaderboardContainer.addView(entryLayout)
        }
    }
}