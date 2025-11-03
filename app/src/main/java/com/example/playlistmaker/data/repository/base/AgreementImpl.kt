package com.example.playlistmaker.data.repository.base

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.repository.base.Agreement
import com.example.playlistmaker.domain.usecases.base.CheckInternetConnectionUseCase
import com.example.playlistmaker.utils.showFailOrSnack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgreementImpl( // 📄
    private val activity: Activity,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase
) : Agreement {

    override fun openAgreement() {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = withContext(Dispatchers.IO) {
                checkInternetConnectionUseCase.execute()
            }

            if (isConnected) {
                val agreementUrl = activity.getString(R.string.agreement_url).toUri()
                activity.startActivity(Intent(Intent.ACTION_VIEW, agreementUrl))
            } else { // 👇
                activity.showFailOrSnack(isSupport = false)
                return@launch
            }
        }
    }
}
