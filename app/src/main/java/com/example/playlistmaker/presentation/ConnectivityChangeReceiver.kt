package com.example.playlistmaker.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ConnectivityChangeReceiver(
    private val onDisconnected: () -> Unit,
    private val isConnected: () -> Boolean
) : BroadcastReceiver() {

    private var lastConnected: Boolean? = null

    override fun onReceive(context: Context, intent: Intent?) {
        // На CONNECTIVITY_CHANGE система говорит «состояние изменилось», но не говорит какое.
        val now = isConnected()  // <- здесь используем NetworkStatusChecker
        val was = lastConnected

        // Показываем тост, только если было подключение -> стало нет
        if (was != null && was && !now) {
            onDisconnected()
        }
        lastConnected = now
    }
}
