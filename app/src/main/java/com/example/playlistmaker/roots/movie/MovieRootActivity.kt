package com.example.playlistmaker.roots.movie

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityRootBinding

class MovieRootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        // ✅ Добавляем historyFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = when (destination.id) {
                R.id.detailsFragment,
                R.id.namesFragment,
                R.id.infoFragment,
                R.id.historyFragment -> View.VISIBLE // 👈 добавили сюда
                else -> View.GONE
            }
        }

        if (savedInstanceState == null) {
            val movieId = intent.getStringExtra("id") ?: "tt1234567"
            val posterUrl = intent.getStringExtra("poster") ?: "https://..."

            val options = NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, true)
                .build()

            navController.navigate(
                R.id.detailsFragment,
                bundleOf("movie_id" to movieId, "poster_url" to posterUrl),
                options
            )
        }
    }
}
