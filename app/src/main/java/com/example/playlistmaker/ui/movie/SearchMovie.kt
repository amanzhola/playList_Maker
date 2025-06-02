package com.example.playlistmaker.ui.movie

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.ui.moviePosters.MoviePager
import com.example.playlistmaker.ui.moviePosters.MoviePagerList
import com.example.playlistmaker.utils.UIUpdater
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchMovie : BaseActivity() { // 🔁 👉 🎬🧼🏗️✅

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 2000L
    }

    private var isClickable = true
    private lateinit var uiUpdater: UIUpdater

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var moviesList: RecyclerView

    private val viewModel: MoviesViewModel by viewModel()
    private val movieStorageHelper: MovieStorageHelper by inject() // 👉 📦
    // provideMovieStorageHelper shows fail -> see TrackAdapter newFiles  💥

    private val adapter by lazy {
        MoviesAdapter(
            { event -> handleMovieEvent(event) },
            { movie -> onFavoriteClicked(movie) } //  (❤️)
        )
    }

    private fun handleMovieEvent(event: MoviesEvent) {
        val selectedEvent = event as? MoviesEvent.SingleMovie
        selectedEvent?.let {
            val selectedMovie = it.movie
            val position = it.position
            showChoiceDialog(selectedMovie, position)
        }
    }

    private lateinit var moviePagerLauncher: ActivityResultLauncher<Intent>

    private fun showChoiceDialog(selectedMovie: Movie, position: Int) {
        if (!clickDebounce()) return
        val options = arrayOf("Один фильм", "Список фильмов")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите опцию")
            .setItems(options) { dialog, which ->

                when (which) {
                    0 -> {
                        movieStorageHelper.saveMovie(selectedMovie)
                        val intent = Intent(this, MoviePager::class.java)
                        moviePagerLauncher.launch(intent)
                    }
                    1 -> {
                        val movieList: List<Movie> = adapter.getMovies()
                        movieStorageHelper.saveMovieList(movieList)
                        movieStorageHelper.setCurrentIndex(position)

                        val intent = Intent(this, MoviePagerList::class.java)
                        moviePagerLauncher.launch(intent)
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

        uiUpdater = UIUpdater(
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = findViewById(R.id.placeholderMessage),
            recyclerView = findViewById(R.id.movies)
        )

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        moviesList = findViewById(R.id.movies)

        moviesList.layoutManager = LinearLayoutManager(this)
        moviesList.adapter = adapter

        moviePagerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Перезагрузи список или обнови избранное
                viewModel.refreshFavorites() // ты можешь сделать такую функцию в ViewModel
            }
        }

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

        searchButton.setOnClickListener {
            val query = queryInput.text.toString()
            viewModel.onSearchQueryEntered(query)
        }


        findViewById<TextView>(R.id.bottom4).isSelected = true
    }

    private fun onFavoriteClicked(movie: Movie) {
        viewModel.toggleFavorite(movie.id) //  (❤️)
    }

    private fun clickDebounce(): Boolean {
        return if (isClickable) {
            isClickable = false
            Handler(Looper.getMainLooper()).postDelayed({ isClickable = true }, CLICK_DEBOUNCE_DELAY)
            true
        } else {
            false
        }
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
