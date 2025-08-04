package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.example.playlistmaker.ui.audio.OnTrackClickListener
import com.example.playlistmaker.ui.audio.TrackAdapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class FragmentFavouriteTracks : Fragment(), OnTrackClickListener {

    private lateinit var binding: FragmentFavouriteTracksBinding
    private val viewModel: FavoriteTracksViewModel by viewModel()

    // Внедрение зависимостей через Koin
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

        // Заглушка
        val failTextView: TextView = view.findViewById(R.id.fail)
        failTextView.text = getString(R.string.emptyMedia)

        binding.tracksRecyclerView .layoutManager = LinearLayoutManager(requireContext())

        // Инициализация адаптера (как в SearchFragment)
        adapter = TrackAdapter(mutableListOf(), resourceColorProvider, networkChecker, this)

        // ✅ передаем правильный actionId для MediaLibraryFragment -> ExtraOptionFragment
        adapter = TrackAdapter(
            mutableListOf(),
            resourceColorProvider,
            networkChecker,
            this,
            R.id.action_global_to_extraOptionFragment // ✅ теперь глобальный action
        )
        binding.tracksRecyclerView .adapter = adapter

        // Подписка на данные
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isEmpty) {
                binding.tracksRecyclerView .visibility = View.GONE
                failTextView.visibility = View.VISIBLE
                failTextView.isEnabled = true
            } else {
                binding.tracksRecyclerView .visibility = View.VISIBLE
                failTextView.visibility = View.GONE
                failTextView.isEnabled = false
                adapter.updateTracks(state.tracks.toMutableList())
            }
        }
    }

    // Реализация OnTrackClickListener
    override fun onTrackClicked(track: Track) {
        // Ничего не делаем — навигация в ExtraOption уже есть в адаптере (binding.root.setOnClickListener)
    }

    override fun onArrowClicked(track: Track) {
        // В избранных можно, например, удалять трек при клике на стрелку (по желанию)
    }

    companion object {
        fun newInstance(args: Bundle?): FragmentFavouriteTracks {
            val fragment = FragmentFavouriteTracks()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadFavorites()
    }

}
