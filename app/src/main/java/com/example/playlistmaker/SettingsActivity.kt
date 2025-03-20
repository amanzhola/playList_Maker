package com.example.playlistmaker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : BaseActivity() {

    private lateinit var backButton: ImageView
    lateinit var switchControl: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences
    private var isDarkTheme: Boolean = false
    private var isBottomNavVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        isDarkTheme = sharedPreferences.getBoolean(darkThemeKey, false)

        initViews()
        setupClickListeners()
        setupBottomNavigationView()
        bottomNavigationView.selectedItemId = R.id.navigation_settings
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
        return R.layout.activity_settings
    }

    override fun getMainLayoutId(): Int {
        return R.id.activity_settings
    }

    private fun <T : View> find(id: Int): T { return findViewById(id)!! }

    private fun initViews() {
        switchControl = findViewById(R.id.switch_control)
        backButton = findViewById(R.id.backArrow)
        switchControl.isChecked = isDarkTheme
    }

    private fun setupClickListeners() {
        switchControl.setOnCheckedChangeListener { _, _ ->
            if (!isDialogVisible) {
                onSegmentClicked(0, false)
            }
        }
        setupViewClickListener<TextView>(R.id.share) { shareApp() }
        setupViewClickListener<TextView>(R.id.group) { writeToSupport() }
        setupViewClickListener<TextView>(R.id.agreement) { openAgreement() }

        if (isDarkTheme){
            setupViewClickListener<TextView>(R.id.title) {
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.fade_in,
                    R.anim.fade_out
                ).toBundle()
                ActivityCompat.finishAfterTransition(this)
            }
        } else {
            setupViewClickListener<ImageView>(R.id.backArrow) {
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                ).toBundle()
                ActivityCompat.finishAfterTransition(this)
            }
        }
    }

    private fun <T : View> setupViewClickListener(viewId: Int, action: () -> Unit) {
        find<T>(viewId).setOnClickListener { action() }
    }
}
