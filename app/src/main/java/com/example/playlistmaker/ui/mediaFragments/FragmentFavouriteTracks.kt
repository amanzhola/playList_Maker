package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.example.playlistmaker.ui.audio.OnTrackClickListener
import com.example.playlistmaker.ui.audio.TrackAdapter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FragmentFavouriteTracks : Fragment(), OnTrackClickListener {

    private lateinit var binding: FragmentFavouriteTracksBinding
    private val viewModel: FavoriteTracksViewModel by viewModel()

    private val resourceColorProvider: ResourceColorProvider by inject { parametersOf(requireContext()) }
    private val networkChecker: NetworkStatusChecker by inject { parametersOf(requireContext()) }

    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val failTextView: TextView = view.findViewById(R.id.fail)
        failTextView.text = getString(R.string.emptyMedia)

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TrackAdapter(
            mutableListOf(),
            resourceColorProvider,
            networkChecker,
            this,
            R.id.action_global_to_extraOptionFragment
        )
        binding.tracksRecyclerView.adapter = adapter

        // ✅ Коллекция StateFlow c repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // пока isLoading — ничего «пустого» не показываем
                    val showEmpty = state.isEmpty
                    val showList = !state.isLoading && !showEmpty

                    binding.tracksRecyclerView.isVisible = showList
                    failTextView.isVisible = showEmpty
                    failTextView.isEnabled = showEmpty

                    if (showList) {
                        adapter.updateTracks(state.tracks.toMutableList())
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Можно оставить, если хочешь явно триггерить обновление при возврате на экран.
        viewModel.reloadFavorites()
    }

    // OnTrackClickListener
    override fun onTrackClicked(track: Track) { /* навигация уже в адаптере */ }
    override fun onArrowClicked(track: Track) { /* по желанию: удаление из избранного */ }

    companion object {
        fun newInstance(args: Bundle?): FragmentFavouriteTracks {
            val fragment = FragmentFavouriteTracks()
            fragment.arguments = args
            return fragment
        }
    }
}
