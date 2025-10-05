package com.example.playlistmaker.ui.chat

import android.util.Log
import com.example.playlistmaker.ui.chat.model.ChatItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.max

class ChatsRepo {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun listenMyChats(
        myUid: String,
        onList: (List<ChatItem>) -> Unit
    ): ListenerRegistration {
        return db.collection("chats")
            .whereArrayContains(
                "participants",
                myUid
            )      // ✅ один фильтр; без orderBy/visibleForMap
            // .limit(200)                                   // (необязательно) ограничь трафик, если чатов много
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(
                        "ChatsRepo",
                        "listenMyChats error code=${(e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code}",
                        e
                    )
                    return@addSnapshotListener
                }

                val docs = snap?.documents.orEmpty()

                // 1) видимость — фильтруем локально по visibleForMap.<myUid> == true
                val visible = docs.filter { d ->
                    (d.get("visibleForMap.$myUid") as? Boolean) == true
                }

                // 2) маппинг как у тебя
                val mapped = visible.map { d ->
                    val lastMsgTs = (d.get("lastMessage.ts") as? Timestamp)
                        ?.toDate()?.time
                    val lastMsgSenderId = d.getString("lastMessage.senderId")

                    val effectiveUpdatedAt = max(
                        d.getTimestamp("updatedAt")?.toDate()?.time ?: 0L,
                        lastMsgTs ?: 0L
                    )

                    Log.d(
                        "ChatsRepo",
                        "chat=${d.id} " +
                                "upd=${d.getTimestamp("updatedAt")?.toDate()?.time ?: 0L} " +
                                "lastMsgTs=${lastMsgTs ?: -1} " +
                                "sender=${lastMsgSenderId ?: "null"} " +
                                "lastRead=${
                                    ((d.get("lastRead.$myUid") as? com.google.firebase.Timestamp)
                                        ?.toDate()?.time) ?: -1
                                }"
                    )

                    ChatItem(
                        id = d.id,
                        title = d.getString("title") ?: "",
                        updatedAt = effectiveUpdatedAt,
                        lastReadTs = (d.get("lastRead.$myUid") as? Timestamp)?.toDate()?.time,
                        participants = (d.get("participants") as? List<*>)?.filterIsInstance<String>()
                            .orEmpty(),
                        lastMsgTs = lastMsgTs,
                        lastMsgSenderId = lastMsgSenderId
                    )
                }

                // 3) сортировка локально по убыванию "актуальности"
                val sorted = mapped.sortedByDescending { it.updatedAt }

                onList(sorted)
            }
    }
}