package com.example.playlistmaker.ui.moviePosters

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityDetailsBinding
import com.example.playlistmaker.domain.util.ResourceMovieDetials
import com.example.playlistmaker.presentation.movieDetails.MovieDetailsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var tabMediator: TabLayoutMediator

    private val viewModel: MovieDetailsViewModel by viewModel()

    // 👉 Переменные для свайпа
    private var downX = 0f
    private var upX = 0f
    private var isOnFirstTab = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val poster = intent.getStringExtra("poster") ?: ""
        val movieId = intent.getStringExtra("id") ?: ""

        // Загружаем детали фильма
        viewModel.loadMovieDetails(movieId)

        viewModel.movieDetails.observe(this) { result ->
            when (result) {
                is ResourceMovieDetials.Success -> {
                    val movie = result.data
                    Log.d("DetailsActivity", "Movie loaded: $movie")
                }
                is ResourceMovieDetials.Error -> {
                    Log.e("DetailsActivity", "Error loading movie: ${result.message}")
                }
            }
        }

        binding.viewPager.adapter = DetailsViewPagerAdapter(
            fragmentManager = supportFragmentManager,
            lifecycle = lifecycle,
            posterUrl = poster,
            movieId = movieId,
        )

        // ✅ Обновляем флаг текущей вкладки
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private var currentPage = 0
            private var lastPage = 0

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                // когда скролл закончен и возврат на первый таб
                if (state == ViewPager2.SCROLL_STATE_IDLE && currentPage == 0 && lastPage == 0) {
                    // пользователь сделал свайп влево-направо на первом табе — выходим
                    finish()
                }
            }

            override fun onPageSelected(position: Int) {
                lastPage = currentPage
                currentPage = position
            }
        })


        // ✅ Обработка свайпа вправо на первом табе
        binding.root.setOnTouchListener { _, event ->
            if (!isOnFirstTab) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    true
                }
                MotionEvent.ACTION_UP -> {
                    upX = event.x
                    val deltaX = upX - downX
                    if (deltaX > 300) {
                        finish() // 👈 Выход к SearchMovieActivity
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.poster)
                1 -> tab.text = getString(R.string.details)
            }
        }
        tabMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }
}
