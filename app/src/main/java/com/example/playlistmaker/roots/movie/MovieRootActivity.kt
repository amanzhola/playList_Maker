package com.example.playlistmaker.roots.movie

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.size
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityRootBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        // for dialog on exit 1
        // IDs верхнего уровня берём прямо из пунктов bottomNavigation (не нужно хардкодить):
        val topLevelDestinations: Set<Int> = buildSet {
            val menu = binding.bottomNavigationView.menu
//            for (i in 0 until menu.size()) add(menu.getItem(i).itemId)
            for (i in 0 until menu.size) add(menu[i].itemId)
        }

        // Перехватываем системный Back // for dialog on exit 2
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentId = navController.currentDestination?.id

                // Если мы НЕ на одном из верхнеуровневых экранов — ведём себя как обычно (шаг назад по графу)
                if (currentId != null && currentId !in topLevelDestinations) {
                    navController.navigateUp()
                    return
                }

                // Иначе — пользователь собирается выйти из приложения: показываем диалог подтверждения
                showExitDialog()
            }
        })


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

    // for dialog on exit 3
    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Вы действительно хотите выйти из фрагментов?")
            .setPositiveButton("Да") { dialog, _ ->
                dialog.dismiss()
                finish() // по заданию — finish(); если нужно закрыть всю задачу, можно finishAffinity()
            }
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.dismiss()
                // ничего не делаем, остаёмся в приложении
            }
            .show()
    }

}
