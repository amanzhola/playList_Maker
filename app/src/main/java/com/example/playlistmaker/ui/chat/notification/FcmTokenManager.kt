package com.example.playlistmaker.ui.chat.notification

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

object FcmTokenManager {

    /** Форс-регистрация токена после логина/старта приложения */
    fun forceRegisterFcmToken(context: Context) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.messaging.token
            .addOnSuccessListener { token ->
                Log.d("FCM", "Got token: $token")
                uploadFcmToken(uid, token)
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Token fetch failed", e)
            }
    }

    /** Внутренняя запись токена в Firestore (users/{uid}/fcmTokens/{token}) */
    fun uploadFcmToken(uid: String, token: String) {
        val db = Firebase.firestore
        val ref = db.collection("users").document(uid)
            .collection("fcmTokens").document(token)

        val data = mapOf(
            "token" to token,
            "platform" to "android",
            "updatedAt" to Timestamp.now()
        )
        ref.set(data)
    }

    /** Очистка токенов при выходе из аккаунта */
    suspend fun onLogoutCleanup() {
        // удалить локальный токен устройства (чтоб не приходили пуши на этот аккаунт)
        FirebaseMessaging.getInstance().deleteToken().await()

        // удалить записи токенов из Firestore для текущего uid (если ещё залогинен)
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore
        val snap = db.collection("users").document(uid)
            .collection("fcmTokens").get().await()
        for (doc in snap.documents) {
            doc.reference.delete().await()
        }
    }

    /** Удобная перегрузка: берёт uid из FirebaseAuth */
    fun uploadFcmToken(token: String) {
        val uid = com.google.firebase.ktx.Firebase.auth.currentUser?.uid ?: return
        uploadFcmToken(uid, token)
    }
}
