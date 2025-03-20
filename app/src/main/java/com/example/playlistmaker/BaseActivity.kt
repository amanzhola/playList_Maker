package com.example.playlistmaker

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale
import kotlin.random.Random
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import android.content.SharedPreferences
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

open class BaseActivity : AppCompatActivity(), CircleSegmentsView.OnSegmentClickListener {

    private lateinit var mainLayout: LinearLayout
    private lateinit var toolbar: Toolbar
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var colorPreferences: SharedPreferences
    private var isDarkTheme: Boolean = false
    private var currentLanguage: String = ""
    private val langKey = "language"
    protected val darkThemeKey = "isDarkTheme"
    protected var isDialogVisible = false
    protected lateinit var bottomNavigationView: BottomNavigationView
    protected lateinit var line: View
    private var currentIndex = 0
    private val baseSegmentColors = intArrayOf(
        R.color.hintFieldColor,
        R.color.blue
    )
    private val segmentColors = IntArray(6) { baseSegmentColors[it % 2] }
    private val newSegmentColors = IntArray(6) { baseSegmentColors[it % 2] }
    private lateinit var segmentTexts: Array<String>
    private lateinit var newSegmentTexts: Array<String>

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        mainLayout = findViewById(getMainLayoutId())

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        handleWindowInsets()

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

        if (this !is MainActivity) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        applySettings()
        colorPreferences = getColorSharedPreferences()
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    protected open fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    protected open fun getMainLayoutId(): Int {
        return R.id.activity_main
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun applySettings() {
        isDarkTheme = sharedPreferences.getBoolean(darkThemeKey, false)
        currentLanguage = sharedPreferences.getString(langKey, "ru") ?: "ru"
        setTheme(isDarkTheme)
        applyLanguage(currentLanguage)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getColorSharedPreferences(): SharedPreferences {
        return this.getSharedPreferences("color_preferences", Context.MODE_PRIVATE)
    }

    private fun getColorKey(segmentIndex: Int): String {
        val activityName = when (this) {
            is MainActivity -> "MainActivity"
            is SettingsActivity -> "SettingsActivity"
            is SearchActivity -> "SearchActivity"
            is MediaLibraryActivity -> "MediaLibraryActivity"
            else -> "UnknownActivity"
        }

        val themeSuffix = if (isDarkTheme) "_dark" else "_light"
        return "${activityName}_segment_$segmentIndex$themeSuffix"
    }

    private fun saveColor(segmentIndex: Int, color: Int) {
        colorPreferences.edit().apply {
            putInt(getColorKey(segmentIndex), color)
            apply()
        }
    }

    private fun loadColor(segmentIndex: Int): Int {
        return colorPreferences.getInt(getColorKey(segmentIndex), -1)
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

    open fun onSegment4Clicked() {}

    private fun setTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.create(locale)
        )
    }

    private fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        val editor = sharedPreferences.edit()
        editor.putBoolean(darkThemeKey, isDarkTheme)
        editor.apply()
        applySettings()
        if (this is SettingsActivity) switchControl.isChecked = isDarkTheme
    }

    private fun changeLanguage() {
        val newLanguage = if (currentLanguage == "ru") "en" else "ru"
        currentLanguage = newLanguage
        val editor = sharedPreferences.edit()
        editor.putString(langKey, newLanguage)
        editor.apply()
        applyLanguage(currentLanguage)
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
            data = Uri.parse("mailto:${getString(R.string.support_email)}?subject=${getString(R.string.support_subject)}&body=${getString(R.string.support_body)}")
        }

        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open email", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun openAgreement() {
        val agreementUrl = getString(R.string.agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))
        startActivity(browserIntent)
    }

    protected fun setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        line =findViewById(R.id.navigationLine)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_search -> {
                    if (!isThisActivity(SearchActivity::class.java)) {
                        startActivityWithAnimation(SearchActivity::class.java)
                    }
                    true
                }
                R.id.navigation_media -> {
                    if (!isThisActivity(MediaLibraryActivity::class.java)) {
                        startActivityWithAnimation(MediaLibraryActivity::class.java)
                    }
                    true
                }
                R.id.navigation_settings -> {
                    if (!isThisActivity(SettingsActivity::class.java)) {
                        startActivityWithAnimation(SettingsActivity::class.java)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startActivityWithAnimation(targetActivity: Class<*>) {
        if (this::class.java != targetActivity) {
            val intent = Intent(this, targetActivity)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.enter_from_bottom, R.anim.exit_to_top)
            startActivity(intent, options.toBundle())
            finish()
        }
    }

    private fun isThisActivity(targetActivity: Class<*>): Boolean {
        return this::class.java == targetActivity
    }

    private fun getNextColor(context: Context): Int {
        val colorResId = ColorProvider.colors[currentIndex]
        val color = context.getColor(colorResId)

        currentIndex = (currentIndex + 1) % ColorProvider.colors.size
        return color
    }

    // option for getNextColor
    private fun getRandomColor(context: Context): Int {
        val randomColorResId = ColorProvider.colors[Random.nextInt(ColorProvider.colors.size)]
        return context.getColor(randomColorResId)
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

        val buttonIds = listOf(R.id.Button_Big1, R.id.Button_Big2, R.id.Button_Big3)

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