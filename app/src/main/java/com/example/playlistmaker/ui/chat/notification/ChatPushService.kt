package com.example.playlistmaker.ui.chat.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.example.playlistmaker.roots.main.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.math.max

class ChatPushService : FirebaseMessagingService() {

    companion object {
        private const val PREF = "badge_prefs"
        private const val KEY_UNREAD = "unread_count"

        private const val CHANNEL_BADGE = "chat_badge"
        private const val SUMMARY_ID = 4242
    }

    private fun prefs() = getSharedPreferences(PREF, MODE_PRIVATE)
    private fun unread() = prefs().getInt(KEY_UNREAD, 0)
    @SuppressLint("UseKtx")
    private fun setUnread(v: Int) = prefs().edit().putInt(KEY_UNREAD, max(0, v)).apply()

    @SuppressLint("UseKtx")
    override fun onMessageReceived(msg: RemoteMessage) {
        android.util.Log.d("CHAT_PUSH", "onMessageReceived data=${msg.data}")

        val myUid = com.google.firebase.ktx.Firebase.auth.currentUser?.uid
        if (myUid != null && msg.data["senderId"] == myUid) return

        val newCount = msg.data["unreadCount"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0

        // локально сохраним (если хочешь)
        getSharedPreferences("badge_prefs", MODE_PRIVATE)
            .edit().putInt("unread_count", newCount).apply()

        // Для OEM-лаунчеров — цифра:
        try { me.leolin.shortcutbadger.ShortcutBadger.applyCount(applicationContext, newCount) } catch (_: Throwable) {}
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun showBadgeSummary(count: Int) {
        val nm = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_BADGE,
                "Chat badge",
                NotificationManager.IMPORTANCE_DEFAULT // не MIN
            ).apply {
                setShowBadge(true)
                setSound(null, null)
                enableVibration(false)
                description = "Badge summary"
            }
            nm.createNotificationChannel(ch)
        }

        if (count <= 0) {
            nm.cancel(SUMMARY_ID)
            return
        }

        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = "open_chats"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_BADGE)
            .setSmallIcon(com.example.playlistmaker.R.drawable.ic_stat_chat)
            .setContentTitle(getString(com.example.playlistmaker.R.string.chats_title))
            .setContentText(getString(com.example.playlistmaker.R.string.you_have_unread))
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setOngoing(false)
            .setNumber(count) // критично для бейджа
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .build()

        nm.notify(SUMMARY_ID, n)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        com.example.playlistmaker.ui.chat.notification.FcmTokenManager.uploadFcmToken(token)
    }
}
