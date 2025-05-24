package com.example.playlistmaker.ui.moviePosters

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.usecases.ToggleFavoriteUseCase
import com.example.playlistmaker.ui.movie.MoviesAdapterList

class MoviePagerList : AppCompatActivity() {

    private lateinit var movieViewPager: ViewPager2 // 🎓
    private lateinit var moviesAdapter: MoviesAdapterList
    private var movies: List<Movie> = emptyList()
    private var isVertical = false
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movie_pager_list)

        movieViewPager = findViewById(R.id.movie_view_pager)

        val movieStorageHelper = Creator.provideMovieStorageHelper(this)
        toggleFavoriteUseCase = Creator.provideToggleFavoriteUseCase(this)

        if (savedInstanceState == null) {
            // 📥 Получаем список и индекс из хранилища
            val movieList = movieStorageHelper.getMovieList()
            val selectedIndex = movieStorageHelper.getCurrentIndex()

            if (movieList.isNotEmpty()) {
                val favoriteIds = toggleFavoriteUseCase.getFavorites()
                movies = movieList.map { movie ->
                    movie.copy(inFavorite = favoriteIds.contains(movie.id))
                }
                setupViewPager(movies, selectedIndex, isVertical)
            } else {
                Toast.makeText(this, "Не удалось загрузить список фильмов", Toast.LENGTH_SHORT).show()
                finish()
            }

        } else {
            // 🔄 Восстанавливаем UI состояние (index, orientation)
            val selectedIndex = savedInstanceState.getInt("SELECTED_INDEX", 0)
            isVertical = savedInstanceState.getBoolean("IS_VERTICAL", false)

            val movieList = movieStorageHelper.getMovieList()
            if (movieList.isNotEmpty()) {
                val favoriteIds = toggleFavoriteUseCase.getFavorites()
                movies = movieList.map { movie ->
                    movie.copy(inFavorite = favoriteIds.contains(movie.id))
                }
                setupViewPager(movies, selectedIndex, isVertical)
            } else {
                Toast.makeText(this, "Список фильмов пуст при восстановлении", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewPager(movies: List<Movie>, selectedIndex: Int, isVertical: Boolean) {
        moviesAdapter = MoviesAdapterList(movies, ::toggleOrientation, this){ movieId ->

            // Здесь вызываем toggleFavoriteUseCase
            val isNowFavorite = toggleFavoriteUseCase(movieId)

            // Обновляем состояние фильма в списке
            movies.find { it.id == movieId }?.inFavorite = isNowFavorite

            // Обновляем UI (лучше обновлять только элемент по позиции, но для простоты — notifyDataSetChanged)
            moviesAdapter.notifyDataSetChanged()
        }
        movieViewPager.adapter = moviesAdapter

        movieViewPager.setCurrentItem(selectedIndex, false)
        setViewPagerOrientation(isVertical)
    }

    private fun setViewPagerOrientation(isVertical: Boolean) {
        movieViewPager.orientation = if (isVertical) ViewPager2.ORIENTATION_VERTICAL
        else ViewPager2.ORIENTATION_HORIZONTAL
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("SELECTED_INDEX", movieViewPager.currentItem)
        outState.putBoolean("IS_VERTICAL", isVertical)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleOrientation() {
        isVertical = !isVertical
        setViewPagerOrientation(isVertical)
    }
}