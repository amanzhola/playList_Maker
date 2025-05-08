package com.example.playlistmaker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.movie.IMDbApi
import com.example.playlistmaker.movie.Movie
import com.example.playlistmaker.movie.MoviePager
import com.example.playlistmaker.movie.MoviePagerList
import com.example.playlistmaker.movie.MoviesAdapter
import com.example.playlistmaker.movie.MoviesEvent
import com.example.playlistmaker.movie.MoviesResponse
import com.example.playlistmaker.movie.MoviesViewModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchMovie : BaseActivity() {

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L // ✨
    }

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())

    private val apiKey = "k_zcuw1ytf"
    private val imdbBaseUrl = "https://tv-api.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(imdbBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val imdbService = retrofit.create(IMDbApi::class.java)

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var moviesList: RecyclerView

    private val movies = ArrayList<Movie>()

    private val gson = Gson()
//    private val adapter = MoviesAdapter { movie ->
//        val intent = Intent(this, MoviePager::class.java)
//        val movieJson = gson.toJson(movie)
//        intent.putExtra("selected_movie", movieJson)
//        startActivity(intent)
//    }

    private val adapter = MoviesAdapter { event ->
        // Прямо обрабатываем событие SingleMovie
        val selectedMovieEvent = event as? MoviesEvent.SingleMovie
        selectedMovieEvent?.let {
            val selectedMovie = it.movie
            val position = it.position // Получаем позицию из события
            showChoiceDialog(selectedMovie, position)  // Передаем выбранный фильм и его индекс
        }
    }

    private fun showChoiceDialog(selectedMovie: Movie, position: Int) {

        if (!clickDebounce()) return // ✨

        val options = arrayOf("Один фильм", "Список фильмов")

        AlertDialog.Builder(this)
            .setTitle("Выберите опцию")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Выбран "Один фильм"
                        val movieJson = gson.toJson(selectedMovie)
                        val intent = Intent(this, MoviePager::class.java)
                        intent.putExtra("selected_movie", movieJson)
                        startActivity(intent)
                    }
                    1 -> {
                        // Выбран "Список фильмов"
                        val movieList: List<Movie> = adapter.getMovies() // Получаем полный список фильмов
                        val movieJsonList = gson.toJson(movieList) // Сериализуем в JSON
                        val intent = Intent(this, MoviePagerList::class.java)
                        intent.putExtra("MOVIE_LIST_JSON", movieJsonList)
                        intent.putExtra("MOVIE_INDEX", position) // Передача индекса фильма
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private var isBottomNavVisible: Boolean = true
    private lateinit var viewModel: MoviesViewModel

//    companion object {
//        private const val KEY_MOVIES_JSON = "saved_movies"
//    }

    private val progressBar: ProgressBar by lazy { findViewById(R.id.progressBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        moviesList = findViewById(R.id.movies)

        adapter.movies = movies

        moviesList.layoutManager = LinearLayoutManager(this)
        moviesList.adapter = adapter
        // option without viewmodel
//        if (savedInstanceState != null) {
//            val json = savedInstanceState.getString(KEY_MOVIES_JSON)
//            val type = object : TypeToken<List<Movie>>() {}.type
//            val restoredMovies: List<Movie>? = Gson().fromJson(json, type)
//
//            restoredMovies?.let {
//                adapter.updateMovies(it)
//            }
//        }

        viewModel = ViewModelProvider(this)[MoviesViewModel::class.java]

        // Подписываемся на изменения списка фильмов
        viewModel.movies.observe(this) { newMovies ->
            adapter.updateMovies(newMovies)
        }

        searchButton.setOnClickListener {
            val query = queryInput.text.toString()

            if (query.isNotEmpty()) {
                progressBar.visibility = VISIBLE
                imdbService.getAdvancedSearch(apiKey, query).enqueue(object :
                    Callback<MoviesResponse> {
                    override fun onResponse(call: Call<MoviesResponse>, response: Response<MoviesResponse>) {
                        progressBar.visibility = GONE
                        if (response.isSuccessful) {
                            response.body()?.results?.let { newMovies ->
//                                adapter.updateMovies(newMovies)
//                                movies.clear()
//                                movies.addAll(newMovies)
                                viewModel.updateMovies(newMovies)
                                showMessage("", "")
                            } ?: showMessage(getString(R.string.nothing_found), "")
                        } else {
                            showMessage(
                                getString(R.string.something_went_wrong),
                                response.code().toString()
                            )
                        }
                    }

                    override fun onFailure(call: Call<MoviesResponse>, t: Throwable) {
                        showMessage(getString(R.string.something_went_wrong), t.message.toString())
                    }
                })
            } else {
                showMessage(getString(R.string.enter_movie_name), "")
            }
        }

//// option for SearchMovie

//        searchButton.setOnClickListener {
//            if (queryInput.text.isNotEmpty()) {
//                imdbService.findMovie(apiKey, queryInput.text.toString()).enqueue(object :
//                    Callback<MoviesResponse> {
//                    override fun onResponse(call: Call<MoviesResponse>, response: Response<MoviesResponse>) {
//                        if (response.isSuccessful) {
//                            response.body()?.results?.let { newMovies ->
////                                adapter.updateMovies(newMovies)
////                                movies.clear()
////                                movies.addAll(newMovies)
//                                viewModel.updateMovies(newMovies)
//                                showMessage("", "")
//                            } ?: showMessage(getString(R.string.nothing_found), "")
//                        } else {
//                            showMessage(getString(R.string.something_went_wrong), response.code().toString())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<MoviesResponse>, t: Throwable) {
//                        showMessage(getString(R.string.something_went_wrong), t.message.toString())
//                    }
//                })
//            } else {
//                showMessage(getString(R.string.enter_movie_name), "")
//            }
//        }

        findViewById<TextView>(R.id.bottom4).isSelected = true
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholderMessage.visibility = VISIBLE
            placeholderMessage.text = text

            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            placeholderMessage.visibility = GONE
        }
    }

    override fun reverseList() {
        val currentTracksList = adapter.getMovies()
        val reversedList = currentTracksList.reversed()
        adapter.updateMovies(reversedList)
        moviesList.scrollToPosition(0)
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation()
        else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig = ToolbarConfig(VISIBLE, R.string.movie) { navigateToMainActivity() }
    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_search_movie
    override fun getMainLayoutId(): Int = R.id.main

}