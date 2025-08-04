package com.example.playlistmaker.ui.movie.movieHistory

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.FragmentHistoryBinding
import com.example.playlistmaker.domain.models.movie.Movie
import com.example.playlistmaker.presentation.movieViewModels.movieHistory.HistoryState
import com.example.playlistmaker.presentation.movieViewModels.movieHistory.HistoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    private val viewModel by viewModel<HistoryViewModel>()
    private var adapter: HistoryAdapter? = null

    private lateinit var placeholderMessage: TextView
    private lateinit var historyList: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter()

        placeholderMessage = binding.placeholderMessage
        historyList = binding.historyList
        progressBar = binding.progressBar

        historyList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        historyList.adapter = adapter

        viewModel.fillData()

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        historyList.adapter = null
    }

    private fun render(state: HistoryState) {
        when (state) {
            is HistoryState.Content -> showContent(state.movies)
            is HistoryState.Empty -> showEmpty(state.message)
            is HistoryState.Loading -> showLoading()
        }
    }

    private fun showLoading() {
        historyList.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showEmpty(message: String) {
        historyList.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        placeholderMessage.text = message
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showContent(movies: List<Movie>) {
        historyList.visibility = View.VISIBLE
        placeholderMessage.visibility = View.GONE
        progressBar.visibility = View.GONE

        adapter?.movies?.clear()
        adapter?.movies?.addAll(movies)
        adapter?.notifyDataSetChanged()
    }
}