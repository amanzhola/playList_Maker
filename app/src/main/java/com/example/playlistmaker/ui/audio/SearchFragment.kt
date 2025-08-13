package com.example.playlistmaker.ui.audio

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioTracksShare
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.searchViewModels.ErrorState
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import com.example.playlistmaker.presentation.utils.AudioErrorManager
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment : BaseFragment(), OnTrackClickListener, BottomNavConfig, ReversableList {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter
    private var isBottomNavVisible = true

    private lateinit var errorManager: AudioErrorManager
    private val viewModel: SearchViewModel by viewModel()
    private val resourceColorProvider: ResourceColorProvider by inject { parametersOf(requireContext()) }
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }
    private val trackShareService: AudioTracksShare by inject { parametersOf(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val failView = binding.failText.fail
        errorManager = getKoin().get {
            parametersOf(failView, binding.btnUpdate, binding.tracksRecyclerView)
        }

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TrackAdapter(mutableListOf(), resourceColorProvider, networkChecker, this)

        binding.tracksRecyclerView.adapter = adapter

        setupObservers()
        setupListeners()

        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // Фон тулбара
        val blueColor = ContextCompat.getColor(requireContext(), R.color.white_textColor)
        getBaseActivity()?.toolbarHelper?.setToolbarBackgroundColor(blueColor)

        // Цвет заголовка
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.textColor_white)
        getBaseActivity()?.toolbarHelper?.setTitleTextColor(whiteColor)

    }

    override fun getBottomNavButtonIndex(): Int = 0
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = true

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading

                    // 👇 используем метод для синхронизации ⬇️ 🚗 💖
                    val shouldShowBottomNav = state.query.isEmpty()
                    updateBottomNavVisibility(shouldShowBottomNav)

                    when (state.error) {
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

                    binding.btnUpdate.text = getString(
                        if (state.error == ErrorState.NONE) R.string.clean else R.string.update
                    )

                    // 🎯 рисуем ровно то, что посчитал VM
                    adapter.updateTracks(state.displayedTracks.toMutableList())

                    binding.history.isVisible = state.showHistory
                    binding.searchBox.hint =
                        if (state.query.isNotEmpty() || state.isInputFocused) null
                        else getString(R.string.search_hint)

                    binding.clearIcon.isVisible = state.isClearIconVisible
                }
            }
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

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    override fun onTrackClicked(track: Track) = viewModel.onTrackClicked(track)
    override fun onArrowClicked(track: Track) = viewModel.removeTrack(track)

    override fun reverseList() {
        adapter.reverseTracks()
        binding.tracksRecyclerView.scrollToPosition(0)
    }

    override fun onSegment4ClickedInternal() {
        updateBottomNavVisibility(!isBottomNavVisible)
    }

    private fun updateBottomNavVisibility(show: Boolean) {
        if (isBottomNavVisible != show) {
            if (show) {
                getBaseActivity()?.showBottomNavigation()
            } else {
                getBaseActivity()?.hideBottomNavigation()
            }
            isBottomNavVisible = show
        }
    }

    fun shareTrackHistoryFromViewModel() {
        val tracks = viewModel.getTrackHistoryList()
        trackShareService.shareTracks(tracks, R.string.history_track)
    }

    fun getAdapter(): TrackAdapter = adapter // 🔝 🎨

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(GONE, R.string.search) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1 // 🔹 явно переключаем индекс
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }

    override fun onResume() {
        super.onResume()
        requireActivity()
            .findViewById<TextView>(R.id.title)
            ?.visibility = View.VISIBLE // Явно показываем заголовок

        val backStackEntry = findNavController().currentBackStackEntry
        val fromExtra = backStackEntry?.savedStateHandle?.get<Boolean>("from_extra") == true

        if (fromExtra) {
            backStackEntry?.savedStateHandle?.remove<Boolean>("from_extra")

            val query = viewModel.uiState.value?.query.orEmpty()

            if (query.isNotEmpty()) {
                // 👇 Логика осталась: если был запрос — скрываем навигатор
                getBaseActivity()?.hideBottomNavigation()
            } else {
                // 👇 Если запроса нет — показываем историю
                viewModel.setInputFocused(true)
            }
        }

        (activity as? BaseActivity)?.updateSegmentTexts()
    }
}
