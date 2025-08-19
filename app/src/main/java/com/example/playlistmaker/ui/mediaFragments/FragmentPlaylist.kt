package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.SpacesItemDecoration
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import com.example.playlistmaker.utils.NavKeys
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentPlaylist : Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModel()
    private val imageLoader: ImageLoader by inject()

    private val adapter by lazy {
        PlaylistGridAdapter(imageLoader) { playlist ->
            // По ТЗ переход в детали пока не нужен.
            // Оставляем заглушку либо ничего не делаем.
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // fail подключён через <include>, нужно использовать findViewById
        val failTextView: TextView = view.findViewById(R.id.fail)

        val topPadding = resources.getDimensionPixelSize(R.dimen.track_45)
        failTextView.setPadding(0, topPadding, 0, 0)

        // ✅ Настройка fail
        failTextView.text = getString(R.string.noPlayList)
        failTextView.isEnabled = true
        failTextView.visibility = View.VISIBLE

        // ✅ Настройка btnUpdate — доступен через binding
        binding.btnUpdate.text = getString(R.string.newPlaylist)
        binding.btnUpdate.visibility = View.VISIBLE

        // ✅ Переход на экран создания плейлиста
        binding.btnUpdate.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_createPlaylistFragment)
        }

        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                showCreationSnackbar(name)
                handle.remove<String>(NavKeys.PLAYLIST_CREATED_NAME) // очистить, чтобы не повторялось
            }

        // RecyclerView: 2 колонки
        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.adapter = adapter
        // красота:
        val space = resources.getDimensionPixelSize(R.dimen.line_margin)
        binding.rvPlaylists.addItemDecoration(SpacesItemDecoration(space))

        // Подписка на список плейлистов
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collect { list ->

                    Log.d("PlaylistUI", "render list size=${list.size}")

                    adapter.submitList(list)
                    val isEmpty = list.isEmpty()
                    // показать список или заглушку
                    binding.rvPlaylists.isVisible = !isEmpty
                    failTextView.isVisible = isEmpty
                    binding.btnUpdate.isVisible = true // кнопка «Новый плейлист» видна всегда по ТЗ
                }
            }
        }
    }

    private fun showCreationSnackbar(name: String, durationMs: Int = 4000) {
        val root = requireActivity().findViewById<View>(android.R.id.content)
        val msg = getString(R.string.playlist_created, name)

        val sb = com.google.android.material.snackbar.Snackbar
            .make(root, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)

        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)

        sb.duration = durationMs      // ← вот здесь ставим > 4 секунд, например 7000 или 10000
        sb.show()
    }

    companion object {

        fun newInstance(args: Bundle?): FragmentPlaylist {
            val fragment = FragmentPlaylist()
            fragment.arguments = args
            return fragment
        }
    }
}
