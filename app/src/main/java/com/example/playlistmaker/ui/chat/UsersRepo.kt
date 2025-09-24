package com.example.playlistmaker.ui.chat

import com.example.playlistmaker.ui.chat.model.UserUi
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UsersRepo {
    private val db = Firebase.firestore

    suspend fun ensureCurrentUser(uid: String) {
        val ref = db.collection("profiles").document(uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(
                mapOf(
                    "displayName" to "User ${uid.take(6)}",
                    "status" to "👋",
                    "photoUrl" to null,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    fun listenUsers(onList: (List<UserUi>) -> Unit) =
        db.collection("profiles")
            .orderBy("displayName", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map {
                    UserUi(
                        uid = it.id,
                        name = it.getString("displayName"),
                        status = it.getString("status"),
                        photoUrl = it.getString("photoUrl")
                    )
                } ?: emptyList()
                onList(list)
            }
}