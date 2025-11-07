// functions/index.js
const functions = require('firebase-functions/v1'); // Gen1 ok
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();

/**
 * Новое сообщение:
 *  - исключаем автора
 *  - учитываем visibleForMap
 *  - ставим unreadFlags.{uid}=true для получателя (если ещё не стоял)
 *  - считаем КОЛ-ВО НЕПРОЧИТАННЫХ ЧАТОВ для пользователя
 *  - шлём FCM с unreadCount = количеству непрочитанных чатов
 */
exports.sendChatPush = functions
  .region('us-central1')
  .firestore
  .document('chats/{chatId}/messages/{msgId}')
  .onCreate(async (snap, ctx) => {
    const msg = snap.data();
    const chatId = ctx.params.chatId;
    const senderId = msg.senderId || msg.author || '';

    console.log('sendChatPush new msg', { chatId, senderId });

    // 1) Чат
    const chatRef = db.collection('chats').doc(chatId);
    const chatDoc = await chatRef.get();
    if (!chatDoc.exists) return null;

    const participants = chatDoc.get('participants') || [];
    const visibleForMap = chatDoc.get('visibleForMap') || {};
    const chatTitle = chatDoc.get('title') || 'Чат';

    // 2) Получатели (не автор, и у кого чат виден)
    const recipients = participants.filter((uid) => {
      if (uid === senderId) return false;
      const vis = Object.prototype.hasOwnProperty.call(visibleForMap, uid)
        ? visibleForMap[uid] === true
        : true;
      return vis;
    });
    if (!recipients.length) return null;

    console.log('recipients', recipients);

    // 3) Собираем токены
    const tokenSnaps = await Promise.all(
      recipients.map((uid) =>
        db.collection('users').doc(uid).collection('fcmTokens').get()
      )
    );
    const tokensByUid = new Map(); // uid -> string[]
    recipients.forEach((uid, i) => {
      const arr = [];
      tokenSnaps[i].forEach((d) => {
        const t = d.get('token');
        if (t) arr.push(t);
      });
      if (arr.length) tokensByUid.set(uid, arr);
    });
    if (tokensByUid.size === 0) return null;

    console.log('tokensByUid.size', tokensByUid.size);

    // 4) Превью текста
    const textPreview =
      (msg.text ? String(msg.text) : '').slice(0, 120) || 'Новое сообщение';

    // 5) Для каждого получателя:
    for (const [uid, tokens] of tokensByUid.entries()) {
      // 5.1. В транзакции — если флаг ещё не стоял, ставим unreadFlags.{uid}=true
      await db.runTransaction(async (tx) => {
        const fresh = await tx.get(chatRef);
        const unreadFlags = fresh.get('unreadFlags') || {};
        const alreadyUnread = unreadFlags[uid] === true;
        if (!alreadyUnread) {
          tx.update(chatRef, { [`unreadFlags.${uid}`]: true });
        }
      });

      // 5.2. Считаем СКОЛЬКО ЧАТОВ НЕПРОЧИТАНО (unreadFlags.uid == true)
      //     Это даёт цифру «сколько разных людей/диалогов ждёт ответа»
      const unreadChatsSnap = await db.collection('chats')
        .where(`unreadFlags.${uid}`, '==', true)
        .get();
      const unreadChatsCount = unreadChatsSnap.size;

      console.log('unreadChatsCount for', uid, '=>', unreadChatsCount);

      if (!tokens.length) continue;

      // 5.3. Готовим payload:
      const data = {
        type: 'chat_new',
        chatId: String(chatId),
        senderId: String(senderId),
        title: String(msg.senderName || chatTitle),
        text: String(textPreview),
        unreadCount: String(unreadChatsCount), // ТОЧНОЕ число непрочитанных ЧАТОВ
      };

      // 5.4. Шлём батчами по 500 (HTTP v1) с notification для бейджа
      for (let i = 0; i < tokens.length; i += 500) {
        const chunk = tokens.slice(i, i + 500);

        const res = await admin.messaging().sendEachForMulticast({
          tokens: chunk,
          data,
          android: {
            priority: 'high',
            notification: {
              channelId: 'chat_badge',            // канал на клиенте
              notificationCount: unreadChatsCount, // цифра/точка для лаунчера
              tag: 'chat_badge_summary',
              title: msg.senderName || chatTitle,  // тихий заголовок
              body: textPreview,                   // тихое тело
            }
          }
        });

        console.log('FCM multicast result:', {
          successCount: res.successCount,
          failureCount: res.failureCount,
          batchSize: chunk.length,
        });

        // чистим «битые» токены
        const cleaning = [];
        res.responses.forEach((r, idx) => {
          if (!r.error) return;
          const code = r.error.code;
          if (
            code === 'messaging/invalid-registration-token' ||
            code === 'messaging/registration-token-not-registered'
          ) {
            const bad = chunk[idx];
            cleaning.push(
              db.collection('users').doc(uid)
                .collection('fcmTokens').doc(bad)
                .delete().catch(() => null)
            );
          }
        });
        await Promise.all(cleaning);
        if (cleaning.length) console.log('cleaned invalid tokens:', cleaning.length);
      }
    }

    return null;
  });

/**
 * (Опционально) callable для ручного обнуления серверного счётчика (если нужно)
 */
exports.resetUnread = functions
  .region('us-central1')
  .https.onCall(async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError('unauthenticated', 'Auth required');
    }
    const uid = context.auth.uid;

    // Сбрасывать можно так: просто снимаем флажки по всем чатам пользователя
    const qs = await db.collection('chats')
      .where('participants', 'array-contains', uid)
      .get();

    const batch = db.batch();
    qs.docs.forEach(doc => {
      const path = `unreadFlags.${uid}`;
      batch.update(doc.ref, { [path]: false });
    });
    await batch.commit();

    return { ok: true };
  });
