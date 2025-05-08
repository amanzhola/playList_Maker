package com.example.playlistmaker.ui.movie

import MoviesInteraction
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ToolbarConfig
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModelFactory
import com.example.playlistmaker.ui.moviePosters.MoviePager
import com.example.playlistmaker.ui.moviePosters.MoviePagerList
import com.example.playlistmaker.utils.Debounce
import com.example.playlistmaker.utils.UIUpdater
import com.google.gson.Gson

class SearchMovie : BaseActivity() {

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private lateinit var debounce: Debounce
    private var isClickable = true

    private lateinit var uiUpdater: UIUpdater

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var moviesList: RecyclerView

    private val moviesInteraction: MoviesInteraction = Creator.provideMoviesInteraction()

    private lateinit var viewModel: MoviesViewModel

    private val adapter by lazy { MoviesAdapter { event -> handleMovieEvent(event) } }

    private fun handleMovieEvent(event: MoviesEvent) {
        val selectedEvent = event as? MoviesEvent.SingleMovie
        selectedEvent?.let {
            val selectedMovie = it.movie
            val position = it.position
            showChoiceDialog(selectedMovie, position)
        }
    }

    private fun showChoiceDialog(selectedMovie: Movie, position: Int) {
        if (!clickDebounce()) return
        val options = arrayOf("Один фильм", "Список фильмов")
        val gson = Gson()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите опцию")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val movieJson = gson.toJson(selectedMovie)
                        val intent = Intent(this, MoviePager::class.java)
                        intent.putExtra("selected_movie", movieJson)
                        startActivity(intent)
                    }
                    1 -> {
                        val movieList: List<Movie> = adapter.getMovies()
                        val movieJsonList = gson.toJson(movieList)
                        val intent = Intent(this, MoviePagerList::class.java)
                        intent.putExtra("MOVIE_LIST_JSON", movieJsonList)
                        intent.putExtra("MOVIE_INDEX", position)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Отмена") { d, _ -> d.dismiss() }
            .show()
    }

    private var isBottomNavVisible = true

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация UIUpdater
        uiUpdater = UIUpdater(
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = findViewById(R.id.placeholderMessage),
            recyclerView = findViewById(R.id.movies)
        )

        viewModel = ViewModelProvider(this, MoviesViewModelFactory(moviesInteraction))[MoviesViewModel::class.java]

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        moviesList = findViewById(R.id.movies)

        moviesList.layoutManager = LinearLayoutManager(this)
        moviesList.adapter = adapter

        viewModel.movies.observe(this) { newMovies ->
            adapter.updateMovies(newMovies)
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                MoviesViewModel.UiState.Loading -> uiUpdater.showLoading()
                is MoviesViewModel.UiState.Success -> uiUpdater.showData()
                is MoviesViewModel.UiState.Error -> {
                    val technicalErrorMessage = state.message
                    val userFacingMessage = getString(R.string.something_went_wrong) + "\n" + technicalErrorMessage
                    uiUpdater.showMessage(userFacingMessage)
                }
                MoviesViewModel.UiState.Empty -> uiUpdater.showMessage(getString(R.string.nothing_found))
                MoviesViewModel.UiState.Default -> { /* Ничего не делаем, или сбрасываем состояние */ }
            }
        }


        debounce = Debounce(CLICK_DEBOUNCE_DELAY)

        searchButton.setOnClickListener {
            val query = queryInput.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchMovies(query)
            } else {
                uiUpdater.showMessage(getString(R.string.enter_movie_name))
            }
        }

        findViewById<TextView>(R.id.bottom4).isSelected = true
    }

    private fun clickDebounce(): Boolean {
        if (isClickable) {
            isClickable = false
            debounce.debounce {
                isClickable = true
            }
            return true
        }
        return false
    }

    override fun reverseList() {
        val currentMovies = adapter.getMovies()
        val reversed = currentMovies.reversed()
        adapter.updateMovies(reversed)
        moviesList.scrollToPosition(0)
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getLayoutId() = R.layout.activity_search_movie
    override fun getMainLayoutId() = R.id.main
    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.movie) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
}