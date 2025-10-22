package com.example.playlistmaker.data.repository.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.repository.base.Support
import com.example.playlistmaker.domain.usecases.base.CheckInternetConnectionUseCase
import com.example.playlistmaker.utils.showFailOrSnack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportImpl(
    private val activity: Activity,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase
) : Support {

    @SuppressLint("UseKtx")
    override fun writeToSupport() {
        // запуск с UI-потока
        CoroutineScope(Dispatchers.Main).launch {
            val hasInternet = withContext(Dispatchers.IO) {
                checkInternetConnectionUseCase.execute() // suspend
            }

            if (!hasInternet) {
                activity.showFailOrSnack(isSupport = true)
                return@launch
            }

            val email   = activity.getString(R.string.support_email)
            val subject = activity.getString(R.string.support_subject)
            val body    = activity.getString(R.string.support_body)

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")          // важен mailto-схема, без адреса тоже ок
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)    // \n обработается нормально
            }

            try {
                activity.startActivity(emailIntent)
            } catch (_: Exception) {
                activity.showFailOrSnack(isSupport = true)
            }
        }
    }
}
