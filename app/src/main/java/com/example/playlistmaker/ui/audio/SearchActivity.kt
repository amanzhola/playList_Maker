package com.example.playlistmaker.ui.audio

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.AudioTracksShare
import com.example.playlistmaker.presentation.audioViewModels.ErrorState
import com.example.playlistmaker.presentation.audioViewModels.SearchViewModel
import com.example.playlistmaker.presentation.utils.AudioErrorManager
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.utils.Debounce
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchActivity : BaseActivity(), OnTrackClickListener {

    private lateinit var adapter: TrackAdapter
    private var isBottomNavVisible = true

    private val inputEditText: TextInputEditText by lazy { findViewById(R.id.inputEditText) }
    private val clearIcon: ImageView by lazy { findViewById(R.id.clearIcon) }
    private val searchInputLayout: TextInputLayout by lazy { findViewById(R.id.search_box) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.tracks_recycler_view) }
    private val textView: TextView by lazy { findViewById(R.id.fail) }
    private val history: TextView by lazy { findViewById(R.id.history) }
    private val update: MaterialButton by lazy { findViewById(R.id.btnUpdate) }
    private lateinit var errorManager: AudioErrorManager
    private lateinit var trackShareService: AudioTracksShare

    private lateinit var viewModel: SearchViewModel
    private lateinit var debounce: Debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debounce = Debounce(SEARCH_DEBOUNCE_DELAY)
        errorManager = AudioErrorManager(textView, update, recyclerView)

        findViewById<TextView>(R.id.bottom1).isSelected = true
        trackShareService = Creator.provideTrackShareService(this)

        val viewModelFactory = Creator.provideSearchViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory)[SearchViewModel::class.java]

        recyclerView.layoutManager = LinearLayoutManager(this)
        val resourceColorProvider = Creator.provideResourceColorProvider(this)
        val networkChecker = Creator.provideNetworkStatusChecker(this)

        adapter = TrackAdapter(mutableListOf(), resourceColorProvider, networkChecker, this)
        recyclerView.adapter = adapter

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        viewModel.uiState.observe(this) { state ->
            /* 🔍 Прогресс */
            progressBar.isVisible = state.isLoading

            /* 🧼 Ошибки */
            when (state.error) {
                ErrorState.ERROR -> errorManager.showError()
                ErrorState.FAILURE -> {
                    errorManager.showFailure()
                    update.setOnClickListener {
                        viewModel.onSearchActionDone()
                        errorManager.hideError()
                    }
                }
                ErrorState.NONE -> {
                    errorManager.hideError()
                    update.isVisible = state.showHistory
                    update.setOnClickListener {
                        if (state.showHistory) viewModel.clearHistory()
                    }
                }
            }

            /* 🔁 Текст 🧹 🛠 🔄 ✍️ */
            val buttonText = if (state.error == ErrorState.NONE) R.string.clean else R.string.update
            update.text = getString(buttonText)

            /* 🧾 Список треков */
            adapter.updateTracks(state.displayedTracks.toMutableList())

            /* 🔙 История */
            history.isVisible = state.showHistory

            /* 🧼 Input hint */
            searchInputLayout.hint = if (state.isInputFocused) null else getString(R.string.search_hint)

            /* ❌ Крестик  */
            clearIcon.isVisible = state.isClearIconVisible

            /* ⬇️ Bottom nav */
            if (state.isInputFocused) hideBottomNavigation() else showBottomNavigation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        debounce.cancel()
    }

    private fun setupListeners() {
        inputEditText.addTextChangedListener(createTextWatcher())
        clearIcon.setOnClickListener { clearSearchInput() }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.setInputFocused(hasFocus)
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
                debounce.debounce {
                    viewModel.onSearchActionDone()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearchInput() {
        viewModel.clearSearchInput()
        inputEditText.text?.clear()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    override fun onTrackClicked(track: Track) = viewModel.onTrackClicked(track)
    override fun onArrowClicked(track: Track) = viewModel.removeTrack(track)

    override fun reverseList() {
        adapter.reverseTracks()
        recyclerView.scrollToPosition(0)
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.search) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_search
    override fun getMainLayoutId(): Int = R.id.main
    fun getAdapter(): TrackAdapter = adapter

    fun shareTrackHistoryFromViewModel() {
        val tracks = viewModel.getTrackHistoryList()
        trackShareService.shareTracks(tracks, R.string.history_track)
    }
}