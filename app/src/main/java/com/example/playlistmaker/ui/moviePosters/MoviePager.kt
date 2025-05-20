package com.example.playlistmaker.ui.moviePosters

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.repository.ShareMovie

class MoviePager : AppCompatActivity() {

    private lateinit var backImageButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var coverImageView: ImageView
    private lateinit var genreTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var shareButton: ImageButton
    private lateinit var shareMovieHelper: ShareMovie
    private lateinit var selectedMovie: Movie //  🎓

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
        shareButton = findViewById(R.id.shareButton)

        shareMovieHelper = Creator.provideShareMovieHelper(this) // 🌼

        val helper = Creator.provideMovieStorageHelper(this)
        val movie = helper.getMovie()

        if (movie != null) {
            selectedMovie = movie
            displayMovieDetails(selectedMovie)
        } else {
            Toast.makeText(this, "Не удалось загрузить фильм", Toast.LENGTH_SHORT).show()
            finish()
        }

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
        val description = "${movie.year}\n${movie.plot ?: ""}"
        descriptionTextView.text = description
    }

    private fun setupClickListeners() {

        shareButton.setOnClickListener {// 🎥 📤 🔜 🍿 ✨ 💃
            shareMovieHelper.shareMovieOrNotify(selectedMovie)
        }

        backImageButton.setOnClickListener {
            finish()
        }
    }
}
