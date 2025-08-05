package com.example.playlistmaker.ui.movie.movieCast

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentMoviesCastBinding
import com.example.playlistmaker.presentation.movieViewModels.movieCast.MoviesCastState
import com.example.playlistmaker.presentation.movieViewModels.movieCast.MoviesCastViewModel
import com.example.playlistmaker.ui.cast.movieCastHeaderDelegate
import com.example.playlistmaker.ui.cast.movieCastPersonDelegate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MoviesCastFragment : Fragment() {

    companion object {

        const val TAG = "MoviesCastFragment" // ← добавь это!

        private const val ARGS_MOVIE_ID = "movie_id"

        // Модифицировали метод newInstance — он должен возвращать фрагмент,
        // а не Intent
        fun newInstance(movieId: String): Fragment {
            return MoviesCastFragment().apply {
                arguments = bundleOf(
                    ARGS_MOVIE_ID to movieId
                )
            }
        }
    }

    private val moviesCastViewModel: MoviesCastViewModel by viewModel {
        // параметр movieId берём из аргументов фрагмента, а не Intent
        parametersOf(requireArguments().getString(ARGS_MOVIE_ID))
    }

    private val adapter = ListDelegationAdapter(
        movieCastHeaderDelegate(),
        movieCastPersonDelegate(),
    )

    private lateinit var binding: FragmentMoviesCastBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMoviesCastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.moviesCastRecyclerView.adapter = adapter
        binding.moviesCastRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        moviesCastViewModel.observeState().observe(viewLifecycleOwner) {
            when (it) {
                is MoviesCastState.Content -> showContent(it)
                is MoviesCastState.Error -> showError(it)
                is MoviesCastState.Loading -> showLoading()
            }
        }
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

}
