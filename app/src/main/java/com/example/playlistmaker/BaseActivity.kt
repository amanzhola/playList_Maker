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
import com.example.playlistmaker.presentation.utils.resetBackgroundRecursively
import com.example.playlistmaker.presentation.utils.screenKeyOrDefault
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

        // 1) toolbar = initializeToolbar() + toolbarHelper.applyThemeColors()
        initializeToolbar()
        // fixing theme on emulator and real mobile difference
        // ⬇️ сразу применяем цвета темы
        toolbarHelper.applyThemeColors()

        // 2) persistence + applier
        colorPersistenceHelper = get<ColorPersistenceHelper> { parametersOf(isDarkThemeEnabled()) }
        colorApplierHelper = ColorApplierHelper(this, mainLayout, toolbarHelper)

        // 3) bottom nav и прочие хелперы
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

        // 4) ColorManager ДОЛЖЕН быть создан до applySavedColorsForCurrentScreen()
        colorManager = ColorManager(
            colorApplierHelper = colorApplierHelper,
            colorPersistenceHelper = colorPersistenceHelper,
            getScope = { getCurrentScreenKey() },
            recreateActivity = { recreate() }
        )

        // segmentManager после colorManager
        segmentManager = SegmentManagerProvider.provide(this, colorApplierHelper,
            colorPersistenceHelper, colorManager)
        segmentManager = SegmentManagerProvider.provide(this, colorApplierHelper,
            colorPersistenceHelper, colorManager)
        colorManager.applySavedColors()

        // 5) только теперь — восстановление сохранённых цветов
        applySavedColorsForCurrentScreen()
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

        // fixing theme on emulator and real mobile difference
        // ⬇️ на всякий случай — если тема сменена в другой Activity
        applyThemeThenRestoreSaved()
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

    // fixing theme on emulator and real mobile difference
    // Если где-то перехватишь uiMode вручную — вызови здесь:
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        delegate.applyDayNight()
        toolbarHelper.applyThemeColors()
    }

    // fixing theme on emulator and real mobile difference
    // Удобный фасад, чтобы дёргать из фрагментов при точечной смене темы
    fun applyToolbarThemeColors() = toolbarHelper.applyThemeColors()

    //************************************************************************
    // Toolbar color fixing to apply and saving for fragment and activity

    /** Вернёт реально видимый экран: активную «дочку» табов, или текущий фрагмент, или null (чистая активити) */
    fun getVisibleScreenFragmentOrNull(): Fragment? {
        val top = getCurrentFragment() ?: return null
        if (!top.isAdded || top.view == null) return top
        val child = top.childFragmentManager.fragments.firstOrNull { it.isVisible && it.view != null }
        return child ?: top
    }

    /** scope текущего экрана (фрагмента/таба) или scope активити, если фрагмента нет */
    fun getCurrentScreenKey(): String {
        val target = getVisibleScreenFragmentOrNull()
        return target?.screenKeyOrDefault() ?: activityScope()
    }

    /** scope для активити (один на активити) */
    fun activityScope(): String = "Activity:${this::class.java.simpleName}"

    fun applySavedColorsForCurrentScreen(skipActivityFallback: Boolean = false) {
        if (!::toolbarHelper.isInitialized || !::colorPersistenceHelper.isInitialized || !::colorManager.isInitialized) return

        // 1) применяем сохранённые слоты для ТЕКУЩЕГО scope (фрагмент/таба или активити)
        colorManager.applySavedColors()

        // 2) фолбэк тулбара из активити — ТОЛЬКО если НЕ попросили пропустить
        if (!skipActivityFallback) {
            val target = getVisibleScreenFragmentOrNull()
            if (target != null) {
                val fragScope = getCurrentScreenKey()
                val bg = colorPersistenceHelper.load(fragScope, 1)
                if (bg == null) {
                    val actBg = colorPersistenceHelper.load(activityScope(), 1)
                    actBg?.let { toolbarHelper.setBackgroundColor(it) }
                }
            }
        }
    }

    // Узнать сохранённый цвет для ТЕКУЩЕГО экрана (scope = текущий фрагмент/вкладка или "чистая" Activity)
    // если где-то нужно узнать сохранённый цвет для текущего экрана:
    fun getSavedColorForCurrentScreen(slot: Int): Int? =
        colorPersistenceHelper.load(getCurrentScreenKey(), slot)

    /** Тема + поверх сохранённые цвета. Вызываем вместо "applyToolbarThemeColors()" где надо */
    fun applyThemeThenRestoreSaved() {
        toolbarHelper.applyThemeColors()
        applySavedColorsForCurrentScreen()
    }

    fun applySavedForScopeOrDefault(scope: String, defaultBg: Int) {
        // 1) кого сейчас показываем (активный ребёнок у ViewPager2)
        val child = getVisibleScreenFragmentOrNull()
        val exclude: Set<Int> =
            (child as? com.example.playlistmaker.presentation.utils.BackgroundExclusionProvider)
                ?.backgroundExclusionIds().orEmpty()

        // 2) всегда сперва сбрасываем ФОН ТЕЛА текущей вкладки в дефолт
        (child?.view as? ViewGroup)?.resetBackgroundRecursively(defaultBg, exclude)

        // 3) подтягиваем персональный bg (slot=1) именно для этого scope
        val bg = colorPersistenceHelper.load(scope, 1)
        // bg: Int? (загруженный цвет), defaultBg: Int
        bg?.let { color ->
            toolbarHelper.setBackgroundColor(color)                 // тулбар = свой цвет
            (child?.view as? ViewGroup)
                ?.resetBackgroundRecursively(color, exclude)        // тело = тот же цвет
        } ?: run {
            toolbarHelper.setBackgroundColor(defaultBg)             // тулбар = дефолт
            // тело уже было сброшено ранее в шаге 2
        }
    }

}