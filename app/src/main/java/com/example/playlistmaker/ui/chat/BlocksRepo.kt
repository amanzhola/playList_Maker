package com.example.playlistmaker.ui.chat

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class BlocksRepo {
    private val db = Firebase.firestore

    /** LIVE: множество uid'ов, которых текущий пользователь заблокировал */
    fun listenBlockedSet(myUid: String, on: (Set<String>) -> Unit): ListenerRegistration =
        db.collection("profiles").document(myUid).collection("blocks")
            .addSnapshotListener { snap, _ ->
                val set = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                on(set)
            }

    /** Поставить блок: myUid блокирует peerUid */
    suspend fun block(myUid: String, peerUid: String) {
        db.collection("profiles").document(myUid)
            .collection("blocks").document(peerUid)
            .set(
                mapOf(
                    "blocked" to true,
                    "ts" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    /** Снять блок */
    suspend fun unblock(myUid: String, peerUid: String) {
        db.collection("profiles").document(myUid)
            .collection("blocks").document(peerUid)
            .delete()
            .await()
    }

    /** Разовое: проверить, заблокирован ли peerUid пользователем myUid */
    suspend fun isBlocked(myUid: String, peerUid: String): Boolean {
        val d = db.collection("profiles").document(myUid)
            .collection("blocks").document(peerUid)
            .get().await()
        return d.exists()
    }
}
