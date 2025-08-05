package com.example.playlistmaker.ui.audioPosters

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentExtraOptionBinding
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.AudioSingleTrackShare
import com.example.playlistmaker.domain.repository.base.TrackListIntentParser
import com.example.playlistmaker.presentation.searchPostersViewModels.ExtraOptionViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ExtraOptionFragment : BaseFragment(), BottomNavConfig {

    private lateinit var binding: FragmentExtraOptionBinding
    private lateinit var adapter: TrackAdapterAudio
    private val viewModel: ExtraOptionViewModel by viewModel()
    private lateinit var snapHelper: PagerSnapHelper

    private val shareHelper: AudioSingleTrackShare by inject { parametersOf(requireActivity()) }
    private val trackListIntentParser: TrackListIntentParser by inject()

    private var currentLayoutOrientation = LinearLayoutManager.HORIZONTAL
    private var isFromSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromSearch = arguments?.getBoolean("IS_FROM_SEARCH", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExtraOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val whiteColor = ContextCompat.getColor(requireContext(), R.color.textColor_white)
        val backgroundColor = ContextCompat.getColor(requireContext(), R.color.white_textColor)

        getBaseActivity()?.toolbarHelper?.apply {
            setTitleTextColor(whiteColor)
            setToolbarBackgroundColor(backgroundColor)
        }

        // Инициализация адаптера
        adapter = TrackAdapterAudio(emptyList(), object : OnTrackAudioClickListener {
            override fun onTrackClicked(track: Track, position: Int) {
                viewModel.setCurrentTrackIndex(position)
                viewModel.toggleIsHorizontal()
                viewModel.setScrollPosition(position)
            }

            override fun onPlayButtonClicked(track: Track) {
                viewModel.audioPlay(track)
            }

            override fun onBackArrowClicked() {
                viewModel.stopAudioPlay()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            // 🆕 Обработка клика по "Избранному"
            override fun onFavoriteClicked(track: Track) {
                viewModel.onFavoriteClicked()
            }
        })

        binding.tracksRecyclerView.adapter = adapter
        snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(binding.tracksRecyclerView) }
        setLayoutManager(currentLayoutOrientation)

        // Подписка на состояние ViewModel
        viewModel.state.observe(viewLifecycleOwner) { state ->

            // Обновляем список треков
            if (adapter.getItems() != state.trackList) {
                adapter.update(state.trackList.map { it.copy() })
                binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
            }

            // Переключаем ориентацию списка (горизонтальная/вертикальная)
            val desiredOrientation = if (state.isHorizontal) LinearLayoutManager.HORIZONTAL
            else LinearLayoutManager.VERTICAL

            if (desiredOrientation != currentLayoutOrientation) {
                currentLayoutOrientation = desiredOrientation
                setLayoutManager(desiredOrientation)
                binding.tracksRecyclerView.scrollToPosition(state.currentTrackIndex)
            }

            // Управление видимостью элементов
            binding.tracksRecyclerView.visibility =
                if (state.isBottomNavVisible) View.GONE else View.VISIBLE

            requireActivity().findViewById<TextView>(R.id.title)?.visibility =
                if (state.isBottomNavVisible) View.VISIBLE else View.INVISIBLE

            requireActivity().findViewById<Toolbar>(R.id.toolbar)?.apply {
                val fixedHeightInPx = 45.convertDpToPx(requireContext())
                layoutParams.height = fixedHeightInPx
                requestLayout()
            }
        }

        // Следим за скроллом и обновляем индекс трека
        binding.tracksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                            ?: 0
                    viewModel.setCurrentTrackIndex(pos)
                    viewModel.setScrollPosition(pos)
                }
            }
        })

        // 🧠 Передача аргументов при первом запуске
        if (savedInstanceState == null) {
            arguments?.let {
                val json = it.getString("TRACK_LIST_JSON") ?: return@let
                val index = it.getInt("TRACK_INDEX")

                trackListIntentParser.parse(json, index)?.let { inputData ->
                    viewModel.initializeWith(inputData)
                }
            }
        }

        // Выделяем иконку нижнего меню
        binding.root.findViewById<View>(R.id.bottom6)?.isSelected = true
    }

    override fun onPause() {
        super.onPause()
        val pos =
            (binding.tracksRecyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                ?: 0
        viewModel.setScrollPosition(pos)
    }

    fun shareSingleTrack() {
        viewModel.getCurrentTrack()?.let { shareHelper.shareTrackOrNotify(it) }
    }

    private fun setLayoutManager(orientation: Int) {
        binding.tracksRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), orientation, false)
        snapHelper.attachToRecyclerView(binding.tracksRecyclerView)
    }

    private fun Int.convertDpToPx(context: Context): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            context.resources.displayMetrics
        ).toInt()

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(View.VISIBLE, R.string.option) {
        if (viewModel.state.value?.isBottomNavVisible == true) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        } else {
            viewModel.stopAudioPlay()
            val navController = findNavController()
//            val backStackEntry = navController.getBackStackEntry(R.id.searchFragment)
            // Проверяем, есть ли в бэкстеке searchFragment
            val backStackEntry = try {
                navController.getBackStackEntry(R.id.searchFragment)
            } catch (e: IllegalArgumentException) {
                null
            }

            backStackEntry?.savedStateHandle?.set("from_extra", true)

            // Возврат в любой предыдущий экран
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getBottomNavButtonIndex(): Int = 5
    override fun shouldShowBottomNav(): Boolean = !isFromSearch
    override fun shouldShowFullBottomNav(): Boolean = isFromSearch

    override fun onSegment4ClickedInternal() {
        val visible = viewModel.state.value?.isBottomNavVisible == true
        viewModel.updateState { it.copy(isBottomNavVisible = !visible) }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
    }
}
