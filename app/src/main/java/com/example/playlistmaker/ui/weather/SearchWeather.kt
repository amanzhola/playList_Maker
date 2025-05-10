package com.example.playlistmaker.ui.weather

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.ToolbarConfig
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.api.WeatherInteraction
import com.example.playlistmaker.domain.models.ForecastLocation
import com.example.playlistmaker.utils.Debounce
import com.example.playlistmaker.utils.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.UIUpdater
import kotlinx.coroutines.launch

class SearchWeather : BaseActivity() { // 🔁 👉 🌤️🧼🏗️✅

    // ❌ Удаляем прямые зависимости, они теперь в Creator
    // ❌  private val forecaBaseUrl = "https://fnw-us.foreca.com" //  👨‍💻✨
    // ❌ private val apiToken = "Bearer ..."
    // ❌ private val retrofit = Retrofit.Builder()...
    // ❌ private val forecaService = retrofit.create(ForecaApi::class.java)
    // ❌ private val networkService = NetworkService() // ✅ 📜

    // ✅ Получаем WeatherInteraction из Creator
    private val weatherInteraction: WeatherInteraction by lazy {
        Creator.provideWeatherInteraction()
    }

    private lateinit var uiUpdater: UIUpdater // ✅ 📜
    // 2️⃣ 🅰️ ⌨️ 📋 👉 🔤 🔍 👇
    private val debounce = Debounce(SEARCH_DEBOUNCE_DELAY)

    private val locations = ArrayList<ForecastLocation>()
    private val adapter = LocationsAdapter { showWeather(it) }

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    // 🏘️ ⛪ 🏙️ 🏡 🏛️ 🏞️ 🏠
    private lateinit var locationsList: RecyclerView

    private var isBottomNavVisible: Boolean = true

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        locationsList = findViewById(R.id.locations)

        // Инициализация UIUpdater ✅
        uiUpdater = UIUpdater(
            progressBar = findViewById(R.id.progressBar),
            placeholderMessage = placeholderMessage,
            recyclerView = locationsList
        )

        @Suppress("DEPRECATION") // ✏️
        savedInstanceState?.let { bundle -> // 🔒 🗄️ 👉 🔁 📝
            val savedQuery = bundle.getString("QUERY")
            savedQuery?.let { queryInput.setText(it) }

            // Обработка savedLocations 🧼
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

        // 1️⃣ Обработка кнопки "Поиск" ✍️ 📝 👉 ❌ 🕒
        searchButton.setOnClickListener {
            if (queryInput.text.isNotEmpty()) {
                search()
            }
        }

        // 2️⃣ 🅱️ Обработка текста с debounce  ⌨️ 📋 👉 🔤 🔍 ☝️
        /*
        queryInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                debounce.debounce {
                    val queryText = s.toString()
                    if (queryText.isNotEmpty()) {
                        search()
                    }
                }
            }
        })
        */

        // 👈 ⚙️
        findViewById<TextView>(R.id.bottom5).isSelected = true
    }


    override fun onSaveInstanceState(outState: Bundle) { // 👉 🔒 🗄️ 📝
        super.onSaveInstanceState(outState)
        outState.putString("QUERY", queryInput.text.toString()) // // 👉 📊 - Возвращаем иконку
        outState.putParcelableArrayList("LOCATIONS", ArrayList<ForecastLocation>(locations))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun search() {
        val query = queryInput.text.toString()
        uiUpdater.showLoading() // ✅ progress bar

        lifecycleScope.launch {
            weatherInteraction.searchLocations(query).collect { result ->
                when (result) {
                    is com.example.playlistmaker.domain.util.Resource.Success -> {
                        val foundLocations = result.data
                        if (!foundLocations.isNullOrEmpty()) {
                            locations.clear()
                            locations.addAll(foundLocations)
                            adapter.notifyDataSetChanged()
                            uiUpdater.showData()
                        } else {
                            uiUpdater.showMessage(getString(R.string.nothing_found_city))
                        }
                    }
                    is com.example.playlistmaker.domain.util.Resource.Error -> {
                        uiUpdater.showMessage(getString(R.string.something_went_wrong))
                    }
                }
            }
        }
    }

    private fun showWeather(location: ForecastLocation) {

        lifecycleScope.launch {
            weatherInteraction.getCurrentWeather(location.id).collect { result ->
                when (result) {
                    is com.example.playlistmaker.domain.util.Resource.Success -> {
                        val weatherData = result.data
                        if (weatherData != null) {
                            val message = "${location.name}: ${weatherData.temperature}°C\n(Ощущается как ${weatherData.feelsLikeTemp}°C)"
                            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        }
                    }
                    is com.example.playlistmaker.domain.util.Resource.Error -> {
                        Toast.makeText(applicationContext, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onSegment4Clicked() { // 👈 ⚙️
        if (isBottomNavVisible) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig { // 👈 ⚙️
        return ToolbarConfig(VISIBLE, R.string.weather) {
            navigateToMainActivity()
        }
    }

    override fun shouldEnableEdgeToEdge(): Boolean { // 👈 ⚙️
        return false
    }

    override fun getLayoutId(): Int { // 👈 ⚙️
        return R.layout.activity_search_weather
    }

    override fun getMainLayoutId(): Int { // 👈 ⚙️
        return R.id.main
    }
}