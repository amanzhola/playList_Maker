package com.example.playlistmaker.ui.movie.moviePosters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.playlistmaker.databinding.FragmentPosterBinding
import com.example.playlistmaker.presentation.movieViewModels.movieDetails.PosterViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PosterFragment : Fragment() {

    companion object {
        private const val POSTER_URL = "poster_url"

        fun newInstance(posterUrl: String) = PosterFragment().apply {
            arguments = Bundle().apply { putString(POSTER_URL, posterUrl) }
        }
    }

    // 🧠 получаем VM с параметром из аргументов
    private val posterViewModel: PosterViewModel by viewModel {
        parametersOf(requireArguments().getString(POSTER_URL).orEmpty())
    }

    private var _binding: FragmentPosterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPosterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔄 собираем StateFlow с учётом жизненного цикла
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                posterViewModel.url.collect { url ->
                    showPoster(url)
                }
            }
        }
    }

    private fun showPoster(url: String) {
        // 🖼️ грузим постер (Glide/Coil — на твой вкус)
        Glide.with(requireContext())
            .load(url)
            .into(binding.poster)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 🧹 не держим в памяти вью после уничтожения
    }
}
