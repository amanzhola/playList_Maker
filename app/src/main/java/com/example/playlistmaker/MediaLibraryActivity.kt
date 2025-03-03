package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat

class MediaLibraryActivity : BaseActivity() {

    private lateinit var backButton: ImageView
    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        initViews()
        setupClickListeners()
        setupBottomNavigationView()
        bottomNavigationView.selectedItemId = R.id.navigation_media
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            bottomNavigationView.animate().translationY(bottomNavigationView.height.toFloat()).alpha(0f).setDuration(300).withEndAction {
                bottomNavigationView.visibility = View.GONE
            }
            line.animate().translationY(line.height.toFloat()).alpha(0f).setDuration(300).withEndAction {
                line.visibility = View.GONE            }
        } else {
            bottomNavigationView.visibility = View.VISIBLE
            line.visibility = View.VISIBLE

            bottomNavigationView.alpha = 0f
            bottomNavigationView.translationY = bottomNavigationView.height.toFloat()
            bottomNavigationView.animate().translationY(0f).alpha(1f).setDuration(300)

            line.alpha = 0f
            line.translationY = line.height.toFloat()
            line.animate().translationY(0f).alpha(1f).setDuration(300)
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_media_library
    }

    override fun getMainLayoutId(): Int {
        return R.id.activity_media_library
    }

    private fun initViews(){
        backButton = findViewById(R.id.backArrow)
    }

    private fun setupClickListeners(){
        backButton.setOnClickListener {
            ActivityOptionsCompat.makeCustomAnimation(
                this, R.anim.enter_from_left, R.anim.exit_to_right
            ).toBundle()
            finishAfterTransition()
        }
    }
}