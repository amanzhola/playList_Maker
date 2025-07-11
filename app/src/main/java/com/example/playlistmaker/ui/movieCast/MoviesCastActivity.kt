//package com.example.playlistmaker.ui.movieCast
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.playlistmaker.databinding.ActivityMoviesCastBinding
//import com.example.playlistmaker.R
//import com.example.playlistmaker.presentation.movieCast.MoviesCastState
//import com.example.playlistmaker.presentation.movieCast.MoviesCastViewModel
//import com.example.playlistmaker.ui.cast.movieCastHeaderDelegate
//import com.example.playlistmaker.ui.cast.movieCastPersonDelegate
//import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
//import org.koin.androidx.viewmodel.ext.android.viewModel
//import org.koin.core.parameter.parametersOf
//
//class MoviesCastActivity : AppCompatActivity(R.layout.fragment_movies_cast) {
//
//    companion object {
//
//        private const val ARGS_MOVIE_ID = "movie_id"
//
//        fun newInstance(context: Context, movieId: String): Intent {
//            return Intent(context, MoviesCastActivity::class.java).apply {
//                putExtra(ARGS_MOVIE_ID, movieId)
//            }
//        }
//
//    }
//
//    // Добавили инжект ViewModel
//    private val moviesCastViewModel: MoviesCastViewModel by viewModel {
//        parametersOf(intent.getStringExtra(ARGS_MOVIE_ID))
//    }
//
////    // Добавили адаптер для RecyclerView
////    private val adapter = MoviesCastAdapter()
//
//    private val adapter = ListDelegationAdapter(
//        movieCastHeaderDelegate(),
//        movieCastPersonDelegate(),
//    )
//
//    private lateinit var binding : ActivityMoviesCastBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMoviesCastBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // TODO "Добавить вёрстку"
//        // TODO "Прочитать идентификатор фильма из Intent"
//
//        // Привязываем адаптер и LayoutManager к RecyclerView
//        binding.moviesCastRecyclerView.adapter = adapter
//        binding.moviesCastRecyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Наблюдаем за UiState из ViewModel
//        moviesCastViewModel.observeState().observe(this) {
//            // В зависимости от UiState экрана показываем
//            // разные состояния экрана
//            when (it) {
//                is MoviesCastState.Content -> showContent(it)
//                is MoviesCastState.Error -> showError(it)
//                is MoviesCastState.Loading -> showLoading()
//            }
//        }
//    }
//
//    private fun showLoading() {
//        binding.contentContainer.isVisible = false
//        binding.errorMessageTextView.isVisible = false
//
//        binding.progressBar.isVisible = true
//    }
//
//    private fun showError(state: MoviesCastState.Error) {
//        binding.contentContainer.isVisible = false
//        binding.progressBar.isVisible = false
//
//        binding.errorMessageTextView.isVisible = true
//        binding.errorMessageTextView.text = state.message
//    }
//
////    @SuppressLint("NotifyDataSetChanged")
////    private fun showContent(state: MoviesCastState.Content) {
////        binding.progressBar.isVisible = false
////        binding.errorMessageTextView.isVisible = false
////
////        binding.contentContainer.isVisible = true
////        binding.movieTitle.text = state.movie.fullTitle
////
////        // Просто объединяем всех участников
////        // в единый список и отображаем
////        adapter.persons = state.movie.directors + state.movie.writers + state.movie.actors + state.movie.others
////        adapter.notifyDataSetChanged()
////    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun showContent(state: MoviesCastState.Content) {
//        binding.progressBar.isVisible = false
//        binding.errorMessageTextView.isVisible = false
//
//        binding.contentContainer.isVisible = true
//
//        // Меняем привязку стейта к UI-элементам
//        binding.movieTitle.text = state.fullTitle
//        adapter.items = state.items
//
//        adapter.notifyDataSetChanged()
//    }
//
//}