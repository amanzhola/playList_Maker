package com.example.playlistmaker.ui.weather

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
//import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.data.dto.weather.CurrentWeatherDto
import com.example.playlistmaker.domain.api.weather.WeatherInteraction
import com.example.playlistmaker.domain.models.weather.ForecastLocation
import com.example.playlistmaker.domain.util.Resource
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.UIUpdater
import com.example.playlistmaker.utils.collectDebouncedIn
import com.example.playlistmaker.utils.showLongSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class SearchWeather : BaseActivity() {

    // ————— State ————————————————————————————————————————————
    private var isBottomNavVisible: Boolean = true
    private var isFallbackDialogShowing = false

    // ————— DI / UI helpers ————————————————————————————————
    private val weatherInteraction: WeatherInteraction by inject()
    private lateinit var uiUpdater: UIUpdater

    private val queryFlow = MutableStateFlow("")
    private val locations = ArrayList<ForecastLocation>()
    private val adapter = LocationsAdapter { showWeather(it) }

    // ————— Views ——————————————————————————————————————————————
    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var locationsList: RecyclerView

    // ————— Lifecycle ———————————————————————————————————————
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        locationsList = findViewById(R.id.locations)

        uiUpdater = UIUpdater(
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = placeholderMessage,
            recyclerView = locationsList
        )

        @Suppress("DEPRECATION")
        savedInstanceState?.let { bundle ->
            val savedQuery = bundle.getString("QUERY")
            savedQuery?.let { queryInput.setText(it) }

            bundle.getParcelableArrayList<ForecastLocation>("LOCATIONS")?.let { savedLocations ->
                locations.clear()
                locations.addAll(savedLocations)
                adapter.notifyDataSetChanged()
                uiUpdater.showData()
            }
        }

        adapter.locations = locations
        locationsList.layoutManager = LinearLayoutManager(this)
        locationsList.adapter = adapter

        searchButton.setOnClickListener {
            val queryText = queryInput.text.toString()
            if (queryText.isNotEmpty()) search(queryText)
        }

        queryInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                queryFlow.value = s.toString()
            }
        })

        queryFlow.collectDebouncedIn(lifecycleScope, SEARCH_DEBOUNCE_DELAY) { queryText ->
            if (queryText.isNotEmpty()) search(queryText)
        }

        findViewById<TextView>(R.id.bottom5).isSelected = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("QUERY", queryInput.text.toString())
        outState.putParcelableArrayList("LOCATIONS", ArrayList<ForecastLocation>(locations))
    }

    // ————— Search (Foreca) + fallback trigger ————————————————
    @SuppressLint("NotifyDataSetChanged")
    private fun search(query: String) {
        uiUpdater.showLoading()
        lifecycleScope.launch {
            weatherInteraction.searchLocations(query).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val found = result.data.orEmpty()
                        if (found.isNotEmpty()) {
                            locations.clear()
                            locations.addAll(found)
                            adapter.notifyDataSetChanged()
                            uiUpdater.showData()
                        } else {
                            // Foreca пусто → спрашиваем страну (убираем прогресс внутри диалога)
                            promptCountryThenWttrForQuery(query)
                        }
                    }
                    is Resource.Error -> {
                        // Foreca дала ошибку → тоже фоллбек
                        promptCountryThenWttrForQuery(query)
                    }
                }
            }
        }
    }

    // ————— Show weather for chosen Foreca location ——————————
    private fun showWeather(location: ForecastLocation) {
        lifecycleScope.launch {
            weatherInteraction.getCurrentWeather(location.id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val w = result.data ?: return@collect
                        showLongSnack("${location.name}: ${w.temperature}°C\n(Ощущается как ${w.feelsLikeTemp}°C)")
                    }
                    is Resource.Error -> {
                        // Foreca упала → фоллбек
                        askCountryThenWttr(location)
                    }
                }
            }
        }
    }

    // ————— Unified fallback dialog ————————————————————————————
    private fun openCountryDialog(
        initialCountry: String?,
        onChosen: (country: String) -> Unit
    ) {
        if (isFallbackDialogShowing) return
        isFallbackDialogShowing = true

        // Сбрасываем прогресс, чтобы он не «горел» под диалогом
        uiUpdater.showMessage(getString(R.string.fallback_prompt))

        val input = EditText(this).apply {
            hint = getString(R.string.country_hint)
            setText(initialCountry.orEmpty())
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.specify_country))
            .setView(input)
            .setOnDismissListener { isFallbackDialogShowing = false }
            .setPositiveButton(android.R.string.ok) { d, _ ->
                d.dismiss()
                val country = input.text.toString().trim()
                if (country.isEmpty()) {
                    showLongSnack(getString(R.string.country_required))
                    return@setPositiveButton
                }
                onChosen(country)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // Ветка: с экрана поиска (Foreca ничего не вернула/ошибка)
    @SuppressLint("NotifyDataSetChanged")
    private fun promptCountryThenWttrForQuery(city: String) {
        openCountryDialog(initialCountry = null) { country ->
            lifecycleScope.launch { fetchAndShowWttr(city, country) }
        }
    }

    // Ветка: у нас есть Foreca-локация, но погода не загрузилась
    @SuppressLint("NotifyDataSetChanged")
    private fun askCountryThenWttr(location: ForecastLocation) {
        openCountryDialog(initialCountry = location.country) { country ->
            lifecycleScope.launch { fetchAndShowWttr(location.name, country) }
        }
    }

    // ————— Common fallback path: geocode → wttr → show —————————
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun fetchAndShowWttr(city: String, country: String) {
        // 1) Геокод "город, страна" → (lat, lon)
        val coords = withContext(Dispatchers.IO) {
            geocodeCityCountry(this@SearchWeather, city, country)
        }
        if (coords == null) {
            val text = getString(R.string.coords_not_found, city, country)
            showLongSnack(text)
            uiUpdater.showMessage(text)
            return
        }

        // 2) По координатам → wttr
        val (lat, lon) = coords
        val wttr = withContext(Dispatchers.IO) { fetchWttrByCoords(lat, lon) }
        if (wttr == null) {
            val text = getString(R.string.wttr_failed)
            showLongSnack(text)
            uiUpdater.showMessage(text)
            return
        }

        // success
        val msg = "$city, $country: ${wttr.temperature}°C\n(Ощущается как ${wttr.feelsLikeTemp}°C)"
        showLongSnack(msg)

        // подложим виртуальную локацию и покажем список (уберёт прогресс)
        locations.clear()
        locations.add(ForecastLocation(id = -1, name = city, country = country))
        adapter.notifyDataSetChanged()
        uiUpdater.showData()
    }

    // ————— wttr JSON fetch (без DI) ————————————————————————
    private fun fetchWttrByCoords(lat: Double, lon: Double): CurrentWeatherDto? {
        val urlStr = "https://wttr.in/~$lat,$lon?format=j1"
        var conn: java.net.HttpURLConnection? = null
        return try {
            val url = java.net.URL(urlStr)
            val c = (url.openConnection() as? java.net.HttpURLConnection) ?: return null
            conn = c

            c.connectTimeout = 10_000
            c.readTimeout = 10_000
            c.setRequestProperty("User-Agent", "PlaylistMaker/1.0 (Android)")

            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return null

            val root = org.json.JSONObject(body)
            // Если пришла html/ошибка, current_condition может отсутствовать
            val arr = root.optJSONArray("current_condition") ?: return null
            val current = arr.optJSONObject(0) ?: return null

            val tempC = current.optString("temp_C", "").toFloatOrNull() ?: return null
            val feelsC = current.optString("FeelsLikeC", "").toFloatOrNull() ?: tempC

            CurrentWeatherDto(temperature = tempC, feelsLikeTemp = feelsC)
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    // ————— Geocoder ————————————————————————————————————————————
    private fun geocodeCityCountry(
        ctx: android.content.Context,
        city: String,
        country: String
    ): Pair<Double, Double>? {
        return try {
            val geocoder = android.location.Geocoder(ctx, Locale.getDefault())
            val query = "$city, $country"

            if (android.os.Build.VERSION.SDK_INT >= 33) {
                val ref = AtomicReference<Pair<Double, Double>?>()
                val latch = CountDownLatch(1)
                geocoder.getFromLocationName(query, 1) { list ->
                    val addr = list.firstOrNull()
                    ref.set(addr?.let { it.latitude to it.longitude })
                    latch.countDown()
                }
                // ждём чуть-чуть, чтобы не зависать навсегда
                latch.await(2, TimeUnit.SECONDS)
                ref.get()
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocationName(query, 1)
                val addr = list?.firstOrNull() ?: return null
                addr.latitude to addr.longitude
            }
        } catch (_: Exception) {
            null
        }
    }

    // ————— Toolbar / Bottom nav ———————————————————————————————
    override fun onSegment4Clicked() {
        if (isBottomNavVisible) hideBottomNavigation() else showBottomNavigation()
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.weather) {
            navigateToMainScreen(this@SearchWeather, -1)
        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean = false
    override fun getLayoutId(): Int = R.layout.activity_search_weather
    override fun getMainLayoutId(): Int = R.id.main
}
