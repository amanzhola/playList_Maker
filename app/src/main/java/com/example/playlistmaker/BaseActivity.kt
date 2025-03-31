package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.search.TrackAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.Locale

interface ApiService {
    @GET("/") // Путь к ресурсу
    fun checkInternetConnection(): Call<Void>
}

object RetrofitInstance {
    private const val BASE_URL = "https://www.google.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

data class ToolbarConfig(
    val backArrowVisibility: Int,
    val titleResId: Int,
    val titleClickListener: (() -> Unit)? = null
)

data class NavigationData(val activityClass: Class<*>, val enterAnim: Int, val exitAnim: Int)

open class BaseActivity : AppCompatActivity(), CircleSegmentsView.OnSegmentClickListener {

    private lateinit var mainLayout: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var backArrow: ImageView
    private lateinit var title: TextView
    private var currentIndex = 0
    private var currentLanguage: String = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var colorPreferences: SharedPreferences
    private var isDialogVisible = false
    private val baseSegmentColors = intArrayOf(
        R.color.hintFieldColor,
        R.color.blue
    )
    private val segmentColors = IntArray(6) { baseSegmentColors[it % 2] }
    private lateinit var segmentTexts: Array<String>
    private val segmentIcons by lazy {
        intArrayOf(
            R.drawable.switch_24,
            R.drawable.share,
            R.drawable.group,
            R.drawable.vector,
            if (this is MainActivity) R.drawable.color_24 else R.drawable.navigation_24,
            R.drawable.translate_24
        )
    }
    private val newSegmentColors = IntArray(6) { baseSegmentColors[it % 2] }
    private lateinit var newSegmentTexts: Array<String>
    private val newSegmentIcons by lazy {
        intArrayOf(
            R.drawable.text_color_24,
            R.drawable.background_24,
            R.drawable.text_color_24,
            R.drawable.color_24,
            if (this is MainActivity) R.drawable.background_24 else R.drawable.translate_24,
            if (this is MainActivity) R.drawable.translate_24 else R.drawable.color_24
        )
    }
    private val totalSegments = segmentColors.size
    private val newTotalSegments = newSegmentColors.size

    private val bottomViewIds = listOf(
        R.id.bottom1,
        R.id.bottom2,
        R.id.bottom3,
        R.id.bottom4,
        R.id.bottom5,
        R.id.bottom6
    )
    var buttonIndex: Int = -1
    private var bottomViewState: Int = 0
    private val langKey = "language"

    private val failTextView: TextView by lazy { findViewById(R.id.fail) }


    override fun onCreate(savedInstanceState: Bundle?) {

        (application as App).switchTheme((application as App).isDarkTheme)
        super.onCreate(savedInstanceState)
        buttonIndex = intent.getIntExtra("buttonIndex", -1)

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        applySettings()
        colorPreferences = getColorSharedPreferences()

        setContentView(getLayoutId())
        mainLayout = findViewById(getMainLayoutId())

        initializeToolbar()

        setupBottomNavigation()
        setBottomNavigationVisibility()

        if (shouldEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
        setupWindowInsets()

        segmentTexts = arrayOf(
            getString(R.string.switch_short),
            getString(R.string.share_short),
            getString(R.string.support_short),
            getString(R.string.agreement_short),
            if (this is MainActivity) getString(R.string.toDefault) else getString(R.string.navigation),
            getString(R.string.language)
        )

        newSegmentTexts = arrayOf(
            getString(R.string.set_titleColor), // "Цвет заглавия",
            getString(R.string.set_backgroundColor), // "Цвет фона",
            getString(R.string.set_btnTextColor), // "Цвет текста текста",
            getString(R.string.set_iconColor), // "Цвет иконок",
            if (this is MainActivity) getString(R.string.set_btnBackgroundColor) else getString(R.string.language), // "Цвет фона кнопки",
            if (this is MainActivity) getString(R.string.language) else getString(R.string.toDefault) // "По умолчанию"
        )
    }

    private fun getColorSharedPreferences(): SharedPreferences {
        return this.getSharedPreferences("color_preferences", Context.MODE_PRIVATE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.dropdown -> {
                val dialog = CircleSegmentsDialog(this, this)
                dialog.setOnDismissListener {
                    isDialogVisible = false
                }
                isDialogVisible = true
                dialog.show()
                dialog.setSegmentData(
                    segmentColors,
                    segmentTexts,
                    segmentIcons,
                    newSegmentColors,
                    newSegmentTexts,
                    newSegmentIcons,
                    totalSegments,
                    newTotalSegments
                )
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
        // To be done by subclass or derived class
    }

    override fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean) {
        val randomColor = getNextColor(this)

        if (isChangedState) {
            when (segmentIndex){
                4 -> if (this !is MainActivity) changeLanguage()
                5 -> if (this is MainActivity) changeLanguage() else clearAllColors()
            }
            if (segmentIndex != 5) saveColor(segmentIndex, randomColor)
            applyColorToSegment(segmentIndex, randomColor)
        } else {

            when (segmentIndex) {
                0 -> toggleTheme()
                1 -> shareApp()
                2 -> writeToSupport()
                3 -> openAgreement()
                4 -> if (this is MainActivity) clearAllColors() else onSegment4Clicked()
                5 -> changeLanguage()
            }

        }
    }

    open fun onSegment4Clicked() {}

    private fun toggleTheme() {
        val app = application as App
        app.switchTheme(!app.isDarkTheme)
        applySettings()
        if (this is SettingsActivity) {
            val switchControl: SwitchMaterial = findViewById(R.id.switch_control)
            switchControl.isChecked = app.isDarkTheme
        }

    }

    private fun applySettings() {
        val app = application as App
        currentLanguage = sharedPreferences.getString(langKey, "ru") ?: "ru"
        app.switchTheme(app.isDarkTheme)
        applyLanguage(currentLanguage)
    }

    private fun applyColorToSegment(segmentIndex: Int, color: Int) {
        when (segmentIndex) {
            0 -> if (this is MainActivity) {
                toolbar.setTitleTextColor(color)
            } else {
                findViewById<TextView>(R.id.title).setTextColor(color)
                findViewById<ImageView>(R.id.backArrow).imageTintList = ColorStateList.valueOf(color)
            }
            1 -> mainLayout.setBackgroundColor(color)
            2 -> when (this) {
                is MainActivity -> mainLayout.changeBtnTextColor(color)
                is SearchActivity -> this.getAdapter().also { it.setTextColor(color) }
                else -> mainLayout.changeColor(color, true, ignoreViewId = R.id.toolbar)
            }
            3 -> when (this) {
                is MainActivity -> mainLayout.changeBtnIconColor(color)
                is SettingsActivity -> mainLayout.changeColor(color, false, ignoreViewId = R.id.toolbar)
                is SearchActivity -> {
                    val adapter: TrackAdapter = this.getAdapter()
                    adapter.setArrowColor(color)
                }
            }
            4 -> if (this is MainActivity) mainLayout.changeBtnBackgroundColor(color)
        }
    }

    private fun saveColor(segmentIndex: Int, color: Int) {
        colorPreferences.edit().apply {
            putInt(getColorKey(segmentIndex), color)
            apply()
        }
    }

    private fun getColorKey(segmentIndex: Int): String {
        val activityName = when (this) {
            is MainActivity -> "MainActivity"
            is SettingsActivity -> "SettingsActivity"
            is SearchActivity -> "SearchActivity"
            is MediaLibraryActivity -> "MediaLibraryActivity"
            else -> "UnknownActivity"
        }
        val app = application as App
        val themeSuffix = if (app.isDarkTheme) "_dark" else "_light"
        return "${activityName}_segment_$segmentIndex$themeSuffix"
    }

    private fun loadColor(segmentIndex: Int): Int {
        return colorPreferences.getInt(getColorKey(segmentIndex), -1)
    }

    override fun onResume() {
        super.onResume()
        for (i in 0..4) {
            val color = loadColor(i)
            if (color != -1) applyColorToSegment(i, color)
        }
    }

    private fun clearAllColors() {
        colorPreferences.edit().apply {
            for (i in 0..4) {
                remove(getColorKey(i))
            }
            apply()
        }
        recreate()
        // option
//        finish()
//        startActivity(intent)
    }


    private fun getNextColor(context: Context): Int {
        val colorResId = ColorProvider.colors[currentIndex]
        val color = context.getColor(colorResId)

        currentIndex = (currentIndex + 1) % ColorProvider.colors.size
        return color
    }

    protected fun getButtonPairs(): List<Pair<String, Int>> {
        return listOf(
            Pair(getString(R.string.search), R.drawable.search_icon),
            Pair(getString(R.string.media), R.drawable.media_icon),
            Pair(getString(R.string.settings), R.drawable.settings_icon),
            Pair(getString(R.string.movie), R.drawable.movies_icon),
            Pair(getString(R.string.weather), R.drawable.weather_icon),
            Pair(getString(R.string.option), R.drawable.add_box_icon)
        )
    }

    protected fun getNavigationList(): List<NavigationData> {
        return listOf(
            NavigationData(SearchActivity::class.java, 0, 0),
            NavigationData(MediaLibraryActivity::class.java, 0, 0),
            NavigationData(SettingsActivity::class.java, 0, 0),
            NavigationData(SearchMovie::class.java, 0, 0),
            NavigationData(SearchWeather::class.java, 0, 0),
            NavigationData(ExtraOption::class.java, 0, 0)
        )
    }

    private fun launchActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("buttonIndex", buttonIndex)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this, R.anim.enter_from_bottom, R.anim.exit_to_top
        )
        startActivity(intent, options.toBundle())
    }

    private fun setupBottomNavigation() {
        val buttonPairs: List<Pair<String, Int>> = getButtonPairs()
        val navigationList: List<NavigationData> = getNavigationList()

        bottomViewIds.forEachIndexed { index, bottomViewId ->
            val bottomView: TextView? = findViewById(bottomViewId)

            if (bottomView != null && index < buttonPairs.size) {
                bottomView.text = buttonPairs[index].first
                bottomView.setCompoundDrawablesWithIntrinsicBounds(0, buttonPairs[index].second, 0, 0)

                bottomView.setOnClickListener {

                    val navigationData = navigationList[index]
                    if (this::class.java == navigationData.activityClass) {
                        updateVisibilityForButtons(index)
                        bottomViewState = if (bottomViewState == 0) 1 else 0
                    } else {
                        buttonIndex = index
                        launchActivity(navigationData.activityClass)
                    }
                }
            }
        }
    }

    private fun updateVisibilityForButtons(num: Int) {
        val isBelowThree = num < 3
        val visibleIndices = if (isBelowThree) {
            if (bottomViewState == 0) arrayOf(num, 3, 4, 5)
            else arrayOf(0, 1, 2)
        } else {
            if (bottomViewState == 0) arrayOf(0, 1, 2, num)
            else arrayOf(3, 4, 5)
        }
        for (i in bottomViewIds.indices) {
            val bottomView: TextView? = findViewById(bottomViewIds[i])
            bottomView?.visibility = if (i in visibleIndices) VISIBLE else GONE
        }
    }

    protected fun setBottomNavigationVisibility() {

        for (i in bottomViewIds.indices) {
            val bottomView: TextView? = findViewById(bottomViewIds[i])
            bottomView?.visibility = when {
                buttonIndex in 0..2 && i in 0..2 -> VISIBLE
                buttonIndex in 3..5 && i in 3..5 -> VISIBLE
                else -> GONE
            }
        }

    }

    protected fun showBottomNavigation() {
        setBottomNavigationVisibility(VISIBLE)
        setNavigationLineVisibility(VISIBLE)
    }

    protected fun hideBottomNavigation() {
        setBottomNavigationVisibility(GONE)
        setNavigationLineVisibility(GONE)
    }

    private fun setBottomNavigationVisibility(visibility: Int) {
        val bottomNavigation: LinearLayout = findViewById(R.id.bottom_navigation)
        bottomNavigation.visibility = visibility
    }

    private fun setNavigationLineVisibility(visibility: Int) {
        val navigationLine: View = findViewById(R.id.navigationLine)
        navigationLine.visibility = visibility
    }

    fun isDarkThemeEnabled(): Boolean {
        val app = application as App
        return app.isDarkTheme
    }

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
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        backArrow = toolbar.findViewById(R.id.backArrow)
        title = toolbar.findViewById(R.id.title)
        title.isEnabled = this is MainActivity

        val toolbarConfig = getToolbarConfig()
        updateToolbar(toolbarConfig)
    }

    protected open fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.app_name)
    }

    private fun updateToolbar(config: ToolbarConfig) {
        backArrow.visibility = config.backArrowVisibility
        title.setText(config.titleResId)

        backArrow.setOnClickListener(null)
        title.setOnClickListener(null)

        if (config.backArrowVisibility == VISIBLE) {
            backArrow.setOnClickListener {
                config.titleClickListener?.invoke()
            }

        } else {
            title.isEnabled = true
            title.setOnClickListener {
                config.titleClickListener?.invoke()
            }
        }
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

    private fun changeLanguage() {
        val newLanguage = if (currentLanguage == "ru") "en" else "ru"
        currentLanguage = newLanguage
        sharedPreferences.edit {
            putString(langKey, newLanguage)
        }
        applyLanguage(currentLanguage)
    }

    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.create(locale))
    }

    protected fun shareApp() {
        val shareMessage = getString(R.string.share_message)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))

    }

    protected fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data =
                "mailto:${getString(R.string.support_email)}?subject=${getString(R.string.support_subject)}&body=${
                    getString(R.string.support_body)
                }".toUri()
        }
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main){
                    showFailPlaceholder(check = true, checkSupport = true)
                    delay(3000)
                    showFailPlaceholder(check = false, checkSupport = false)
                }
            }
        }
    }

    protected fun openAgreement() {
        CoroutineScope(Dispatchers.IO).launch {
            val isInternetAvailable: Boolean = checkInternetConnection()
            withContext(Dispatchers.Main){
                if (!isInternetAvailable){
                    showFailPlaceholder(check = true, checkSupport = false)
                    delay(timeMillis = 3000)
                    showFailPlaceholder(check = false, false)
                } else {
                    val agreementUrl: String = getString(R.string.agreement_url)
                    val browserIntent = Intent(Intent.ACTION_VIEW, agreementUrl.toUri())
                    startActivity(browserIntent)
                }
            }
        }
    }

    private fun checkInternetConnection(): Boolean {
        return try {
            val response: Response<Void> = RetrofitInstance.api.checkInternetConnection().execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun showFailPlaceholder(check: Boolean, checkSupport: Boolean) = if (check){
        hideAllViews(check = true)
        failTextView.visibility = VISIBLE
        failTextView.isEnabled = checkSupport
        failTextView.text = this.getString( if (checkSupport) R.string.supportEmail else R.string.networkFail)
    } else {
        hideAllViews(check = false)
        failTextView.visibility = GONE
    }

    private fun hideAllViews(check: Boolean) {
        for (i in 0 until mainLayout.childCount) {
            when (val view: View = mainLayout.getChildAt(i)) {
                is MaterialButton -> {
                    if (this is MainActivity) {
                        if (check) view.visibility = View.INVISIBLE
                        else view.visibility = VISIBLE
                    }
                }
                is TextView -> {
                    if (this is SettingsActivity) {
                        if (check) view.visibility = View.INVISIBLE
                        else view.visibility = VISIBLE
                    }
                }
                is SwitchMaterial -> {
                    if (this is SettingsActivity) {
                        if (check) view.visibility = View.INVISIBLE
                        else view.visibility = VISIBLE
                    }
                }
                is RecyclerView -> {
                    if (this is SearchActivity) {
                        if (check) view.visibility = View.INVISIBLE
                        else view.visibility = VISIBLE
                    }
                }
            }
        }
    }

    private fun ViewGroup.changeBtnTextColor(textColor: Int) {
        for (i in 0 until childCount) {
            when (val view: View? = getChildAt(i)) {
                is Button -> {
                    view.setTextColor(textColor)
                }
                is ViewGroup -> {
                    view.changeBtnTextColor(textColor)
                }
            }
        }
    }

    private fun ViewGroup.changeBtnIconColor(iconColor: Int) {
        val buttonIds = listOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6)

        for (id in buttonIds) {
            val button = findViewById<MaterialButton?>(id)
            button?.let {
                it.icon?.let { drawable ->
                    DrawableCompat.setTint(drawable, iconColor)
                    it.icon = drawable
                }
            }
        }

    }

    private fun ViewGroup.changeBtnBackgroundColor(backgroundColor: Int) {
        for (i in 0 until childCount) {
            when (val view: View? = getChildAt(i)) {
                is Button -> {
                    view.run { setBackgroundColor(backgroundColor) }
                }
                is ViewGroup -> {
                    view.run { changeBtnBackgroundColor(backgroundColor) }
                }
            }
        }
    }

    private fun ViewGroup.changeColor(randomColor: Int, isTextColor: Boolean? = null, ignoreViewId: Int? = null) {
        when (isTextColor) {
            true -> {
                changeColorView(randomColor, true, ignoreViewId)
            }
            false -> {
                changeColorView(randomColor, false, ignoreViewId)
            }
            else -> {
                children.forEach { view: View ->
                    if (view is Button) {
                        view.setBackgroundColor(randomColor)
                    }
                }
            }
        }
    }

    private fun ViewGroup.changeColorView(color: Int, isTextColor: Boolean, ignoreViewId: Int?) {
        children.forEach { view: View ->
            if (view.id == ignoreViewId) return@forEach
            when (view) {
                is TextView -> {
                    if (isTextColor) {
                        view.setTextColor(color)
                    } else {
                        val drawable = view.compoundDrawablesRelative[2]
                        drawable?.let {
                            it.setTint(color)
                            view.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, it, null)
                        }
                    }
                }
                is BottomNavigationView -> {
                }
                is ViewGroup -> {
                    view.changeColorView(color, isTextColor, ignoreViewId)
                }
            }
        }
    }
}