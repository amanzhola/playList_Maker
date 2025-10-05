package com.example.playlistmaker.ui.chat

import com.example.playlistmaker.ui.chat.model.UserUi
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UsersRepo {

    private val db = Firebase.firestore

    // Кэш имён, чтобы не дёргать сеть повторно
    private val nameCache = mutableMapOf<String, String?>()

    /** 1) Разово получить имя пользователя по uid (+ кэш). */
    suspend fun fetchDisplayName(uid: String): String? {
        nameCache[uid]?.let { return it }
        val snap = db.collection("profiles").document(uid).get().await()
        val name = snap.getString("displayName")
        nameCache[uid] = name
        return name
    }

    /** 2) LIVE-подписка на профиль — получаем UserUi и автообновления. */
    fun listenProfile(uid: String, on: (UserUi?) -> Unit): ListenerRegistration {
        return db.collection("profiles").document(uid)
            .addSnapshotListener { d, _ ->
                if (d == null || !d.exists()) { on(null); return@addSnapshotListener }
                val ui = UserUi(
                    uid = d.id,
                    name = d.getString("displayName"),
                    photoUrl = d.getString("photoUrl"),
                    status = d.getString("status")
                )
                nameCache[uid] = ui.name
                on(ui)
            }
    }

    /** 3) Убедиться, что профиль для текущего uid существует. */
    suspend fun ensureCurrentUser(uid: String) {
        val ref = db.collection("profiles").document(uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(
                mapOf(
                    "displayName" to "User ${uid.take(6)}",
                    "status" to "Great Day👋",
                    "photoUrl" to null,
                    "lastSeen" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    /** 4) Проверить, что пользователь существует. */
    suspend fun exists(uid: String): Boolean {
        if (uid.isBlank()) return false
        val snap = db.collection("profiles").document(uid).get().await()
        return snap.exists()
    }

    /**
     * 5) Поиск по имени (prefix search) + точное совпадение по UID.
     * Важно: для orderBy("displayName") должен быть создан индекс/поле в профилях.
     */
    suspend fun searchByNameOrId(
        query: String,
        excludeUid: String? = null,
        limit: Int = 20
    ): List<UserUi> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val out = mutableListOf<UserUi>()

        // По имени (prefix)
        val byName = db.collection("profiles")
            .orderBy("displayName", Query.Direction.ASCENDING)
            .startAt(q)
            .endAt(q + "\uf8ff")
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { d ->
                val uid = d.id
                if (excludeUid != null && uid == excludeUid) return@mapNotNull null
                UserUi(
                    uid = uid,
                    name = d.getString("displayName"),
                    status = d.getString("status"),
                    photoUrl = d.getString("photoUrl")
                )
            }
        out += byName

        // Пробуем точное совпадение по UID (если запрос похож на uid)
        if (q.length >= 4) {
            val doc = db.collection("profiles").document(q).get().await()
            if (doc.exists()) {
                val uid = doc.id
                val notExcluded = excludeUid == null || uid != excludeUid
                val notDuplicated = out.none { it.uid == uid }
                if (notExcluded && notDuplicated) {
                    out += UserUi(
                        uid = uid,
                        name = doc.getString("displayName"),
                        status = doc.getString("status"),
                        photoUrl = doc.getString("photoUrl")
                    )
                }
            }
        }

        return out.take(limit)
    }

    /** 6) Обновление полей профиля. Любые null поля — пропускаются. */
    suspend fun updateProfile(
        uid: String,
        displayName: String?,
        status: String?,
        photoUrl: String?
    ) {
        val data = mutableMapOf<String, Any?>(
            "displayName" to displayName,
            "status" to status,
            "photoUrl" to photoUrl,
            "lastSeen" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        ).filterValues { it != null }

        if (displayName != null) nameCache[uid] = displayName

        db.collection("profiles").document(uid)
            .set(data, SetOptions.merge())
            .await()
    }
}
