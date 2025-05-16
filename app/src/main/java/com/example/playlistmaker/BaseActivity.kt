package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.ThemeInteraction
import com.example.playlistmaker.presentation.utils.BottomNavigationHelper
import com.example.playlistmaker.presentation.utils.BottomNavigationProvider
import com.example.playlistmaker.presentation.utils.ColorApplierHelper
import com.example.playlistmaker.presentation.utils.ColorManager
import com.example.playlistmaker.presentation.utils.ColorPersistenceHelper
import com.example.playlistmaker.presentation.utils.SegmentHelper
import com.example.playlistmaker.presentation.utils.SegmentManager
import com.example.playlistmaker.presentation.utils.SegmentManagerProvider
import com.example.playlistmaker.presentation.utils.SegmentTextHelper
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.presentation.utils.ToolbarHelper
import com.example.playlistmaker.presentation.utils.activityHelper.AppShareHelper
import com.example.playlistmaker.presentation.utils.activityHelper.FailUiController
import com.example.playlistmaker.presentation.utils.activityHelper.LegalHelper
import com.example.playlistmaker.presentation.utils.activityHelper.NetworkChecker
import com.example.playlistmaker.presentation.utils.activityHelper.SupportHelper
import com.example.playlistmaker.ui.main.MainActivity
import com.google.gson.Gson

// 🏆 🤔
    data class NavigationData(val activityClass: Class<*>, val enterAnim: Int, val exitAnim: Int)
    // 🏆 ➡️ 🔄 📦 🤖 📈
    open class BaseActivity : AppCompatActivity(), CircleSegmentsView.OnSegmentClickListener {

        private lateinit var mainLayout: LinearLayout
        private var isDialogVisible = false
        private val baseSegmentColors = intArrayOf(
            R.color.hintFieldColor,
            R.color.blue
        )
        private val segmentColors = IntArray(6) { baseSegmentColors[it % 2] }
        private lateinit var segmentTexts: Array<String>
        private val segmentIcons by lazy { SegmentTextHelper.getSegmentIcons(this) }
        private val newSegmentColors = IntArray(6) { baseSegmentColors[it % 2] }
        private lateinit var newSegmentTexts: Array<String>
        private val newSegmentIcons by lazy { SegmentTextHelper.getNewSegmentIcons(this) }
        private val totalSegments = segmentColors.size
        private val newTotalSegments = newSegmentColors.size

        private val bottomViewIds = listOf( // 👉 📊
            R.id.bottom1,
            R.id.bottom2,
            R.id.bottom3,
            R.id.bottom4,
            R.id.bottom5,
            R.id.bottom6
        )
        var buttonIndex: Int = -1

        private val failTextView: TextView by lazy { findViewById(R.id.fail) }
        lateinit var gson: Gson

        private val themeInteraction: ThemeInteraction by lazy { // 😎
            Creator.provideThemeInteraction()
        }

        private lateinit var toolbarHelper: ToolbarHelper
        private lateinit var bottomNavigationHelper: BottomNavigationHelper
        private lateinit var segmentHelper: SegmentHelper
        private lateinit var colorApplierHelper: ColorApplierHelper
        private lateinit var colorPersistenceHelper: ColorPersistenceHelper
        private lateinit var segmentManager: SegmentManager
        private lateinit var colorManager: ColorManager
        lateinit var appShareHelper: AppShareHelper
        lateinit var supportHelper: SupportHelper
        lateinit var legalHelper: LegalHelper


        override fun onCreate(savedInstanceState: Bundle?) {
            ThemeLanguageHelper.applySavedLanguage(this) // 🌓 ↔️ 🌗
            super.onCreate(savedInstanceState) // 🔜 🔝 🔚

            setContentView(getLayoutId())
            mainLayout = findViewById(getMainLayoutId())

            buttonIndex = intent.getIntExtra("buttonIndex", -1)
            val activityName = this::class.simpleName ?: "UnknownActivity"
            colorPersistenceHelper = ColorPersistenceHelper(this, activityName, isDarkThemeEnabled())

            initializeToolbar()
            colorApplierHelper = ColorApplierHelper(this, mainLayout, toolbarHelper)
            bottomNavigationHelper = BottomNavigationProvider.createHelper(this, bottomViewIds, buttonIndex)
            bottomNavigationHelper.setupBottomNavigation()
            bottomNavigationHelper.setBottomNavigationVisibility()

            if (shouldEnableEdgeToEdge()) {
                enableEdgeToEdge()
            }
            setupWindowInsets()

            segmentTexts = SegmentTextHelper.getSegmentTexts(this, this is MainActivity)
            newSegmentTexts = SegmentTextHelper.getNewSegmentTexts(this, this is MainActivity)
            gson = Gson()
            // SearchHisory done 👌 null for 2 more 😉 parameters to have 1 out of 3
            val failUiController = FailUiController(this, mainLayout, failTextView)
            appShareHelper = AppShareHelper(this)
            supportHelper = SupportHelper(this, failUiController)
            legalHelper = LegalHelper(this, NetworkChecker(), failUiController)

            segmentHelper = SegmentHelper(this, this)

            colorManager = ColorManager(
                colorApplierHelper = colorApplierHelper,
                colorPersistenceHelper = colorPersistenceHelper,
                recreateActivity = { recreate() }
            )

            segmentManager = SegmentManagerProvider.provide(
                activity = this,
                colorApplierHelper = colorApplierHelper,
                colorPersistenceHelper = colorPersistenceHelper,
                colorManager = colorManager
            )

            segmentManager = SegmentManagerProvider.provide(
                activity = this,
                colorApplierHelper = colorApplierHelper,
                colorPersistenceHelper = colorPersistenceHelper,
                colorManager = colorManager
            )

            colorManager.applySavedColors()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.dropdown -> {
                    segmentHelper.showSegmentDialog( // 📈 🎨
                        segmentColors,
                        segmentTexts,
                        segmentIcons,
                        newSegmentColors,
                        newSegmentTexts,
                        newSegmentIcons,
                        totalSegments,
                        newTotalSegments
                    ) {
                        isDialogVisible = false
                    }
                    isDialogVisible = true
                    true
                }
                R.id.filter_list -> {
                    reverseList()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        open fun reverseList() {
            // To be done by subclass or derived class 🔧✨
        }

        override fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean) {
            segmentManager.onSegmentClicked(segmentIndex, isChangedState, this)
        }

        open fun onSegment4Clicked() {} // by btm navig 🔥
        override fun onResume() {
            super.onResume()
            segmentManager.applySavedColors()
        }

        protected fun setBottomNavigationVisibility() { // 🧠

            for (i in bottomViewIds.indices) {
                val bottomView: TextView? = findViewById(bottomViewIds[i])
                bottomView?.visibility = when {
                    buttonIndex in 0..2 && i in 0..2 -> VISIBLE
                    buttonIndex in 3..5 && i in 3..5 -> VISIBLE
                    else -> GONE
                }
            }
        }

        protected fun showBottomNavigation() = bottomNavigationHelper.showBottomNavigation()
        protected fun hideBottomNavigation() = bottomNavigationHelper.hideBottomNavigation()
        fun isDarkThemeEnabled() = themeInteraction.isDarkTheme() // 🌓 ↔️ 🌗

        protected open fun shouldEnableEdgeToEdge(): Boolean = true
        protected open fun getLayoutId(): Int = R.layout.activity_main
        protected open fun getMainLayoutId(): Int = R.id.main
        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menuInflater.inflate(R.menu.menu, menu)
            return true
        }

        private fun setupWindowInsets() {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        private fun initializeToolbar() {
            toolbarHelper = ToolbarHelper(this)
            toolbarHelper.initialize(getToolbarConfig(), this is MainActivity)
        }

        protected open fun getToolbarConfig(): ToolbarConfig {
            return ToolbarConfig(VISIBLE, R.string.app_name)
        }

        protected fun navigateToMainActivity() {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val options = ActivityOptionsCompat.makeCustomAnimation(
                this, R.anim.enter_from_left, R.anim.exit_to_right
            )
            startActivity(intent, options.toBundle())
        }

        fun changeLanguage() {
            ThemeLanguageHelper.toggleLanguage(this)
            recreate() // 👈 ⚠️ (перезапуск) 🔄
        }

        fun shareApp() = appShareHelper.shareApp()
        fun writeToSupport() = supportHelper.writeToSupport()
        fun openAgreement() = legalHelper.openAgreement()
    }