package com.example.playlistmaker.ui.movie.movieNames

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.databinding.FragmentNamesBinding
import com.example.playlistmaker.domain.models.moviePerson.Person
import com.example.playlistmaker.presentation.movieViewModels.movieNames.NamesState
import com.example.playlistmaker.presentation.movieViewModels.movieNames.NamesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NamesFragment : Fragment() {

    private val viewModel by viewModel<NamesViewModel>()

    private val adapter = PersonsAdapter()

    private var _binding: FragmentNamesBinding? = null
    private val binding get() = _binding!!

    private var textWatcher: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ♻️ список
        binding.personsList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.personsList.adapter = adapter

        // ⌨️ TextWatcher — просто прокидываем ввод; дебаунс внутри VM
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchDebounce(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        }.also { binding.queryInput.addTextChangedListener(it) }

        // 🔄 Подписка на UI-состояние
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }

        // 🔔 Одноразовые сообщения (вместо SingleLiveEvent)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toasts.collect { msg ->
                    msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
                }
            }
        }
    }

    override fun onDestroyView() {
        // снимаем слушатель с editText, чистим биндинг
        textWatcher?.let { binding.queryInput.removeTextChangedListener(it) }
        textWatcher = null
        _binding = null
        super.onDestroyView()
    }

    // ─── UI helpers ───

    private fun render(state: NamesState) {
        when (state) {
            is NamesState.Loading -> showLoading()
            is NamesState.Error -> showError(state.message)
            is NamesState.Empty -> showEmpty(state.message)
            is NamesState.Content -> showContent(state.persons)
        }
    }

    private fun showLoading() = with(binding) {
        personsList.visibility = View.GONE
        placeholderMessage.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) = with(binding) {
        personsList.visibility = View.GONE
        progressBar.visibility = View.GONE
        placeholderMessage.visibility = View.VISIBLE
        placeholderMessage.text = errorMessage
    }

    private fun showEmpty(emptyMessage: String) {
        showError(emptyMessage)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showContent(persons: List<Person>) {
        binding.progressBar.visibility = View.GONE
        binding.placeholderMessage.visibility = View.GONE
        binding.personsList.visibility = View.VISIBLE

        adapter.persons.clear()
        adapter.persons.addAll(persons)
        adapter.notifyDataSetChanged()
    }
}
