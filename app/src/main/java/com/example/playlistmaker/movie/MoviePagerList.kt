package com.example.playlistmaker.movie

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MoviePagerList : AppCompatActivity() {

    private lateinit var movieViewPager: ViewPager2
    private lateinit var moviesAdapter: MoviesAdapterList
    private val gson = Gson()
    private var movies: List<Movie> = emptyList() // Создаем переменную для хранения списка фильмов
    private var isVertical = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movie_pager_list)

        movieViewPager = findViewById(R.id.movie_view_pager)

        // Проверяем, есть ли сохраненное состояние
        if (savedInstanceState != null) {
            // Восстанавливаем данные, если они есть
            val movieJsonList = savedInstanceState.getString("MOVIE_LIST_JSON")
            movies = gson.fromJson(movieJsonList, object : TypeToken<List<Movie>>() {}.type)
            val selectedIndex = savedInstanceState.getInt("SELECTED_INDEX", 0)
            isVertical = savedInstanceState.getBoolean("IS_VERTICAL", false)
            setupViewPager(movies, selectedIndex, isVertical)
        } else {
            // Получаем данные из Intent
            val movieJsonList = intent.getStringExtra("MOVIE_LIST_JSON")
            val selectedIndex = intent.getIntExtra("MOVIE_INDEX", 0)

            // Десериализация списка фильмов
            movies = gson.fromJson(movieJsonList, object : TypeToken<List<Movie>>() {}.type)

            // Настраиваем ViewPager
            setupViewPager(movies, selectedIndex, isVertical)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewPager(movies: List<Movie>, selectedIndex: Int, isVertical: Boolean) {
        // Создание адаптера для ViewPager2 // Передаем функцию переключения в адаптер
        moviesAdapter = MoviesAdapterList(movies, ::toggleOrientation, this)
        movieViewPager.adapter = moviesAdapter // Устанавливаем адаптер
        // Устанавливаем текущий выбранный фильм
        movieViewPager.setCurrentItem(selectedIndex, false)
        setViewPagerOrientation(isVertical) // Устанавливаем ориентацию
    }

    private fun setViewPagerOrientation(isVertical: Boolean) {
        movieViewPager.orientation = if (isVertical) ViewPager2.ORIENTATION_VERTICAL
        else ViewPager2.ORIENTATION_HORIZONTAL
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем данные перед поворотом экрана
        val movieJsonList = gson.toJson(movies)
        outState.putString("MOVIE_LIST_JSON", movieJsonList)
        outState.putInt("SELECTED_INDEX", movieViewPager.currentItem)
        outState.putBoolean("IS_VERTICAL", isVertical)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleOrientation() {
        isVertical = !isVertical
        setViewPagerOrientation(isVertical)
    }
}