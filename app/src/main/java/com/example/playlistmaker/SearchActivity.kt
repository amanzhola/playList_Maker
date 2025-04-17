package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.search.ErrorManager
import com.example.playlistmaker.search.ErrorState
import com.example.playlistmaker.search.ITunesApi
import com.example.playlistmaker.search.OnTrackClickListener
import com.example.playlistmaker.search.SearchHistory
import com.example.playlistmaker.search.SearchViewModel
import com.example.playlistmaker.search.SearchViewModelFactory
import com.example.playlistmaker.search.Track
import com.example.playlistmaker.search.TrackAdapter
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
    private lateinit var apiService: ITunesApi
    private val textView: TextView by lazy { findViewById(R.id.fail) }
    private val history: TextView by lazy { findViewById(R.id.history) }
    private val update: MaterialButton by lazy { findViewById(R.id.btnUpdate) }
    private lateinit var searchHistory: SearchHistory
    private lateinit var errorManager : ErrorManager

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(SearchHistory.PREFS_NAME, Context.MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences, history, update)
        errorManager = ErrorManager(textView, update, recyclerView)
        findViewById<TextView>(R.id.bottom1).isSelected = true

        apiService = ITunesApi.create()
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(mutableListOf(), this, this)
        recyclerView.adapter = adapter

        val viewModelFactory = SearchViewModelFactory(apiService, searchHistory, sharedPreferences)
        viewModel = ViewModelProvider(this, viewModelFactory)[SearchViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers(){

        viewModel.isLoading.observe(this) { isLoading ->
            findViewById<ProgressBar>(R.id.progressBar).visibility = if (isLoading) VISIBLE else GONE
        }

        viewModel.errorState.observe(this) { errorState ->
            when (errorState ?: ErrorState.NONE) {
                ErrorState.ERROR -> {
                    errorManager.showError()
                }

                ErrorState.FAILURE -> {
                    errorManager.showFailure()
                    update.setOnClickListener {
                        errorManager.hideError()
                        viewModel.onSearchActionDone()
                    }
                }

                ErrorState.NONE -> {
                    errorManager.hideError()

                    viewModel.displayTrackList.observe(this) { tracks ->
                        val mutableTracks: MutableList<Track> = tracks.toMutableList()
                        adapter.updateTracks(mutableTracks)
                    }

                    viewModel.isHistory.observe(this) { isInHistory ->
                        if (isInHistory) searchHistory.showHistory()
                        else searchHistory.hideHistory()
                    }

                    viewModel.isInputFocused.observe(this) { isFocused ->
                        searchInputLayout.hint = if (isFocused) null else getString(R.string.search)
//                        if (isFocused) hideBottomNavigation() else showBottomNavigation()
                    }

                    viewModel.searchQuery.observe(this) { query ->
                        clearIcon.visibility = if (!query.isNullOrEmpty()) VISIBLE else GONE
                    }

                }
            }
        }
    }
    override fun onTrackClicked(track: Track) = viewModel.onTrackClicked(track)
    override fun onArrowClicked(track: Track) = viewModel.removeTrack(track)

    private fun setupListeners() {
        inputEditText.addTextChangedListener(createTextWatcher())
        clearIcon.setOnClickListener { clearSearchInput() }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.setInputFocused(hasFocus)
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchActionDone()
                true
            } else {
                false
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) { }
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

    override fun reverseList() {
        val currentTracksList = adapter.getTracks()
        val reversedList = currentTracksList.reversed().toMutableList()
        adapter.updateTracks(reversedList)
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

}