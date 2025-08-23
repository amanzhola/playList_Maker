package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var scrollTopOnce = false
    private var pendingSnackbarName: String? = null

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModel()
    private val imageLoader: ImageLoader by inject()

    private val adapter by lazy {
        PlaylistGridAdapter(imageLoader) { /* no-op */ }
            .apply {
                stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.setFragmentResultListener("playlist_scroll_top", this) { _, b ->
            scrollTopOnce = b.getBoolean("scrollTop", false)
            pendingSnackbarName = b.getString("name")
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

        // fail подключён через <include>
        val failTextView: TextView = view.findViewById(R.id.fail)
        val topPadding = resources.getDimensionPixelSize(R.dimen.track_45)
        failTextView.setPadding(0, topPadding, 0, 0)
        failTextView.text = getString(R.string.noPlayList)
        failTextView.isEnabled = true
        failTextView.visibility = View.VISIBLE

        // Кнопка «Новый плейлист»
        binding.btnUpdate.text = getString(R.string.newPlaylist)
        binding.btnUpdate.visibility = View.VISIBLE
        binding.btnUpdate.setOnClickListener {

            findNavController().navigate(R.id.action_global_to_createPlaylistFragment,
                bundleOf("from_playlist" to true))
        }

        // RecyclerView
        val lm = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.layoutManager = lm
        binding.rvPlaylists.adapter = adapter


        val space = resources.getDimensionPixelSize(R.dimen.line_margin)
        binding.rvPlaylists.addItemDecoration(SpacesItemDecoration(space))

        // Snackbar после создания плейлиста (без скролла)
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                showCreationSnackbar(name)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
            }

        // Подписка на список плейлистов
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collect { list ->

                    adapter.submitList(list.toList()) {

                        if (scrollTopOnce) {
                            binding.rvPlaylists.stopScroll()
                            lm.scrollToPositionWithOffset(0, 0)
                            binding.rvPlaylists.post { lm.scrollToPositionWithOffset(0, 0) }
                            scrollTopOnce = false

                            pendingSnackbarName?.let { showCreationSnackbar(it) }
                            pendingSnackbarName = null
                        }
                    }

                    val isEmpty = list.isEmpty()
                    binding.rvPlaylists.isVisible = !isEmpty
                    failTextView.isVisible = isEmpty
                    binding.btnUpdate.isVisible = true
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
        sb.duration = durationMs
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
