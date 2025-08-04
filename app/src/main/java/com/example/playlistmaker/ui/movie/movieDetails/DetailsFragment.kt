package com.example.playlistmaker.ui.movie.movieDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentDetailsBinding
import com.example.playlistmaker.ui.movie.moviePosters.DetailsViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class DetailsFragment : Fragment() {

    companion object {

        private const val ARGS_MOVIE_ID = "movie_id"
        private const val ARGS_POSTER_URL = "poster_url"

        // Тег для использования во FragmentManager
        const val TAG = "DetailsFragment"

        fun newInstance(movieId: String, posterUrl: String): Fragment {
            return DetailsFragment().apply {
                // Пробрасываем аргументы в Bundle
                arguments = bundleOf(
                    ARGS_MOVIE_ID to movieId,
                    ARGS_POSTER_URL to posterUrl
                )
            }
        }

    }

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var tabsMediator: TabLayoutMediator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DetailsFragment", "onViewCreated called")

        val posterUrl = arguments?.getString("poster_url") ?: ""
        val movieId = arguments?.getString("movie_id") ?: ""

        // Установка адаптера
        binding.viewPager.adapter = DetailsViewPagerAdapter(
            fragmentManager = childFragmentManager,
            lifecycle = lifecycle,
            posterUrl = posterUrl,
            movieId = movieId,
        )

        // TabLayoutMediator
        tabsMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.poster)
                1 -> tab.text = getString(R.string.details)
            }
        }
        tabsMediator.attach()
        Log.d("DetailsFragment", "tabsMediator attached")

        var downX = 0f
        var upX = 0f
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
                    upX = event.x
                    if (upX - downX > 300) {
                        requireActivity().finish()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabsMediator.detach()
    }

}