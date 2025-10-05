package com.example.playlistmaker.ui.chat

import android.net.Uri
import com.example.playlistmaker.ui.chat.model.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class ChatRepo {

    // Единый KTX доступ
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    /** Дет. id для DM (лексикографическая нормализация) */
    private fun dmId(a: String, b: String): String {
        val (x, y) = if (a < b) a to b else b to a
        return "dm_${x}_${y}"
    }

    /**
     * Создать/открыть DM и поднять updatedAt + изначально сделать видимым обоим
     * (idempotent: set(..., merge=true))
     */
    suspend fun createOrOpenDm(me: String, other: String): String {
        require(me.isNotBlank() && other.isNotBlank() && me != other) { "Неверный UID" }
        val id = dmId(me, other)
        val uniq = listOf(me, other)
        val visMap = uniq.associateWith { true }
        db.collection("chats").document(id)
            .set(
                mapOf(
                    "type"          to "dm",
                    "participants"  to uniq,
                    "visibleFor"    to uniq,
                    "visibleForMap" to visMap,
                    "updatedAt"     to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
        return id
    }

    /** Гарантированно создать/домерджить шапку чата (безопасный idempotent апдейт) */
    suspend fun ensureChatHeader(chatId: String, participants: List<String>) {
        if (participants.isEmpty()) return
        val uniq = participants.distinct()
        val visMap = uniq.associateWith { true }
        db.collection("chats").document(chatId)
            .set(
                mapOf(
                    "participants"  to uniq,
                    "visibleFor"    to uniq,
                    "visibleForMap" to visMap,
                    "updatedAt"     to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    /** Скрыть чат из списка для me (не удаляя историю) */
    suspend fun hideFor(chatId: String, me: String) {
        val ref = db.collection("chats").document(chatId)
        ref.update(
            mapOf(
                "visibleFor" to FieldValue.arrayRemove(me),
                "visibleForMap.$me" to FieldValue.delete()
            )
        ).await()
    }

    /** Вернуть чат в список для me */
    suspend fun unhideFor(chatId: String, me: String) {
        val ref = db.collection("chats").document(chatId)
        ref.update(
            mapOf(
                "visibleFor" to FieldValue.arrayUnion(me),
                "visibleForMap.$me" to true
            )
        ).await()
    }

    /** Поставить/снять блокировку для участника (ключ в карте blocked) */
    suspend fun setBlocked(chatId: String, whoBlocksUid: String, blocked: Boolean) {
        val ref = db.collection("chats").document(chatId)
        if (blocked) {
            ref.set(mapOf("blocked" to mapOf(whoBlocksUid to true)), SetOptions.merge()).await()
        } else {
            ref.update("blocked.$whoBlocksUid", FieldValue.delete()).await()
        }
    }

    /** Мягкая клиентская проверка: собеседник меня заблокировал? */
    private suspend fun isBlockedForSender(chatId: String, senderUid: String): Boolean {
        val d = db.collection("chats").document(chatId).get().await()
        val parts = (d.get("participants") as? List<*>)?.filterIsInstance<String>().orEmpty()
        val peer  = parts.firstOrNull { it != senderUid } ?: return false
        return d.get("blocked.$peer") as? Boolean ?: false
    }

    /** Пометить чат прочитанным: lastRead.myUid = now() */
    suspend fun markChatRead(chatId: String, myUid: String) {
        db.collection("chats").document(chatId)
            .set(
                mapOf("lastRead" to mapOf(myUid to FieldValue.serverTimestamp())),
                SetOptions.merge()
            )
            .await()
    }

    /** LIVE-слушатель сообщений (ASC по времени) */
    fun listenMessages(chatId: String, onList: (List<Message>) -> Unit) =
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { d ->
                    Message(
                        id        = d.id,
                        chatId    = chatId,
                        senderId  = d.getString("senderId") ?: d.getString("author") ?: "",
                        text      = d.getString("text"),
                        mediaUrl  = d.getString("mediaUrl") ?: d.getString("imageUrl"),
                        createdAt = d.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }.orEmpty()
                onList(list)
            }

    /**
     * Отправка текста + атомарное обновление «шапки» (updatedAt / lastMessage)
     * Делается одним batch’ем, чтобы индикатор «новое» срабатывал стабильно.
     */
    suspend fun sendText(chatId: String, text: String) {
        val uid = auth.currentUser?.uid ?: error("No auth")

        // опционально: клиентская проверка блока
        if (isBlockedForSender(chatId, uid)) {
            throw IllegalStateException("Пользователь вас заблокировал")
        }

        val chatRef = db.collection("chats").document(chatId)
        val msgRef  = chatRef.collection("messages").document()

        val batch = db.batch()
        val now   = FieldValue.serverTimestamp()

        val msg = mapOf(
            "senderId"  to uid,
            "text"      to text,
            "mediaUrl"  to null,
            "createdAt" to now
        )
        batch.set(msgRef, msg)

        val header = mapOf(
                "updatedAt"   to now,
        "lastMessage" to mapOf(
            "type"      to "text",
            "text"      to text.take(100),
            "ts"        to now,
            "senderId"  to uid   // 👈 добавили
        )
        )

        batch.set(chatRef, header, SetOptions.merge())

        batch.commit().await()
    }

    /**
     * Отправка картинки: грузим в Storage, пишем сообщение, обновляем шапку.
     * Тоже атомарно по смыслу (сообщение и шапка — одним batch’ем).
     */
    suspend fun sendImage(chatId: String, uri: Uri) {
        val uid = auth.currentUser?.uid ?: error("No auth")

        if (isBlockedForSender(chatId, uid)) {
            throw IllegalStateException("Пользователь вас заблокировал")
        }

        // 1) Upload в Storage
        val fileRef = storage.reference
            .child("chatMedia/$chatId/${uid}_${System.currentTimeMillis()}.jpg")
        fileRef.putFile(uri).await()
        val url = fileRef.downloadUrl.await().toString()

        // 2) Сообщение + «шапка» батчем
        val chatRef = db.collection("chats").document(chatId)
        val msgRef  = chatRef.collection("messages").document()

        val batch = db.batch()
        val now   = FieldValue.serverTimestamp()

        val msg = mapOf(
            "senderId"  to uid,
            "text"      to null,
            "mediaUrl"  to url,
            "createdAt" to now
        )
        batch.set(msgRef, msg)

        val header = mapOf(
            "updatedAt"   to now,
            "lastMessage" to mapOf(
                "type"     to "image",
                "ts"       to now,
                "senderId" to uid   // 👈 добавили
            )
        )
        batch.set(chatRef, header, SetOptions.merge())

        batch.commit().await()
    }
}
