package com.example.playlistmaker.ui.chat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.data.migrations.fixMyChatsParticipants
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.chat.model.ChatItem
import com.example.playlistmaker.ui.chat.model.UserUi
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.showLongSnack
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class   UsersFragment : BaseFragment(), BottomNavConfig {

    private var blocksReg: ListenerRegistration? = null
    private var blockedSet: Set<String> = emptySet()

    private val peerProfiles = mutableMapOf<String, UserUi?>()
    private val peerRegs     = mutableMapOf<String, ListenerRegistration>()

    private var chatsReg: ListenerRegistration? = null

    // --- репозитории ---
    private val chatsRepo = ChatsRepo()
    private val usersRepo = UsersRepo()
    private val chatRepo  = ChatRepo()

    // --- UI ---
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ChatsAdapter
    private lateinit var fab: FloatingActionButton

    private fun renderChats(chats: List<ChatItem>, myUid: String) {
        if (!isAdded || view == null) return

        val enriched = chats.map { c ->
            if (c.title.isNotBlank()) return@map c
            val peerUid = c.participants.firstOrNull { it != myUid }
            val ui = peerUid?.let { peerProfiles[it] }
            c.copy(title = ui?.name ?: "Chat")
        }
        adapter.submitList(enriched)
    }

    /** Разовая миграция: если visibleFor отсутствует/пусто — ставим participants */
    private suspend fun backfillVisibleForMap(myUid: String) {
        val db = com.google.firebase.Firebase.firestore
        val qs = db.collection("chats")
            .whereArrayContains("participants", myUid)
            .get().await()

        val batch = db.batch()
        for (d in qs.documents) {
            val data = d.data ?: continue
            val vis = (data["visibleFor"] as? List<*>)?.filterIsInstance<String>().orEmpty()
            // если карты нет — подложим из списка
            if ((data["visibleForMap"] as? Map<*, *>) == null) {
                val visMap = vis.associateWith { true }
                batch.set(d.reference, mapOf("visibleForMap" to visMap), SetOptions.merge())
            }
        }
        batch.commit().await()
    }

    // вынеси твою текущую подписку в метод — чтобы вызвать её после миграции
    private fun attachChatsListener(myUid: String) {
        chatsReg?.remove()
        chatsReg = chatsRepo.listenMyChats(myUid) { rawList ->
            if (!isAdded || view == null) return@listenMyChats

            // собрать peer uids для DM
            val peerUids = rawList
                .mapNotNull { c -> c.participants.firstOrNull { it != myUid } }
                .toSet()

            // снять лишние слушатели профилей
            (peerRegs.keys - peerUids).forEach { uid ->
                peerRegs.remove(uid)?.remove()
                peerProfiles.remove(uid)
            }

            // добавить недостающие
            (peerUids - peerRegs.keys).forEach { uid ->
                val reg = usersRepo.listenProfile(uid) { ui ->
                    peerProfiles[uid] = ui
                    renderChats(rawList, myUid)
                }
                peerRegs[uid] = reg
            }

            // первый рендер (пока профили подтянутся)
            renderChats(rawList, myUid)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_users, c, false)

    @SuppressLint("UseKtx")
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        val myUid = Firebase.auth.currentUser?.uid ?: return

        // 1) ОДНОРАЗОВО: миграция участников
        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = requireContext().getSharedPreferences("migrations", Context.MODE_PRIVATE)
            val done = prefs.getBoolean("fix_participants_done_v2", false)
            if (!done) {
                try {
                    fixMyChatsParticipants(myUid)
                    prefs.edit().putBoolean("fix_participants_done_v2", true).apply()
                    Log.d("MIGRATION", "fixMyChatsParticipants OK")
                } catch (t: Throwable) {
                    Log.e("MIGRATION", "fixMyChatsParticipants FAIL: ${t.message}", t)
                    // даже если упало — не мешаем UI, но в логах увидишь проблемный кейс
                }
            }
            // 2) ТЕПЕРЬ уже вешай слушатели чатов
            attachChatsListener(myUid)
        }

        // Кнопка профиля
        v.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        // Список моих диалогов
        rv = v.findViewById(R.id.rvUsers)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.setHasFixedSize(true)

        // Адаптер (click + longClick)
        adapter = ChatsAdapter(
            myUid = myUid,
            profileOf = { uid -> peerProfiles[uid] },
            onClick = { chat ->
                val peerUid = chat.participants.firstOrNull { it != myUid }

                // НЕ используем chat.title и НЕ подставляем "Chat".
                // Берём только кэшированное имя профиля, если оно уже есть.
                val cachedName = peerUid?.let { peerProfiles[it]?.name }
                val realTitle = cachedName?.takeIf { it.isBlank().not() && it != "Chat" }

                Log.d("NAV", "A: to ChatFragment chatId=${chat.id}, title='${realTitle ?: "<none>"}', peerUid=$peerUid")

                // В аргументы всегда кладём peerUid.
                // chatTitle добавляем ТОЛЬКО если это реальное имя, а не заглушка.
                val args = bundleOf(
                    "chatId" to chat.id,
                    "peerUid" to peerUid
                ).apply {
                    if (realTitle != null) putString("chatTitle", realTitle)
                }

                findNavController().navigate(R.id.chatFragment, args)
            },
            onLongClick = { chat ->
                val peerUid = chat.participants.firstOrNull { it != myUid }
                val options = mutableListOf<String>()
                val isBlocked = blockedSet.contains(peerUid)

                if (peerUid != null) {
                    options += if (!isBlocked) "Заблокировать" else "Разблокировать"
                }
                options += "Убрать из списка"

                AlertDialog.Builder(requireContext())
                    .setTitle(chat.title.ifBlank { peerProfiles[peerUid]?.name ?: "Chat" })
                    .setItems(options.toTypedArray()) { _, which ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                when (options[which]) {
                                    "Заблокировать"   -> BlocksRepo().block(myUid, peerUid!!)
                                    "Разблокировать"  -> BlocksRepo().unblock(myUid, peerUid!!)
                                    "Убрать из списка"-> ChatRepo().hideFor(chat.id, myUid)
                                }
                            } catch (t: Throwable) {
                                showLongSnack("Ошибка: ${t.message}", anchor = fab)

                            }
                        }
                    }
                    .show()
            }
        )
        rv.adapter = adapter

        // FAB: длинное — ввод UID, короткое — поиск
        fab = v.findViewById(R.id.fabNewChat)
        fab.setOnLongClickListener { showNewChatDialog(); true }
        fab.setOnClickListener { showNewChatDialogWithSearch() }

        // Гарантия, что профиль создан
        CoroutineScope(Dispatchers.Main).launch {
            try { usersRepo.ensureCurrentUser(myUid) } catch (_: Throwable) {}
        }

        // ======= ВАЖНО: одна миграция + одна подписка =======
        viewLifecycleOwner.lifecycleScope.launch {
            try { usersRepo.ensureCurrentUser(myUid) } catch (_: Throwable) {}

            // 1) миграция visibleFor
            try { backfillVisibleForMap(myUid) } catch (_: Throwable) {}
        }
        // ======= НЕ ставить вторую подписку ниже! =======
    }

    @SuppressLint("UseKtx")
    override fun onResume() {
        super.onResume()
        val ctx = requireContext()

        // Локально 0
        ctx.getSharedPreferences("badge_prefs", Context.MODE_PRIVATE)
            .edit().putInt("unread_count", 0).apply()

        // OEM-цифры → 0
        try { me.leolin.shortcutbadger.ShortcutBadger.applyCount(ctx, 0) } catch (_: Throwable) {}

        // Снять системные уведомления, чтобы пропала точка
        androidx.core.app.NotificationManagerCompat.from(ctx).cancelAll()

        // Снять флажки непрочитанности на сервере (чтобы «кол-во чатов» стало 0)
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val qs = db.collection("chats")
                    .whereArrayContains("participants", uid)
                    .get().await()
                val batch = db.batch()
                for (doc in qs.documents) {
                    val uf = doc.get("unreadFlags") as? Map<*,*> ?: emptyMap<String, Boolean>()
                    if (uf[uid] == true) {
                        batch.update(doc.reference, mapOf("unreadFlags.$uid" to false))
                    }
                }
                batch.commit().await()
            } catch (_: Exception) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatsReg?.remove(); chatsReg = null

        peerRegs.values.forEach { it.remove() }
        peerRegs.clear()
        peerProfiles.clear()

        blocksReg?.remove(); blocksReg = null
    }

    private fun showNewChatDialog() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.enter_uid_hint)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_chat)
            .setMessage(R.string.new_chat_by_uid_msg)
            .setView(input)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.create) { dlg, _ ->
                val other = input.text?.toString()?.trim().orEmpty()
                val me = Firebase.auth.currentUser?.uid

                if (other.isEmpty() || me.isNullOrEmpty() || other == me) {
                    showLongSnack("Некорректный UID", anchor = fab); return@setPositiveButton
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val exists = try { usersRepo.exists(other) } catch (_: Throwable) { false }
                    if (!exists) { showLongSnack("Пользователь не найден", anchor = fab)
                        return@launch }

                    try {
                        val chatId = chatRepo.createOrOpenDm(me, other)
                        try { chatRepo.ensureChatHeader(chatId, listOf(me, other)) } catch (_: Throwable) {}
                        (dlg as? AlertDialog)?.dismiss()

                        Log.d("NAV", "B: to ChatFragment chatId=$chatId, title='<none>', peerUid=$other")

                        findNavController().navigate(
                            R.id.chatFragment,
                            bundleOf("chatId" to chatId, "peerUid" to other)
                        )
                    } catch (t: Throwable) {
                        showLongSnack("Ошибка: ${t.message}", anchor = fab)
                    }
                }
            }
            .show()
    }

    private fun showNewChatDialogWithSearch() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_chat_search, null)
        val et = dialogView.findViewById<EditText>(R.id.etQuery)
        val rv = dialogView.findViewById<RecyclerView>(R.id.rvSearch)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val me = Firebase.auth.currentUser?.uid ?: return

        var dlg: androidx.appcompat.app.AlertDialog? = null

        val searchAdapter = UsersAdapter { u ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val chatId = chatRepo.createOrOpenDm(me, u.uid)
                    try { chatRepo.ensureChatHeader(chatId, listOf(me, u.uid)) } catch (_: Throwable) {}
                    dlg?.dismiss()
                    val title = u.name ?: "Chat"
                    findNavController().navigate(
                        R.id.chatFragment,
                        bundleOf("chatId" to chatId, "peerUid" to u.uid, "chatTitle" to title)
                    )
                } catch (t: Throwable) {
                    showLongSnack("Ошибка: ${t.message}", anchor = fab)
                }
            }
        }
        rv.adapter = searchAdapter

        dlg = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_chat)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        et.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(q: CharSequence?, start: Int, before: Int, count: Int) {
                val query = q?.toString()?.trim().orEmpty()
                if (query.length < 3) { searchAdapter.submitList(emptyList()); return }
                viewLifecycleOwner.lifecycleScope.launch {
                    val users = usersRepo.searchByNameOrId(query, excludeUid = me, limit = 20)
                    searchAdapter.submitList(users)
                }
            }
        })

        dlg.show()
    }

    // ---- BottomNavConfig ----
    override fun getBottomNavButtonIndex(): Int = 5
    override fun shouldShowBottomNav(): Boolean = true
    override fun shouldShowFullBottomNav(): Boolean = true

    // ---- Toolbar ----
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(GONE, R.string.chats_title) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }
}
