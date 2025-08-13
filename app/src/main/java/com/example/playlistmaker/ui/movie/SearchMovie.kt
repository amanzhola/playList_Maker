package com.example.playlistmaker.ui.movie

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.movie.MovieStorageHelper
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.presentation.movieViewModels.MoviesViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.movie.MovieRootActivity
import com.example.playlistmaker.ui.movie.moviePosters.MoviePager
import com.example.playlistmaker.ui.movie.moviePosters.MoviePagerList
import com.example.playlistmaker.utils.CLICK_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.ClickDebouncer
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.UIUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG_HISTORY_VM = "HistoryVM"

class SearchMovie : BaseActivity() { // 🔁 👉 🎬🧼🏗️✅

    companion object {
        private const val KEY_QUERY_TEXT = "search_query_text" // 💾 сохраняем текст поля
    }

    // 🧯 глушим TextWatcher при программной очистке/восстановлении
    private var suppressWatcher = false

    // ⏳ ручной debounce (ровно 2 секунды тишины перед запросом)
    private var typingJob: Job? = null

    private lateinit var clickDebouncer: ClickDebouncer
    private var isDialogShown = false

    private lateinit var uiUpdater: UIUpdater

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var placeholderMessage: TextView
    private lateinit var moviesList: RecyclerView

    private val viewModel: MoviesViewModel by viewModel()
    private val movieStorageHelper: MovieStorageHelper by inject() // 👉 📦

    private val adapter by lazy {
        MoviesAdapter(
            { event -> handleMovieEvent(event) },
            { movie -> onFavoriteClicked(movie) } // (❤️)
        )
    }

    private fun handleMovieEvent(event: MoviesEvent) {
        val selectedEvent = event as? MoviesEvent.SingleMovie ?: return
        val selectedMovie = selectedEvent.movie
        val position = selectedEvent.position

        clickDebouncer.tryClick { // ⛔ 🕒 1 секунда защита
            showChoiceDialog(selectedMovie, position)
        }
    }

    private lateinit var moviePagerLauncher: ActivityResultLauncher<Intent>

    private fun showChoiceDialog(selectedMovie: Movie, position: Int) {
        if (isDialogShown) return
        isDialogShown = true

        val options = arrayOf(getString(R.string.first), getString(R.string.second), getString(R.string.third))

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите опцию")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        movieStorageHelper.saveMovie(selectedMovie)
                        moviePagerLauncher.launch(Intent(this, MoviePager::class.java))
                    }
                    1 -> {
                        val movieList = adapter.getMovies()
                        movieStorageHelper.saveMovieList(movieList)
                        movieStorageHelper.setCurrentIndex(position)
                        moviePagerLauncher.launch(Intent(this, MoviePagerList::class.java))
                    }
                    2 -> {
                        val intent = Intent(this, MovieRootActivity::class.java)
                            .putExtra("poster", selectedMovie.image)
                            .putExtra("id", selectedMovie.id)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Отмена") { d, _ -> d.dismiss() }
            .setOnDismissListener { isDialogShown = false } // ✅ не даём открыть второй раз подряд
            .show()
    }

    private var isBottomNavVisible = true

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clickDebouncer = ClickDebouncer(CLICK_DEBOUNCE_DELAY, lifecycleScope)
        uiUpdater = UIUpdater(
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = findViewById(R.id.placeholderMessage),
            recyclerView = findViewById(R.id.movies)
        )

        // ——— init views ———
        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        clearButton = findViewById(R.id.clearButton)
        moviesList = findViewById(R.id.movies)

        moviesList.layoutManager = LinearLayoutManager(this)
        moviesList.adapter = adapter

        // 🔕 убираем анимации — чтобы старые элементы не «вспыхивали»
        (moviesList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        moviesList.itemAnimator = null

        moviePagerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    viewModel.refreshFavorites()
                }
            }

        // ——— restore текста при ротации/теме ———
        savedInstanceState?.getString(KEY_QUERY_TEXT)?.let { restored ->
            if (restored.isNotEmpty()) {
                suppressWatcher = true
                queryInput.setText(restored)
                queryInput.setSelection(restored.length)
                suppressWatcher = false
                clearButton.isVisible = true
                // держим дефолтный экран; реальный поиск — только после 2с тишины
                adapter.updateMovies(emptyList())
                uiUpdater.showDefault()
            }
        }

        // ——— Подписка на VM ———
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MoviesViewModel.UiState.Loading -> {
                            // 🧹 убираем старые результаты пока ждём ответ
                            adapter.updateMovies(emptyList())
                            uiUpdater.showLoading()
                        }
                        is MoviesViewModel.UiState.Success -> {
                            adapter.updateMovies(state.movies)
                            uiUpdater.showData()
                        }
                        is MoviesViewModel.UiState.Empty -> {
                            adapter.updateMovies(emptyList())
                            uiUpdater.showMessage(getString(R.string.nothing_found))
                        }
                        is MoviesViewModel.UiState.Error -> {
                            adapter.updateMovies(emptyList())
                            val userFacing =
                                getString(R.string.something_went_wrong) + "\n" + state.message
                            uiUpdater.showMessage(userFacing)
                        }
                        is MoviesViewModel.UiState.Default -> {
                            adapter.updateMovies(emptyList())
                            uiUpdater.showDefault()
                        }
                    }
                }
            }
        }

        // ——— ОДИН TextWatcher с ручным debounce ровно 2s ———
        queryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (suppressWatcher) return

                val text = s?.toString().orEmpty()

                // 🎛️ крестик
                clearButton.isVisible = text.isNotEmpty()

                // перезапускаем «таймер тишины»
                typingJob?.cancel()

                if (text.isEmpty()) {
                    // 🧹 пустой ввод → сразу дефолт
                    adapter.updateMovies(emptyList())
                    uiUpdater.showDefault()
                    viewModel.setDefaultState()
                    return
                }

                // ⏳ ждём SEARCH_DEBOUNCE_DELAY «без набора»
                typingJob = lifecycleScope.launch {
                    delay(SEARCH_DEBOUNCE_DELAY)
                    // 💬 emoji-safe: передаём сырую строку, VM сама триммит корректно
                    viewModel.onSearchQueryEntered(text)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // ——— крестик очистки ———
        clearButton.setOnClickListener {
            // 🚫 останавливаем отложенный запуск
            typingJob?.cancel()

            // 🤫 не триггерим TextWatcher повторно
            suppressWatcher = true
            queryInput.setText("")
            suppressWatcher = false

            // 🧼 UI моментально чистим
            adapter.updateMovies(emptyList())
            uiUpdater.showDefault()

            // 🔁 сбрасываем VM (query="" и локальные правки)
            viewModel.setDefaultState()
        }

        // косметика: при фокусе на пустом поле — чистый экран
        queryInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && queryInput.text.isNullOrEmpty()) {
                adapter.updateMovies(emptyList())
                uiUpdater.showDefault()
            }
        }

        findViewById<TextView>(R.id.bottom4).isSelected = true
    }

    // 💾 сохраняем текст вручную (без системного автосейва)
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_QUERY_TEXT, queryInput.text?.toString().orEmpty())
        super.onSaveInstanceState(outState)
    }

    private fun onFavoriteClicked(movie: Movie) {
        viewModel.toggleFavorite(movie.id) // (❤️)
    }

    override fun reverseList() {
        val currentMovies = adapter.getMovies()
        adapter.updateMovies(currentMovies.reversed())
        moviesList.scrollToPosition(0)
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getLayoutId() = R.layout.activity_search_movie
    override fun getMainLayoutId() = R.id.main
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.movie) { navigateToMainScreen(this@SearchMovie, -1) }

    override fun shouldEnableEdgeToEdge(): Boolean = false

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // 🧹 подчистим VM на всякий
            viewModel.setDefaultState()
        }
    }
}
