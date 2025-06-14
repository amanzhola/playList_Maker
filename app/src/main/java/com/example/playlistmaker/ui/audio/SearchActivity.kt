package com.example.playlistmaker.ui.audio

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.searchViewModels.ErrorState
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import com.example.playlistmaker.presentation.utils.AudioErrorManager
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchActivity : BaseActivity(), OnTrackClickListener {

    private lateinit var binding: ActivitySearchBinding
    private val failTextView: TextView by lazy { findViewById(R.id.fail) }
    private lateinit var adapter: TrackAdapter
    private var isBottomNavVisible = true

    private lateinit var errorManager: AudioErrorManager // 🐞➖ ➡️ 🚫 ➡️ 😎 (binding.btnUpdate)
    private val viewModel: SearchViewModel by viewModel()
    private val resourceColorProvider: ResourceColorProvider by inject { parametersOf(this) } // 🎨
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(this) } // 🌐
    private val trackShareService: AudioTracksShare by inject { parametersOf(this) } // 👨‍💻


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.bind(findViewById(getMainLayoutId()))
        setContentView(binding.root)

        errorManager = getKoin().get { parametersOf(failTextView, binding.btnUpdate, binding.tracksRecyclerView) }
        binding.root.findViewById<View>(R.id.bottom1).isSelected = true // ⬇️ 🚗
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(mutableListOf(), resourceColorProvider, networkChecker, this)
        binding.tracksRecyclerView.adapter = adapter

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading /* 🔍 Прогресс */

            when (state.error) { /* 🧼 Ошибки */
                ErrorState.ERROR -> errorManager.showError()
                ErrorState.FAILURE -> {
                    errorManager.showFailure()
                    binding.btnUpdate.setOnClickListener {
                        viewModel.onSearchActionDone()
                        errorManager.hideError()
                    }
                }
                ErrorState.NONE -> {
                    errorManager.hideError()
                    binding.btnUpdate.isVisible = state.showHistory
                    binding.btnUpdate.setOnClickListener {
                        if (state.showHistory) viewModel.clearHistory()
                    }
                }
            }

            val buttonText = if (state.error == ErrorState.NONE) R.string.clean else R.string.update
            binding.btnUpdate.text = getString(buttonText)  /* 🔁 Текст 🧹 🛠 🔄 ✍️ */

            adapter.updateTracks(state.displayedTracks.toMutableList()) /* 🧾 Список треков */

            binding.history.isVisible = state.showHistory /* 🔙 История */
            binding.searchBox.hint = if (state.isInputFocused) null else getString(R.string.search_hint) /* 🧼 Input hint */
            binding.clearIcon.isVisible = state.isClearIconVisible  /* ❌ Крестик  */

            if (state.isInputFocused) hideBottomNavigation() else showBottomNavigation()  /* ⬇️ Bottom nav 🚗 */
        }
    }

    private fun setupListeners() {
        binding.inputEditText.addTextChangedListener(createTextWatcher())
        binding.clearIcon.setOnClickListener { clearSearchInput() }

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.setInputFocused(hasFocus)
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onQueryChanged(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun clearSearchInput() {
        viewModel.clearSearchInput()
        binding.inputEditText.text?.clear()
        hideKeyboard()
    }

    private fun hideKeyboard() { // 🔠 🔤
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    override fun onTrackClicked(track: Track) = viewModel.onTrackClicked(track) // ✅🐛 ➖🔠
    override fun onArrowClicked(track: Track) = viewModel.removeTrack(track) // 🧼➖🧹

    override fun reverseList() {
        adapter.reverseTracks()
        binding.tracksRecyclerView.scrollToPosition(0)
    }

    override fun onSegment4Clicked() { // ⬇️ 🚗  💖
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.search) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_search
    override fun getMainLayoutId(): Int = R.id.main
    fun getAdapter(): TrackAdapter = adapter // 🔝 🎨

    fun shareTrackHistoryFromViewModel() { // 💖  🔝 ⭐
        val tracks = viewModel.getTrackHistoryList()
        trackShareService.shareTracks(tracks, R.string.history_track)
    }
}