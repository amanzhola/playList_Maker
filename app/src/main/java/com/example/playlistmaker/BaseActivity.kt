package com.example.playlistmaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.domain.repository.base.Agreement
import com.example.playlistmaker.domain.repository.base.Share
import com.example.playlistmaker.domain.repository.base.Support
import com.example.playlistmaker.presentation.utils.BottomNavigationHelper
import com.example.playlistmaker.presentation.utils.BottomNavigationProvider
import com.example.playlistmaker.presentation.utils.ColorApplierHelper
import com.example.playlistmaker.presentation.utils.ColorManager
import com.example.playlistmaker.presentation.utils.ColorPersistenceHelper
import com.example.playlistmaker.presentation.utils.ScreenType
import com.example.playlistmaker.presentation.utils.SegmentHelper
import com.example.playlistmaker.presentation.utils.SegmentManager
import com.example.playlistmaker.presentation.utils.SegmentManagerProvider
import com.example.playlistmaker.presentation.utils.SegmentTextHelper
import com.example.playlistmaker.presentation.utils.ThemeLanguageHelper
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.presentation.utils.ToolbarHelper
import com.example.playlistmaker.presentation.utils.toScreenType
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.MainFragment
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

// 🏆 🤔
sealed class NavigationData {
    data class ActivityData(
        val activityClass: Class<out Activity>,
        val enterAnim: Int,
        val exitAnim: Int,
        val buttonIndex: Int? = null // ✅ добавили
    ) : NavigationData()

    data class FragmentData(
        val destinationId: Int, // <-- ID из navigation_graph.xml
        val args: Bundle? = null,
        val enterAnim: Int,
        val exitAnim: Int
    ) : NavigationData()
}

// 🏆 ➡️ 🔄 📦 🤖 📈
    open class BaseActivity : AppCompatActivity(), CircleSegmentsView.OnSegmentClickListener {

//        private lateinit var mainLayout: LinearLayout // -> 🏆 🤔
        // 👉 вот почему мне приходилось оборачивать constrain в linearlayout
        private lateinit var mainLayout: ViewGroup // 🔄 LinearLayout etc

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
        private val themeInteraction: ThemeInteraction by inject() // 😎
        lateinit var toolbarHelper: ToolbarHelper
        lateinit var bottomNavigationHelper: BottomNavigationHelper
        private lateinit var segmentHelper: SegmentHelper
        private lateinit var colorApplierHelper: ColorApplierHelper
        private lateinit var colorPersistenceHelper: ColorPersistenceHelper
        private lateinit var segmentManager: SegmentManager
        private lateinit var colorManager: ColorManager
        private val share: Share by inject { parametersOf(this) }
        private val support by lazy {
            getKoin().get<Support> { parametersOf(this, mainLayout, failTextView) }
        }
        private val agreement by lazy {
            getKoin().get<Agreement> { parametersOf(this, mainLayout, failTextView) }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            ThemeLanguageHelper.applySavedLanguage(this) // 🌓 ↔️ 🌗
            super.onCreate(savedInstanceState) // 🔜 🔝 🔚 ⬇️ ⬅️ 🔙

            setContentView(getLayoutId())

            mainLayout = findViewById(getMainLayoutId())

            buttonIndex = intent.getIntExtra("buttonIndex", -1)
            val activityName = this::class.simpleName ?: "UnknownActivity"
            colorPersistenceHelper = get<ColorPersistenceHelper> { parametersOf(activityName, isDarkThemeEnabled()) }

            initializeToolbar()
            colorApplierHelper = ColorApplierHelper(this, mainLayout, toolbarHelper)
            bottomNavigationHelper = BottomNavigationProvider.createHelper(this, bottomViewIds, buttonIndex)
            bottomNavigationHelper.setupBottomNavigation()
            bottomNavigationHelper.setBottomNavigationVisibility()

            if (shouldEnableEdgeToEdge()) { enableEdgeToEdge() }
            setupWindowInsets()

            val isMainFragment = (getCurrentFragment() is MainFragment)
            segmentTexts = SegmentTextHelper.getSegmentTexts(this, isMainFragment)
            newSegmentTexts = SegmentTextHelper.getNewSegmentTexts(this, isMainFragment)

            // 👌 for 2 more 😉 parameters by default to have 1 out of 3
            segmentHelper = SegmentHelper(this, this)
            colorManager = ColorManager(colorApplierHelper, colorPersistenceHelper) { recreate() }
            segmentManager = SegmentManagerProvider.provide(this, colorApplierHelper,
                colorPersistenceHelper, colorManager)
            segmentManager = SegmentManagerProvider.provide(this, colorApplierHelper,
                colorPersistenceHelper, colorManager)
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

        // ⬇️ 🚗 💖
        fun showBottomNavigation() = bottomNavigationHelper.showBottomNavigation()
        fun hideBottomNavigation() = bottomNavigationHelper.hideBottomNavigation()
        fun isDarkThemeEnabled() = themeInteraction.isDarkTheme() // 🌓 ↔️ 🌗

        protected open fun shouldEnableEdgeToEdge(): Boolean = true
        protected open fun getLayoutId(): Int = R.layout.base_main
        protected open fun getMainLayoutId(): Int = R.id.nav_host_container

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

    // 🔧 BaseActivity
    fun navigateToMainScreen(host: Any, buttonIndex: Int = -1) {
        val context = when (host) {
            is AppCompatActivity -> host
            is Fragment -> host.requireContext()
            else -> throw IllegalArgumentException("Unsupported host")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("buttonIndex", buttonIndex)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val options = ActivityOptionsCompat.makeCustomAnimation(
            context, R.anim.enter_from_left, R.anim.exit_to_right
        )

        when (host) {
            is AppCompatActivity -> {
                host.startActivity(intent, options.toBundle())
            }
            is Fragment -> {
                host.startActivity(intent, options.toBundle())
            }
            else -> throw IllegalArgumentException("Unsupported host")
        }
    }

    fun changeLanguage() {
                ThemeLanguageHelper.toggleLanguage(this)
                recreate() // 👈 ⚠️ (перезапуск) 🔄
            }

        fun shareApp() = share.shareApp()
        fun writeToSupport() = support.writeToSupport()
        fun openAgreement() = agreement.openAgreement()


//********************************************************************************
        // tranfer main to fragment

    // no-op: handled by navController.navigate()
    protected open fun setupInitialFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(getMainLayoutId(), fragment)
            .commit()
    }

    fun updateToolbar(config: ToolbarConfig) {
        toolbarHelper.updateToolbar(config)
    }

    fun enableEdgeToEdge(enabled: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, !enabled)
    }

    fun getCurrentFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as? NavHostFragment
        return navHostFragment?.childFragmentManager?.primaryNavigationFragment
    }

    fun updateSegmentTexts() {
        val currentFragment = getCurrentFragment()
        val screenType = currentFragment.toScreenType()
        val isMainFragment = screenType == ScreenType.MAIN_FRAGMENT

        segmentTexts = SegmentTextHelper.getSegmentTexts(this, isMainFragment)
        newSegmentTexts = SegmentTextHelper.getNewSegmentTexts(this, isMainFragment)
    }


}