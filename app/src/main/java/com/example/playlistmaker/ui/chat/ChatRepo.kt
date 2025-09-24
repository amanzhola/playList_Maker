package com.example.playlistmaker.ui.chat

import android.net.Uri
import com.example.playlistmaker.ui.chat.model.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

class ChatRepo {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    // ---------- DM: детерминированный chatId ----------
    private fun dmId(a: String, b: String): String {
        val (x, y) = if (a < b) a to b else b to a
        return "dm_${x}_${y}"
    }

    // Создать/открыть DM с записью меты (type/participants/updatedAt)
    suspend fun createOrOpenDm(currentUid: String, otherUid: String): String {
        val id = dmId(currentUid, otherUid)
        val ref = db.collection("chats").document(id)
        ref.set(
            mapOf(
                "type" to "dm",
                "participants" to listOf(currentUid, otherUid),
                "title" to null,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        return id
    }

    // Групповой чат (2+ других + ты = >=3)
    suspend fun createGroup(currentUid: String, memberUids: List<String>, title: String): String {
        val all = (memberUids + currentUid).distinct()
        require(all.size >= 3) { "Групповой чат — минимум 3 участника" }
        val ref = db.collection("chats").document()
        ref.set(
            mapOf(
                "type" to "group",
                "participants" to all,
                "title" to title,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return ref.id
    }

    // ---------- Сообщения ----------
    fun listenMessages(chatId: String, onList: (List<Message>) -> Unit) =
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { d ->
                    Message(
                        id = d.id, chatId = chatId,
                        senderId = d.getString("senderId") ?: "",
                        text = d.getString("text"),
                        mediaUrl = d.getString("mediaUrl"),
                        createdAt = d.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }.orEmpty()
                onList(list)
            }

    suspend fun sendText(chatId: String, text: String) {
        val uid = auth.currentUser?.uid ?: error("No auth")
        val data = mapOf(
            "senderId" to uid,
            "text" to text,
            "mediaUrl" to null,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("chats").document(chatId).collection("messages").add(data).await()
        // Обновим мету чата (для списка чатов)
        db.collection("chats").document(chatId)
            .set(mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }

    suspend fun sendImage(chatId: String, uri: Uri) {
        val uid = auth.currentUser?.uid ?: error("No auth")
        val ref = storage.reference.child("chatMedia/$chatId/${uid}_${System.currentTimeMillis()}.jpg")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        val data = mapOf(
            "senderId" to uid,
            "text" to null,
            "mediaUrl" to url,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("chats").document(chatId).collection("messages").add(data).await()
        db.collection("chats").document(chatId)
            .set(mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }

    // старый вариант можно оставить (совместим), но лучше пользоваться createOrOpenDm(...)
    suspend fun getOrCreateDm(uidA: String, uidB: String): String {
        val s = listOf(uidA, uidB).sorted()
        val chatId = "${s[0]}__${s[1]}" // ОСТАВЛЯЮ для обратной совместимости
        val ref = db.collection("chats").document(chatId)
        db.runTransaction { tx ->
            if (!tx.get(ref).exists()) tx.set(
                ref, mapOf(
                    "type" to "dm",
                    "participants" to s,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
        return chatId
    }
}
