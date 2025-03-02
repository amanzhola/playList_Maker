package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        setupButtons()
    }

    private fun setupButtons() {
        val buttonIds = listOf(R.id.Button_Big1, R.id.Button_Big2, R.id.Button_Big3)

        for (buttonId in buttonIds) {
            findViewById<View>(buttonId).setOnClickListener {
                when (buttonId) {
                    R.id.Button_Big1 -> launchActivityWithAnimation(SearchActivity::class.java, R.anim.fade_in, R.anim.fade_out)
                    R.id.Button_Big2 -> launchActivityWithAnimation(MediaLibraryActivity::class.java, R.anim.slide_in_right, R.anim.slide_out_left)
                    R.id.Button_Big3 -> launchActivityWithAnimation(SettingsActivity::class.java, R.anim.zoom_in, R.anim.zoom_out)
                }
            }
        }
    }

    private fun <T> launchActivityWithAnimation(activityClass: Class<T>, enterAnim: Int, exitAnim: Int) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptionsCompat.makeCustomAnimation(this, enterAnim, exitAnim)
        startActivity(intent, options.toBundle())
    }

}