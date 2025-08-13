package com.example.playlistmaker.ui.movie.movieCast

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentMoviesCastBinding
import com.example.playlistmaker.presentation.movieViewModels.movieCast.MoviesCastState
import com.example.playlistmaker.presentation.movieViewModels.movieCast.MoviesCastViewModel
import com.example.playlistmaker.ui.cast.movieCastHeaderDelegate
import com.example.playlistmaker.ui.cast.movieCastPersonDelegate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MoviesCastFragment : Fragment() {

    companion object {
        const val TAG = "MoviesCastFragment"
        private const val ARGS_MOVIE_ID = "movie_id"

        fun newInstance(movieId: String): Fragment =
            MoviesCastFragment().apply {
                arguments = bundleOf(ARGS_MOVIE_ID to movieId)
            }
    }

    private val moviesCastViewModel: MoviesCastViewModel by viewModel {
        parametersOf(requireArguments().getString(ARGS_MOVIE_ID).orEmpty())
    }

    private val adapter = ListDelegationAdapter(
        movieCastHeaderDelegate(),
        movieCastPersonDelegate(),
    )

    private var _binding: FragmentMoviesCastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesCastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.moviesCastRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.moviesCastRecyclerView.adapter = adapter

        // 🔄 собираем StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                moviesCastViewModel.state.collect { st ->
                    when (st) {
                        is MoviesCastState.Loading -> showLoading()
                        is MoviesCastState.Error   -> showError(st)
                        is MoviesCastState.Content -> showContent(st)
                    }
                }
            }
        }

        // (опционально) если нужно принудительно обновлять по возврату:
        // viewLifecycleOwner.lifecycleScope.launch { moviesCastViewModel.refresh() }
    }

    private fun showLoading() {
        binding.contentContainer.isVisible = false
        binding.errorMessageTextView.isVisible = false
        binding.progressBar.isVisible = true
    }

    private fun showError(state: MoviesCastState.Error) {
        binding.contentContainer.isVisible = false
        binding.progressBar.isVisible = false
        binding.errorMessageTextView.isVisible = true
        binding.errorMessageTextView.text = state.message
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showContent(state: MoviesCastState.Content) {
        binding.progressBar.isVisible = false
        binding.errorMessageTextView.isVisible = false
        binding.contentContainer.isVisible = true

        binding.movieTitle.text = state.fullTitle
        adapter.items = state.items
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
