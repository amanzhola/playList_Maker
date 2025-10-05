package com.example.playlistmaker.data.migrations

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Одноразовая миграция: гарантирует, что у моих чатов поле participants — это List<String>.
 * Нужна, чтобы правила допускают чтение и не сыпался PERMISSION_DENIED.
 */
suspend fun fixMyChatsParticipants(myUid: String) {
    val db = FirebaseFirestore.getInstance()

    // Берём только те чаты, где я точно в массиве (это безопасно для правил)
    val qs = db.collection("chats")
        .whereArrayContains("participants", myUid)
        .get()
        .await()

    val batch = db.batch()

    for (d in qs.documents) {
        val p = d.get("participants")
        val list = (p as? List<*>)?.filterIsInstance<String>()
        if (list == null) {
            // Чат «битый»: приводим к корректному формату
            batch.set(
                d.reference,
                mapOf("participants" to listOf(myUid)),
                SetOptions.merge()
            )
        }
    }

    batch.commit().await()
}
