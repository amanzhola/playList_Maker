
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

suspend fun migrateChatsFor(myUid: String) {
    val db = FirebaseFirestore.getInstance()
    val all = db.collection("chats").get().await()

    val batch = db.batch()
    for (d in all.documents) {
        val raw = d.get("participants")
        val list = (raw as? List<*>)?.filterIsInstance<String>()

        // Если participants нет/битый — делаем валидный список
        if (list == null || list.isEmpty()) {
            batch.set(d.reference, mapOf("participants" to listOf(myUid)), SetOptions.merge())
        }

        // Если нет updatedAt — проставим serverTimestamp (или можешь подтянуть ts последнего сообщения)
        if (d.getTimestamp("updatedAt") == null) {
            batch.set(d.reference, mapOf("updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
        }
    }
    batch.commit().await()
}
