package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.min


class SearchActivity : BaseActivity(), OnTrackClickListener {

    companion object {
        const val SEARCH_QUERY_KEY = "searchQuery"
        const val TRACK_LIST_KEY = "trackList"
    }

    private lateinit var adapter: TrackAdapter
    private var isBottomNavVisible: Boolean = true
    private lateinit var inputEditText: TextInputEditText
    private lateinit var clearIcon: ImageView
    private lateinit var searchInputLayout: TextInputLayout
    private var searchQuery = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var apiService: ITunesApi
    private var trackList: MutableList<Track> = mutableListOf()
    private lateinit var textView: TextView
    private lateinit var history: TextView
    private lateinit var update: MaterialButton
    private var isErrorVisible = false
    private var isErrorTypeNoResults: Boolean = false
    private var isHistory: Boolean = false
    private lateinit var searchHistory: SearchHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchHistory = SearchHistory(getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
        searchHistory.loadHistory()

        initViews()
        setupListeners()
        findViewById<TextView>(R.id.bottom1).isSelected = true

        apiService = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)

        recyclerView = findViewById(R.id.tracks_recycler_view)
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

    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
        outState.putString(TRACK_LIST_KEY, Gson().toJson(trackList))
        outState.putBoolean("isErrorVisible", isErrorVisible)
        outState.putBoolean("isErrorTypeNoResults", isErrorTypeNoResults)
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
        isErrorVisible = savedInstanceState.getBoolean("isErrorVisible", false)
        isErrorTypeNoResults = savedInstanceState.getBoolean("isErrorTypeNoResults", false)
        if (isErrorVisible){
            showErrorPlaceholder(isErrorTypeNoResults)
        }
    }

    override fun onTrackClicked(track: Track) {
        val position = trackList.indexOf(track)
        if (position >= 0) {
            trackList.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, trackList.size)
            if (!isHistory) addTrackToHistory(track)
        }
    }

    private fun addTrackToHistory(newTrack: Track) {
        searchHistory.addTrackToHistory(newTrack)
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
                showHistory()

            } else {
                showBottomNavigation()

                if (isHistory) {
                    isHistory = false
                    showHistory()
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

            if (!isNetworkAvailable(this)) {
                clearTracksAndShowError(false)
                showErrorPlaceholder(false)
                return
            }

            apiService.search(query).enqueue(object : Callback<SearchResponse> {
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        trackList = response.body()!!.results
                        updateRecyclerView()
                    } else {
                        clearTracksAndShowError(trackList.isEmpty())
                    }
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    clearTracksAndShowError(false)
                }
            })
        }
    }

    private fun updateRecyclerView() {
        if (trackList.isEmpty()) {
            clearTracksAndShowError(true)
        } else {
            isErrorVisible = false
            textView.visibility = View.GONE
            update.visibility = View.GONE

            adapter.updateTracks(trackList)
        }
    }

    private fun clearTracksAndShowError(isNoResults: Boolean) {
        trackList.clear()
        adapter.updateTracks(trackList)
        showErrorPlaceholder(isNoResults)
    }

    private fun showErrorPlaceholder(isNoResults: Boolean) {
        isErrorVisible = true
        isErrorTypeNoResults = isNoResults

        textView.visibility = VISIBLE

        if (isNoResults) {
            textView.isEnabled = true
            textView.setText(R.string.searchFail)
            update.visibility = View.GONE
        } else {
            textView.isEnabled = false
            textView.setText(R.string.networkFail)

            update.visibility = VISIBLE
            update.setText(R.string.update)

            update.setOnClickListener {
                trackList.clear()
                searchQuery.let { query ->
                    inputEditText.setText(query)
                    isHistory = false
                    performSearch()
                }
            }
        }
    }


    private fun showHistory() {
        if (isHistory && searchHistory.trackHistoryList.isNotEmpty()) {
            history.visibility = VISIBLE
            update.visibility = VISIBLE

            adapter.updateTracks(searchHistory.trackHistoryList)
            setRecyclerViewHeightBasedOnItems()

            update.setOnClickListener {
                searchHistory.clearHistory()
                showHistory()
            }
        } else {
            history.visibility = View.GONE
            update.visibility = View.GONE
            resetRecyclerViewHeight()
        }
    }

    private fun setRecyclerViewHeightBasedOnItems() {
        val itemCount = searchHistory.trackHistoryList.size
        if (itemCount == 0) {
            resetRecyclerViewHeight()
            return
        }

        val itemView = layoutInflater.inflate(R.layout.track_item, recyclerView, false)
        itemView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val itemHeight = itemView.measuredHeight

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay

        val displayMetrics = DisplayMetrics()
        display.getRealMetrics(displayMetrics)

        val maxHeight = (displayMetrics.heightPixels * 0.5).toInt()
        val maxVisibleItemsByHeight: Int = maxHeight / itemHeight
        val maxVisibleItems: Int = min(itemCount, maxVisibleItemsByHeight)

        val layoutParams = recyclerView.layoutParams
        layoutParams.height = itemHeight * maxVisibleItems
        recyclerView.layoutParams = layoutParams
    }

    private fun resetRecyclerViewHeight() {
        val layoutParams = recyclerView.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        recyclerView.layoutParams = layoutParams
    }


    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                clearIcon.visibility = if (!s.isNullOrEmpty()) VISIBLE else View.GONE
                if (!s.isNullOrEmpty()) {
                    isHistory = false
                    showHistory()
                }

            }
            override fun afterTextChanged(s: Editable?) { }
        }
    }

    private fun clearSearchInput() {
        inputEditText.text?.clear()
        clearIcon.visibility = View.GONE
        hideKeyboard()

        trackList.clear()
        adapter.updateTracks(trackList)

        isErrorVisible = false
        textView.visibility = View.GONE
        update.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun initViews() {
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        searchInputLayout = findViewById(R.id.search_box)
        textView = findViewById(R.id.fail)
        update = findViewById(R.id.btnUpdate)
        history = findViewById(R.id.history)
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

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && (networkCapabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

}