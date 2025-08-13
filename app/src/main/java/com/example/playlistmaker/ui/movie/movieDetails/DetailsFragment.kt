package com.example.playlistmaker.ui.movie.movieDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentDetailsBinding
import com.example.playlistmaker.ui.movie.moviePosters.DetailsViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {

    companion object {
        private const val ARGS_MOVIE_ID = "movie_id"
        private const val ARGS_POSTER_URL = "poster_url"
        const val TAG = "DetailsFragment"

        fun newInstance(movieId: String, posterUrl: String) = DetailsFragment().apply {
            arguments = bundleOf(
                ARGS_MOVIE_ID to movieId,
                ARGS_POSTER_URL to posterUrl
            )
        }
    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private var tabsMediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🎁 аргументы
        val posterUrl = requireArguments().getString(ARGS_POSTER_URL).orEmpty()
        val movieId = requireArguments().getString(ARGS_MOVIE_ID).orEmpty()

        // 📚 ViewPager2 + Adapter
        binding.viewPager.apply {
            adapter = DetailsViewPagerAdapter(
                fragmentManager = childFragmentManager,
                lifecycle = viewLifecycleOwner.lifecycle,
                posterUrl = posterUrl,
                movieId = movieId
            )
            offscreenPageLimit = 2 // ⚡ держим обе вкладки
        }

        // 🏷️ Tabs
        tabsMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.poster)
                else -> getString(R.string.details)
            }
        }.also { it.attach() }

        // 👈 свайп-назад работает только на первой вкладке
        var downX = 0f
        var isOnFirstTab = true

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                isOnFirstTab = (position == 0)
            }
        })

        binding.root.setOnTouchListener { _, event ->
            if (!isOnFirstTab) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - downX
                    if (dx > 300f) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    } else false
                }
                else -> false
            }
        }

        // (опционально) 🔄 если нужно что-то слушать по жизненному циклу — заготовка
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // collect flows here if надо
            }
        }
    }

    override fun onDestroyView() {
        // 🧹 аккуратно отцепляем mediator и биндинг
        tabsMediator?.detach()
        tabsMediator = null
        _binding = null
        super.onDestroyView()
    }
}
