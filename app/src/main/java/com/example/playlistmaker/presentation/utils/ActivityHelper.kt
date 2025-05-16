package com.example.playlistmaker.presentation.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.MainActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.SettingsActivity
import com.example.playlistmaker.ui.audio.SearchActivity
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

interface ApiService { //  👨‍💻✨
    @GET("/") // Путь к ресурсу
    fun checkInternetConnection(): Call<Void>
}

object RetrofitInstance { // 📝
    private const val BASE_URL = "https://www.google.com/" // 🔗

    private val retrofit by lazy { // 🎵🎚️🚀
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { // 👨‍💻
        retrofit.create(ApiService::class.java)
    }
}

class ActivityHelper(
    private val activity: Activity,
    private val mainLayout: ViewGroup,
    private val failTextView: TextView
) {

    fun shareApp() {
        val shareMessage = activity.getString(R.string.share_message)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }
        activity.startActivity(Intent.createChooser(sendIntent, null))
    }

    fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data =
                "mailto:${activity.getString(R.string.support_email)}?subject=${
                    activity.getString(R.string.support_subject)
                }&body=${activity.getString(R.string.support_body)}".toUri()
        }

        try {
            activity.startActivity(emailIntent)
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {// 👇
                    showFailPlaceholder(check = true, checkSupport = true)
                    delay(3000)
                    showFailPlaceholder(check = false, checkSupport = false)
                }
            }
        }
    }

    fun openAgreement() {
        CoroutineScope(Dispatchers.IO).launch {
            val isInternetAvailable = checkInternetConnection()
            withContext(Dispatchers.Main) {
                if (!isInternetAvailable) { // 👇
                    showFailPlaceholder(check = true, checkSupport = false)
                    delay(3000)
                    showFailPlaceholder(check = false, checkSupport = false)
                } else {
                    val agreementUrl = activity.getString(R.string.agreement_url)
                    val browserIntent = Intent(Intent.ACTION_VIEW, agreementUrl.toUri())
                    activity.startActivity(browserIntent)
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

    private fun showFailPlaceholder(check: Boolean, checkSupport: Boolean) {
        if (check) {
            hideAllViews(true)
            failTextView.visibility = View.VISIBLE
            failTextView.isEnabled = checkSupport
            failTextView.text = activity.getString(
                if (checkSupport) R.string.supportEmail else R.string.networkFail
            )
        } else {
            hideAllViews(false)
            failTextView.visibility = View.GONE
        }
    }

    private fun hideAllViews(check: Boolean) {
        for (i in 0 until mainLayout.childCount) {
            val view = mainLayout.getChildAt(i)
            when (view) {
                is MaterialButton -> {
                    if (activity is MainActivity) view.visibility = if (check) View.INVISIBLE else View.VISIBLE
                }
                is TextView, is SwitchMaterial -> { // 🤔 😬
                    if (activity is SettingsActivity) view.visibility = if (check) View.INVISIBLE else View.VISIBLE
                }
                is RecyclerView -> {
                    if (activity is SearchActivity) view.visibility = if (check) View.INVISIBLE else View.VISIBLE
                }
            }
        }
    }
}
