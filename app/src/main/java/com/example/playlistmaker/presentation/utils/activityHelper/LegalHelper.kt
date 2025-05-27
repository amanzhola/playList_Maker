//package com.example.playlistmaker.presentation.utils.activityHelper
//
//import android.app.Activity
//import android.content.Intent
//import androidx.core.net.toUri
//import com.example.playlistmaker.R
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class LegalHelper( // 📄
//    private val activity: Activity,
//    private val networkChecker: NetworkChecker,
//    private val failUiController: FailUiController
//) {
//
//    fun openAgreement() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val isConnected = networkChecker.isInternetAvailable()
//            withContext(Dispatchers.Main) {
//                if (isConnected) {
//                    val agreementUrl = activity.getString(R.string.agreement_url).toUri()
//                    activity.startActivity(Intent(Intent.ACTION_VIEW, agreementUrl))
//                } else { // 👇
//                    failUiController.showTemporaryError(isSupport = false)
//                }
//            }
//        }
//    }
//}
