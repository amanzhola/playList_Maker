package com.example.playlistmaker.movie

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.google.gson.Gson

class MoviePager : AppCompatActivity() {

    private lateinit var selectedMovie: Movie
    private lateinit var backImageButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var genreTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var descriptionTextView: TextView

    private var movieJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movie_pager)

        backImageButton = findViewById(R.id.menu_button)
        titleTextView = findViewById(R.id.title)
        coverImageView = findViewById(R.id.cover)
        genreTextView = findViewById(R.id.genre)
        ratingTextView = findViewById(R.id.rating)
        descriptionTextView = findViewById(R.id.description)

        val gson = Gson()

        movieJson = if (savedInstanceState != null) savedInstanceState.getString("selected_movie")
        else intent.getStringExtra("selected_movie")

        val movieJson = intent.getStringExtra("selected_movie")
        if (movieJson != null) selectedMovie = gson.fromJson(movieJson, Movie::class.java)

        /// Заполняем данными о фильме
        displayMovieDetails(selectedMovie)

        // Настраиваем слушатели кликов
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun displayMovieDetails(movie: Movie) {
        titleTextView.text = movie.title

        Glide.with(this)
            .load(movie.image)
            .into(coverImageView) // 🧨
        // ✍️
        genreTextView.text = movie.genres ?: "Жанр не указан"
        val formattedRating = "${movie.imDbRating ?: "N/A"}/10"
        ratingTextView.text = formattedRating
        val description = "${movie.description}\n${movie.plot ?: ""}"
        descriptionTextView.text = description
    }

    private fun setupClickListeners() {
        backImageButton.setOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selected_movie", movieJson)
    }
}
