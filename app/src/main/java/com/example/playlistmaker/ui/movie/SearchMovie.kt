package com.example.playlistmaker.ui.movie

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.example.playlistmaker.utils.UIUpdater
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchMovie : BaseActivity() { // 🔁 👉 🎬 экран поиска • 🧼 чистки • 🏗️ сборка UI • ✅ готово

    private lateinit var clickDebouncer: ClickDebouncer      // 🛑🕒 анти-даблклик (защита от быстрых тапов)
    private var isDialogShown = false                        // 🚪 флаг «диалог уже открыт»

    private lateinit var uiUpdater: UIUpdater                // 🎛️ переключатель: лоадер / плейсхолдер / список

    // ── View refs ───────────────────────────────────────────────────────────────
    private lateinit var searchButton: Button                // 🔍 кнопка поиска (сейчас не используется — авто-поиск)
    private lateinit var queryInput: EditText                // ⌨️ поле ввода запроса
    private lateinit var placeholderMessage: TextView        // 📨 плейсхолдер сообщений
    private lateinit var moviesList: RecyclerView            // 🎞️ список фильмов
    private lateinit var clearButton: ImageButton            // ❌ очистить запрос

    // ── DI / VM ─────────────────────────────────────────────────────────────────
    private val viewModel: MoviesViewModel by viewModel()    // 🧠 VM на Flow/StateFlow
    private val movieStorageHelper: MovieStorageHelper by inject() // 📦 крошечное хранилище между экранами

    // ── Adapter ─────────────────────────────────────────────────────────────────
    private val adapter by lazy {
        MoviesAdapter(
            { event -> handleMovieEvent(event) },            // 🎯 тап по карточке → события
            { movie -> onFavoriteClicked(movie) }            // ❤️ тап по избранному
        )
    }

    // Обработка событий из адаптера
    private fun handleMovieEvent(event: MoviesEvent) {
        val selectedEvent = event as? MoviesEvent.SingleMovie // 🧲 безопасный даункаст
        selectedEvent?.let {
            val selectedMovie = it.movie                      // 🎬 выбранный фильм
            val position = it.position                        // #️⃣ позиция в списке

            clickDebouncer.tryClick {                         // 🛡️ 1с защита от повторных кликов
                showChoiceDialog(selectedMovie, position)     // 🪟 диалог выбора действия
            }
        }
    }

    private lateinit var moviePagerLauncher: ActivityResultLauncher<Intent> // 🎬 Activity Result API

    // Диалог с 3 опциями навигации
    private fun showChoiceDialog(selectedMovie: Movie, position: Int) {

        if (isDialogShown) return                              // 🚫 уже показывается
        isDialogShown = true                                   // ✅ ставим флажок

        val options = arrayOf(getString(R.string.first), getString(R.string.second), getString(R.string.third)) // 🧾 три варианта

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите опцию")                        // 🏷️ заголовок
            .setItems(options) { dialog, which ->              // 🧩 обработка выбора
                when (which) {
                    0 -> {                                     // 1) Открыть одиночный пейджер
                        movieStorageHelper.saveMovie(selectedMovie)           // 💾 сохранить выбранный
                        val intent = Intent(this, MoviePager::class.java)    // 🎯 экран пейджера
                        moviePagerLauncher.launch(intent)                     // 🚀 старт
                    }
                    1 -> {                                     // 2) Открыть пейджер списка
                        val movieList: List<Movie> = adapter.getMovies()     // 🧺 текущий список
                        movieStorageHelper.saveMovieList(movieList)          // 💾 сохранить список
                        movieStorageHelper.setCurrentIndex(position)         // 🎯 стартовый индекс

                        val intent = Intent(this, MoviePagerList::class.java) // 📚 экран лист-пейджера
                        moviePagerLauncher.launch(intent)                     // 🚀
                    }
                    2 -> {                                     // 3) Перейти в детали
                        val intent = Intent(this, MovieRootActivity::class.java) // 🧭 экран деталей
                        intent.putExtra("poster", selectedMovie.image)            // 🖼️ постер
                        intent.putExtra("id", selectedMovie.id)                   // 🆔 id фильма
                        startActivity(intent)                                     // 🚀
                    }
                }
            }
            .setNegativeButton("Отмена") { d, _ -> d.dismiss() }               // 🙅 отмена
            .setOnDismissListener { isDialogShown = false }                     // 🧹 сброс флага, когда окно закрылось
            .show()
    }

    private var isBottomNavVisible = true                 // 👇/👆 состояние нижней навигации

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // toolbar save and apply background color
        applySavedColorsForCurrentScreen()

        // ── Инициализация вспомогательных компонентов ───────────────────────────
        clickDebouncer = ClickDebouncer(CLICK_DEBOUNCE_DELAY, lifecycleScope)   // 🛑🕒 анти-даблклик
        uiUpdater = UIUpdater(                                                   // 🎛️ control center
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = findViewById(R.id.placeholderMessage),
            recyclerView = findViewById(R.id.movies)
        )

        // ── Поиск View ──────────────────────────────────────────────────────────
        placeholderMessage = findViewById(R.id.placeholderMessage)  // 🔎
        searchButton = findViewById(R.id.searchButton)              // 🔎 (не используется с авто-поиском)
        queryInput = findViewById(R.id.queryInput)                  // ⌨️
        moviesList = findViewById(R.id.movies)                      // 🎞️
        clearButton = findViewById(R.id.clearButton)                // ❌

        // ── Начальное состояние поля ввода ─────────────────────────────────────
        val vmText = viewModel.query.value                          // 🔁 восстановим ввод из VM (переживает повороты)
        if (queryInput.text?.toString() != vmText) {
            queryInput.setText(vmText)                              // ↩️ проставим текст
            queryInput.setSelection(vmText.length)                  // 📍 курсор в конце
        }
        clearButton.isVisible = vmText.isNotEmpty()                 // 👁️ показать/скрыть крестик

        // ── Кнопка «крестик» ───────────────────────────────────────────────────
        clearButton.setOnClickListener {
            queryInput.setText("")                                  // 🧹 очистить поле
            viewModel.setDefaultState()                             // 🧽 сбросить состояние VM
        }

        // ── RecyclerView ───────────────────────────────────────────────────────
        moviesList.layoutManager = LinearLayoutManager(this)        // 🧱 линейный список
        moviesList.adapter = adapter                                // 🔌 подключаем адаптер

        // ── Activity Result (пейджеры) ─────────────────────────────────────────
        moviePagerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.refreshFavorites()                        // 🔄❤️ обновим «избранное» после возврата
            }
        }

        // ── Подписки на StateFlow из ViewModel ─────────────────────────────────
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {            // 🔁 подписки активны, когда экран видим
                // 🎞️ список фильмов
                launch {
                    viewModel.movies.collect { newMovies ->
                        adapter.updateMovies(newMovies)             // 🔄 отрисовываем новые данные
                    }
                }
                // 🎛️ ui-state (лоадер/ошибки/пусто/данные)
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is MoviesViewModel.UiState.Loading -> uiUpdater.showLoading() // ⏳
                            is MoviesViewModel.UiState.Success -> uiUpdater.showData()    // ✅
                            is MoviesViewModel.UiState.Error -> {                         // ❌
                                val userFacingMessage = getString(R.string.something_went_wrong) +
                                        "\n" + state.message
                                uiUpdater.showMessage(userFacingMessage)
                            }
                            is MoviesViewModel.UiState.Empty -> uiUpdater.showMessage(getString(R.string.nothing_found)) // 🫙
                            is MoviesViewModel.UiState.Default -> { /* 💤 ничего не делаем */ }
                        }
                    }
                }
            }
        }

        // ── Автопоиск: реакция на ввод ─────────────────────────────────────────

        // 1) sprint 20 replaced by Полный автоматический поиск EditText + Flow.debounce
//        searchButton.setOnClickListener {
//            val query = queryInput.text.toString()
//            viewModel.onSearchQueryEntered(query)
//        }

        // add by sprint 20 -> Полный автоматический поиск EditText + Flow.debounce
        // 2) UI -> VM
        queryInput.doAfterTextChanged { s ->
            val txt = s?.toString().orEmpty()                       // ✍️ текущий ввод
            if (txt != viewModel.query.value) {                     // 🧯 защита от лишних триггеров
                viewModel.onSearchQueryEntered(txt)                 // 📩 отправили в VM
            }
            clearButton.isVisible = txt.isNotEmpty()                // 👁️ показать крестик, если есть текст
        }

        findViewById<TextView>(R.id.bottom4).isSelected = true      // ⭐ подсветим пункт навигации
    }

    // ❤️ клик по избранному на карточке
    private fun onFavoriteClicked(movie: Movie) {
        viewModel.toggleFavorite(movie.id)                          // 🔁 flip избранного
    }

    // 🔃 реверс списка (пример доп. фичи)
    override fun reverseList() {
        val currentMovies = adapter.getMovies()
        val reversed = currentMovies.reversed()                     // 🔁 переворачиваем порядок
        adapter.updateMovies(reversed)
        moviesList.scrollToPosition(0)                              // ⬆️ прокрутка к началу
    }

    // 👇/👆 показать/скрыть нижнюю навигацию
    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible                    // 🔄 переключили флаг
    }

    // ── BaseActivity hooks ─────────────────────────────────────────────────────
    override fun getLayoutId() = R.layout.activity_search_movie     // 🧱 layout ресурc
    override fun getMainLayoutId() = R.id.main                      // 🎯 корневой контейнер
    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(GONE, R.string.movie) {
        navigateToMainScreen(this@SearchMovie, -1)                  // 🧭 обработчик навбара «назад»
    }
    override fun shouldEnableEdgeToEdge(): Boolean = false          // ⛔ без edge-to-edge для экрана

    // toolbar save and apply background color
    override fun onResume() {
        super.onResume()
        applyThemeThenRestoreSaved()
    }
}