package com.example.playlistmaker.ui.mediaFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.presentation.media.FavouriteTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentFavouriteTracks : Fragment() {

    private lateinit var binding: FragmentFavouriteTracksBinding
    private val viewModel: FavouriteTracksViewModel by viewModel()

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
        failTextView.isEnabled = true
        failTextView.visibility = View.VISIBLE
    }

    companion object {

        fun newInstance(args: Bundle?): FragmentFavouriteTracks {
            val fragment = FragmentFavouriteTracks()
            fragment.arguments = args
            return fragment
        }
    }
}
