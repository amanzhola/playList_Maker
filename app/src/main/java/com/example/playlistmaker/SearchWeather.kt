package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.weather.LocationsAdapter
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

class ForecaAuthRequest(val user: String, val password: String)
class ForecaAuthResponse(@SerializedName("access_token") val token: String)
interface ForecaApi {
    @POST("/authorize/token?expire_hours=-1")
    fun authenticate(@Body request: ForecaAuthRequest): Call<ForecaAuthResponse>
    @GET("/api/v1/location/search/{query}")
    fun getLocations(@Header("Authorization")
                     token: String, @Path("query") query: String): Call<LocationsResponse>
    @GET("/api/v1/current/{location}")
    fun getForecast(@Header("Authorization")
                    token: String, @Path("location") locationId: Int): Call<ForecastResponse>
}

data class ForecastLocation(val id: Int, val name: String, val country: String)
class LocationsResponse(val locations: ArrayList<ForecastLocation>)
data class CurrentWeather(val temperature: Float, val feelsLikeTemp: Float)
class ForecastResponse(val current: CurrentWeather)

class SearchWeather : BaseActivity() {

    private val forecaBaseUrl = "https://fnw-us.foreca.com" // Базовый URL для API
    private var token = "" // Токен авторизации
    private val retrofit = Retrofit.Builder()
        .baseUrl(forecaBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val forecaService = retrofit.create(ForecaApi::class.java) // Создаем сервис для работы с API

    private val locations = ArrayList<ForecastLocation>() // Список для хранения найденных городов
    private val adapter = LocationsAdapter { showWeather(it) } // Адаптер для списка

    private lateinit var searchButton: Button
    private lateinit var queryInput: EditText
    private lateinit var placeholderMessage: TextView
    private lateinit var locationsList: RecyclerView

    private var isBottomNavVisible: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация UI элементов
        placeholderMessage = findViewById(R.id.placeholderMessage)
        searchButton = findViewById(R.id.searchButton)
        queryInput = findViewById(R.id.queryInput)
        locationsList = findViewById(R.id.locations)

        // Установка адаптера для RecyclerView
        adapter.locations = locations
        locationsList.layoutManager = LinearLayoutManager(this)
        locationsList.adapter = adapter

        // Установка обработчика нажатия на кнопку "Найти"
        searchButton.setOnClickListener {
            if (queryInput.text.isNotEmpty()) {
                if (token.isEmpty()) {
                    authenticate() // Если токен пустой, выполнить аутентификацию
                } else {
                    search() // Иначе сразу выполнить поиск
                }
            }
        }

        findViewById<TextView>(R.id.bottom5).isSelected = true
    }

    private fun authenticate() {
        // Запрос на аутентификацию
        forecaService.authenticate(ForecaAuthRequest("amanzholaimov", "gXX4BKF7vncd")) // Замените USER и PASSWORD на реальные учётные данные
            .enqueue(object : Callback<ForecaAuthResponse> {
                override fun onResponse(call: Call<ForecaAuthResponse>, response: Response<ForecaAuthResponse>) {
                    if (response.code() == 200) {
                        token = response.body()?.token.toString() // Сохраняем токен
                        search() // Переходим к поиску
                    } else {
                        showMessage(getString(R.string.something_went_wrong), response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ForecaAuthResponse>, t: Throwable) {
                    showMessage(getString(R.string.something_went_wrong), t.message.toString())
                }
            })
    }

    private fun search() {
        // Запрос поиск городов
        forecaService.getLocations("Bearer $token", queryInput.text.toString())
            .enqueue(object : Callback<LocationsResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<LocationsResponse>, response: Response<LocationsResponse>) {
                    when (response.code()) {
                        200 -> {
                            if (response.body()?.locations?.isNotEmpty() == true) {
                                locations.clear() // Очищаем предыдущие результаты
                                locations.addAll(response.body()?.locations!!)
                                adapter.notifyDataSetChanged() // Уведомляем адаптер об изменениях
                                showMessage("", "") // Скрыть сообщение
                            } else {
                                showMessage(getString(R.string.nothing_found_city), "")
                            }
                        }
                        401 -> authenticate() // Пытаемся заново аутентифицироваться
                        else -> showMessage(getString(R.string.something_went_wrong), response.code().toString())
                    }
                }

                override fun onFailure(call: Call<LocationsResponse>, t: Throwable) {
                    showMessage(getString(R.string.something_went_wrong), t.message.toString())
                }
            })
    }

    private fun showWeather(location: ForecastLocation) {
        // Запрос на получение погоды для конкретного города
        forecaService.getForecast("Bearer $token", location.id)
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    if (response.body()?.current != null) {
                        val message = "${location.name}: ${response.body()?.current?.temperature}°C\n(Ощущается как ${response.body()?.current?.feelsLikeTemp}°C)"
                        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show() // Показываем сообщение с погодой
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show() // Ошибка запроса
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String, additionalMessage: String) {
        // Метод для отображения сообщений об ошибках
        if (text.isNotEmpty()) {
            placeholderMessage.visibility = VISIBLE
            locations.clear()
            adapter.notifyDataSetChanged()
            placeholderMessage.text = text
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            placeholderMessage.visibility = View.GONE
        }
    }

    override fun onSegment4Clicked() {
        if (isBottomNavVisible) {
            hideBottomNavigation()
        } else {
            showBottomNavigation()
        }
        isBottomNavVisible = !isBottomNavVisible
    }

    override fun getToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(VISIBLE, R.string.weather) { navigateToMainActivity() }
    }

    override fun shouldEnableEdgeToEdge(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_search_weather
    }

    override fun getMainLayoutId(): Int {
        return R.id.main
    }
}