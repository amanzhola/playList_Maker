package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentPlaylist : Fragment() {

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModel()

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
    }

    companion object {

        fun newInstance(args: Bundle?): FragmentPlaylist {
            val fragment = FragmentPlaylist()
            fragment.arguments = args
            return fragment
        }
    }
}
