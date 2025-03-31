package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.search.ErrorManager
import com.example.playlistmaker.search.ITunesApi
import com.example.playlistmaker.search.OnTrackClickListener
import com.example.playlistmaker.search.SearchHistory
import com.example.playlistmaker.search.SearchResponse
import com.example.playlistmaker.search.Track
import com.example.playlistmaker.search.TrackAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : BaseActivity(), OnTrackClickListener {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
        const val TRACK_LIST_KEY = "trackList"
    }

    private lateinit var adapter: TrackAdapter
    private var isBottomNavVisible = true
    private val inputEditText: TextInputEditText by lazy { findViewById(R.id.inputEditText) }
    private val clearIcon: ImageView by lazy { findViewById(R.id.clearIcon) }
    private val searchInputLayout: TextInputLayout by lazy { findViewById(R.id.search_box) }
    private var searchQuery = ""
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.tracks_recycler_view) }
    private lateinit var apiService: ITunesApi
    private var trackList: MutableList<Track> = mutableListOf()
    private val textView: TextView by lazy { findViewById(R.id.fail) }
    private val history: TextView by lazy { findViewById(R.id.history) }
    private val update: MaterialButton by lazy { findViewById(R.id.btnUpdate) }
    private var isHistory = false
    private lateinit var searchHistory: SearchHistory
    private lateinit var errorManager : ErrorManager
    private var isErrorShown = false
    private var isFailureShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchHistory = SearchHistory(getSharedPreferences("app_prefs", Context.MODE_PRIVATE),
            history,update)
        errorManager = ErrorManager(textView, update, recyclerView)

        searchHistory.loadHistory()

        setupListeners()
        findViewById<TextView>(R.id.bottom1).isSelected = true

        apiService = ITunesApi.create()

        recyclerView.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")

            val jsonTrackList = savedInstanceState.getString(TRACK_LIST_KEY, "")
            if (jsonTrackList.isNotEmpty()) {
                trackList = Gson().fromJson(jsonTrackList, Array<Track>::class.java).toMutableList()
            }
        }

        adapter = TrackAdapter(trackList, this, this)
        recyclerView.adapter = adapter

        updateUI()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        if (isHistory) {
            adapter.updateTracks(searchHistory.trackHistoryList)
        } else {
            adapter.updateTracks(trackList)
        }
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putString(TRACK_LIST_KEY, Gson().toJson(trackList))
        outState.putBoolean("isHistory", isHistory)
        outState.putBoolean("isErrorShown", isErrorShown)
        outState.putBoolean("isFailureShown", isFailureShown)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
        inputEditText.setText(searchQuery)

        val jsonTrackList = savedInstanceState.getString(TRACK_LIST_KEY, "")
        if (jsonTrackList.isNotEmpty()) {
            trackList = Gson().fromJson(jsonTrackList, Array<Track>::class.java).toMutableList()
            adapter = TrackAdapter(trackList, this, this)
            recyclerView.adapter = adapter
        }

        isErrorShown = savedInstanceState.getBoolean("isErrorShown", false)
        isFailureShown = savedInstanceState.getBoolean("isFailureShown", false)

        if (isErrorShown) {
            errorManager.showError()
        } else if (isFailureShown) {
            errorManager.showFailure()
        }

        searchHistory.loadHistory()
        if (isHistory && searchHistory.trackHistoryList.isNotEmpty()) {
            adapter.updateTracks(searchHistory.trackHistoryList)
            searchHistory.showHistory()

        }
    }

    override fun onTrackClicked(track: Track) {
        searchHistory.addTrackToHistory(track)
    }

    override fun onArrowClicked(track: Track) {
        if (isHistory){
            val position = searchHistory.trackHistoryList.indexOf(track)
            if (position >= 0) {
                searchHistory.trackHistoryList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, searchHistory.trackHistoryList.size)
                searchHistory.saveHistory()
            }
        } else {
            val position = trackList.indexOf(track)
            if (position >= 0) {
                trackList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, trackList.size)
//                searchHistory.addTrackToHistory(track)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        searchHistory.saveHistory()
    }

    private fun setupListeners() {
        inputEditText.addTextChangedListener(createTextWatcher())
        clearIcon.setOnClickListener { clearSearchInput() }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            searchInputLayout.hint = if (hasFocus) null else getString(R.string.search)
            if (hasFocus) {
                hideBottomNavigation()
                isHistory = true
                if (searchHistory.trackHistoryList.isNotEmpty()){
                    adapter.updateTracks(searchHistory.trackHistoryList)
                    searchHistory.showHistory()
                }
            } else {
                showBottomNavigation()
                if (isHistory) {
                    isHistory = false
                    searchHistory.hideHistory()
                }
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                isHistory = false
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = inputEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            searchQuery = query

//            if (!NetworkUtils.isNetworkAvailable(this)) {
//                // extra check for network (for future)
//                return
//            }

            apiService.search(query).enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        trackList = response.body()!!.results
                        if (trackList.isEmpty()){
                            isErrorShown = true
                            isFailureShown = false
                            errorManager.showError()
                        } else {
                            isErrorShown = false
                            isFailureShown = false
                            errorManager.hideError()
                            adapter.updateTracks(trackList)
                        }
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    isFailureShown = true
                    isErrorShown = false
                    errorManager.showFailure()
                    update.setOnClickListener {
                        trackList.clear()
                        searchQuery.let { query ->
                            inputEditText.setText(query)

                            performSearch()

                            isHistory = false
                            textView.visibility = GONE
                            searchHistory.hideHistory()
                        }
                    }
                }
            })
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                clearIcon.visibility = if (!s.isNullOrEmpty()) VISIBLE else GONE
                if (!s.isNullOrEmpty()) {
                    isHistory = false
                    searchHistory.hideHistory()
                }

            }
            override fun afterTextChanged(s: Editable?) { }
        }
    }

    private fun clearSearchInput() {
        inputEditText.text?.clear()
        clearIcon.visibility = GONE
        trackList.clear()
        adapter.updateTracks(trackList)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reverseList() {
        val currentTracksList = adapter.currentTracks
        currentTracksList.reverse()
        adapter.notifyDataSetChanged()
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.search) { navigateToMainActivity() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search
    }

    override fun getMainLayoutId(): Int {
        return R.id.main
    }

    fun getAdapter(): TrackAdapter {
        return adapter
    }

}