package com.example.playlistmaker.ui.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.showLongSnack
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatFragment : BaseFragment(), BottomNavConfig {

    private var titleSet = false
    private fun setChatTitleOnce(title: String?) {
        if (titleSet) return
        val t = title?.takeIf { it.isNotBlank() && it != "Chat" } ?: return
        titleSet = true
        (activity as? BaseActivity)?.toolbarHelper?.setTitle("💬 $t")
    }

    // -- args
    private val chatId   by lazy { requireArguments().getString("chatId") ?: error("chatId required") }
    private val chatTitle by lazy { requireArguments().getString("chatTitle") }   // может быть null
    private val peerUid   by lazy { requireArguments().getString("peerUid") }     // может быть null

    // -- repos
    private val repo      = ChatRepo()
    private val usersRepo = UsersRepo()

    // -- regs
    private var messagesReg: ListenerRegistration? = null
    private var profileReg:  ListenerRegistration? = null
    private var blocksReg:   ListenerRegistration? = null

    // -- ui
    private lateinit var adapter: MessageAdapter

    // image picker
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) viewLifecycleOwner.lifecycleScope.launch {
                try {
                    repo.sendImage(chatId, uri)
                } catch (t: Throwable) {
//                    Toast.makeText(requireContext(), "Не отправлено: ${t.message}", Toast.LENGTH_LONG).show()
                    showLongSnack("Не отправлено: ${t.message}")
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        Log.d("CHAT_ARGS", "onViewCreated chatId=$chatId, chatTitle='$chatTitle', peerUid=$peerUid")

        // Сразу ставим заголовок, если пришёл из UsersFragment
        // 1) если пришло настоящее имя — поставим
        setChatTitleOnce(chatTitle)

        // 2) иначе подтянем из профиля по peerUid
        if (!titleSet && !peerUid.isNullOrBlank()) {
            profileReg = usersRepo.listenProfile(peerUid!!) { ui ->
                setChatTitleOnce(ui?.name) // "Chat" отфильтруется
            }
        }

        val rv        = v.findViewById<RecyclerView>(R.id.rvMessages)
        val et        = v.findViewById<EditText>(R.id.etInput)
        val btnSend   = v.findViewById<ImageButton>(R.id.btnSend)
        val btnAttach = v.findViewById<ImageButton>(R.id.btnAttach)
        val inputBar  = v.findViewById<View>(R.id.inputBar)

        // список
        rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rv.setHasFixedSize(true)
        adapter = MessageAdapter(Firebase.auth.currentUser?.uid.orEmpty())
        rv.adapter = adapter

        // автоскролл при добавлении
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
            }
        })

        // инсетсы: приподнимаем inputBar и даём паддинг списку
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            inputBar.translationY = -ime.toFloat()
            val barH = if (inputBar.height > 0) inputBar.height else inputBar.measuredHeight
            rv.updatePadding(bottom = ime + barH)
            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
                rv.scrollToPosition(adapter.itemCount - 1)
            }
            insets
        }

        // если есть peer — гарантируем заголовок и слушаем блокировку
        val me = Firebase.auth.currentUser?.uid ?: return
        peerUid?.let { peer ->
            viewLifecycleOwner.lifecycleScope.launch {
                try { repo.ensureChatHeader(chatId, listOf(me, peer)) } catch (_: Throwable) {}
            }
            blocksReg = BlocksRepo().listenBlockedSet(me) { blocked ->
                val locked = peer in blocked
                et.isEnabled = !locked
                btnSend.isEnabled = !locked
                btnAttach.isEnabled = !locked
                if (locked) et.hint = "Пользователь заблокирован"
            }
        }

        // ЕДИНСТВЕННАЯ подписка на сообщения
        messagesReg = repo.listenMessages(chatId) { list ->
            adapter.submit(list)
            if (list.isNotEmpty()) rv.scrollToPosition(list.size - 1)
            // пометить прочитанным на каждую поставку (safe)
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("ChatFragment", "messages arrived -> markRead chat=$chatId size=${list.size}")
                try { repo.markChatRead(chatId, me) } catch (_: Throwable) {}
            }
        }

        // отправка текста
        btnSend.setOnClickListener {
            val t = et.text.toString().trim()
            if (t.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        repo.sendText(chatId, t)
                        et.setText("")
                    } catch (e: Exception) {
                        showLongSnack("Не отправлено: ${e.message}")
                    }
                }
            }
        }

        // вложение
        btnAttach.setOnClickListener { pickImage.launch("image/*") }
    }

    // ---- BottomNavConfig ----
    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowBottomNav(): Boolean = false
    override fun shouldShowFullBottomNav(): Boolean = false

    // ---- Toolbar ----
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.chat_title) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        chatTitle?.takeIf { it.isNotBlank() }?.let { nonEmpty ->
            (activity as? BaseActivity)?.toolbarHelper?.setTitle("💬 $nonEmpty")
        }

        @Suppress("DEPRECATION")
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        // for counting to icon count
        viewLifecycleOwner.lifecycleScope.launch {
            markChatRead(chatId)
            // если показываешь per-chat уведомления — убери их:
            val nm = requireContext().getSystemService(android.app.NotificationManager::class.java)
            nm?.cancel(chatId.hashCode())
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )
    }

    // доп. защита: помечаем прочитанным на входе экрана
    override fun onStart() {
        super.onStart()
        val myUid = Firebase.auth.currentUser?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("ChatFragment", "onStart -> markRead chat=$chatId")
            try { repo.markChatRead(chatId, myUid) } catch (_: Throwable) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesReg?.remove(); messagesReg = null
        profileReg?.remove();  profileReg  = null
        blocksReg?.remove();   blocksReg   = null
    }

    private suspend fun markChatRead(chatId: String) {
        val uid = com.google.firebase.ktx.Firebase.auth.currentUser?.uid ?: return
        val db = com.google.firebase.ktx.Firebase.firestore
        try {
            db.collection("chats").document(chatId)
                .update(mapOf("unreadFlags.$uid" to false))
                .await()
        } catch (_: Exception) {}
    }
}
