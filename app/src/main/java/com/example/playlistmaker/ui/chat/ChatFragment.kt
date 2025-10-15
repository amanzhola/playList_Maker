package com.example.playlistmaker.ui.chat

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
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

    // ---------------- общие поля/стейт ----------------
    private var sendInFlight = false

    // --- Анти-дубль текста ---
    private var lastSentText: String? = null
    private var lastSendAtMs: Long = 0L

    // --- Анти-дубль для картинок ---
    private var imageInFlight = false

    // --- Заголовок тулбара (кэш) ---
    private var currentTitle: String? = null
    private fun updateToolbarTitle(newTitle: String?) {
        val t = newTitle?.trim().orEmpty()
        if (t.isEmpty() || t.equals("Chat", ignoreCase = true)) return

        val finalTitle = "💬 $t"
        if (finalTitle == currentTitle) return  // не дёргать тулбар лишний раз

        currentTitle = finalTitle
        (activity as? BaseActivity)?.toolbarHelper?.setTitle(finalTitle)
    }

    // --- Args ---
    private val chatId   by lazy { requireArguments().getString("chatId") ?: error("chatId required") }
    private val chatTitle by lazy { requireArguments().getString("chatTitle") }   // may be null
    private val peerUid   by lazy { requireArguments().getString("peerUid") }     // may be null

    // --- Repos ---
    private val repo      = ChatRepo()
    private val usersRepo = UsersRepo()

    // --- Regs ---
    private var messagesReg: ListenerRegistration? = null
    private var profileReg:  ListenerRegistration? = null
    private var blocksReg:   ListenerRegistration? = null

    // --- UI ---
    private lateinit var adapter: MessageAdapter

    // image picker c прогрессом и анти-дублем
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            viewLifecycleOwner.lifecycleScope.launch {
                if (imageInFlight) return@launch
                imageInFlight = true

                val v = view ?: run { imageInFlight = false; return@launch }
                val pb = v.findViewById<ProgressBar>(R.id.pbImage)
                val et = v.findViewById<EditText>(R.id.etInput)
                val btnSend = v.findViewById<ImageButton>(R.id.btnSend)
                val btnAttach = v.findViewById<ImageButton>(R.id.btnAttach)

                pb.progress = 0
                pb.visibility = View.VISIBLE
                btnAttach.isEnabled = false
                btnSend.isEnabled = false

                try {
                    repo.sendImage(chatId, uri) { percent ->
                        pb.progress = percent.coerceIn(0, 100)
                    }
                } catch (t: Throwable) {
                    if (isAdded && view != null) {
                        showLongSnack("Картинка не отправлена: ${t.message}")
                    }
                } finally {
                    imageInFlight = false
                    pb.visibility = View.GONE
                    btnAttach.isEnabled = true
                    btnSend.isEnabled = et.text?.isNotBlank() == true && !sendInFlight
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View =
        inflater.inflate(R.layout.fragment_chat, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        Log.d("CHAT_ARGS", "onViewCreated chatId=$chatId, chatTitle='$chatTitle', peerUid=$peerUid")
        updateToolbarTitle(chatTitle)

        if (currentTitle == null && !peerUid.isNullOrBlank()) {
            profileReg = usersRepo.listenProfile(peerUid!!) { ui ->
                updateToolbarTitle(ui?.name)
            }
        }

        val rv        = v.findViewById<RecyclerView>(R.id.rvMessages)
        val et        = v.findViewById<EditText>(R.id.etInput)
        val btnSend   = v.findViewById<ImageButton>(R.id.btnSend)
        val btnAttach = v.findViewById<ImageButton>(R.id.btnAttach)
        val bottomBox = v.findViewById<View>(R.id.bottomBox) // контейнер: прогресс + inputBar

        // ---- состояние кнопки отправки ----
        fun updateSendEnabled() {
            btnSend.isEnabled = !sendInFlight && et.text?.isNotBlank() == true
        }
        updateSendEnabled()

        // ---- крестик внутри EditText (справа), появляется через 1с «тишины» ----
        val clearDrawable = androidx.core.content.ContextCompat
            .getDrawable(requireContext(), android.R.drawable.ic_menu_close_clear_cancel)!!
            .mutate()
        androidx.core.graphics.drawable.DrawableCompat.setTint(
            clearDrawable,
            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.backgroundDay1)
        )
        val size = resources.getDimensionPixelSize(R.dimen.searchEraserPadding_16)
        clearDrawable.setBounds(0, 0, size, size)

        fun setClearVisible(visible: Boolean) {
            val end = if (visible) clearDrawable else null
            et.setCompoundDrawablesRelative(null, null, end, null)
        }
        setClearVisible(false)

        val delayMs = 1000L
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        val showAfterIdle = Runnable {
            setClearVisible(et.text?.isNotEmpty() == true)
        }

        // единый TextWatcher (и для Send, и для крестика)
        et.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // во время набора — прячем крестик и сбрасываем таймер
                setClearVisible(false)
                mainHandler.removeCallbacks(showAfterIdle)
            }
            override fun afterTextChanged(s: android.text.Editable?) {
                // включаем/выключаем Send
                updateSendEnabled()
                // планируем появление крестика
                if (s.isNullOrEmpty()) {
                    setClearVisible(false)
                } else {
                    mainHandler.postDelayed(showAfterIdle, delayMs)
                }
            }
        })

        // тап по иконке «крестик» (правая зона ввода)
        et.setOnTouchListener { _, ev ->
            if (ev.action == android.view.MotionEvent.ACTION_UP) {
                val dr = et.compoundDrawablesRelative[2] // end
                if (dr != null) {
                    val hit = ev.x >= et.width - et.paddingEnd - dr.bounds.width() &&
                            ev.x <= et.width - et.paddingEnd
                    if (hit) { et.setText(""); return@setOnTouchListener true }
                }
            }
            false
        }

        // ---- отправка по IME-кнопке «Send» ----
        et.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                btnSend.performClick()
                true
            } else false
        }

        // ---- список сообщений ----
        rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rv.setHasFixedSize(true)
        adapter = MessageAdapter(Firebase.auth.currentUser?.uid.orEmpty())
        rv.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
            }
        })

        // ---- IME-инсетсы: поднимаем нижнюю коробку (прогресс + поле) ----
        ViewCompat.setOnApplyWindowInsetsListener(v) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            bottomBox.translationY = -ime.toFloat()
            val barH = if (bottomBox.height > 0) bottomBox.height else bottomBox.measuredHeight
            rv.updatePadding(bottom = ime + barH)
            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
                rv.scrollToPosition(adapter.itemCount - 1)
            }
            insets
        }

        // ---- блокировки, если известен peer ----
        val me = Firebase.auth.currentUser?.uid ?: return
        peerUid?.let { peer ->
            viewLifecycleOwner.lifecycleScope.launch {
                try { repo.ensureChatHeader(chatId, listOf(me, peer)) } catch (_: Throwable) {}
            }
            blocksReg = BlocksRepo().listenBlockedSet(me) { blocked ->
                val locked = peer in blocked
                et.isEnabled = !locked
                btnSend.isEnabled   = !locked && et.text?.isNotBlank() == true && !sendInFlight
                btnAttach.isEnabled = !locked && !imageInFlight
                if (locked) et.hint = "Пользователь заблокирован"
            }
        }

        // ---- подписка на сообщения ----
        messagesReg = repo.listenMessages(chatId) { list ->
            adapter.submit(list)
            if (list.isNotEmpty()) rv.scrollToPosition(list.size - 1)
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("ChatFragment", "messages arrived -> markRead chat=$chatId size=${list.size}")
                try { repo.markChatRead(chatId, me) } catch (_: Throwable) {}
            }
        }

        // ---- отправка текста (анти-дубль + оптимистичное очищение) ----
        btnSend.setOnClickListener {
            val t = et.text?.toString()?.trim().orEmpty()
            if (t.isEmpty()) return@setOnClickListener
            if (sendInFlight) return@setOnClickListener

            val now = android.os.SystemClock.elapsedRealtime()
            if (t == lastSentText && (now - lastSendAtMs) < 1_500L) return@setOnClickListener

            sendInFlight = true
            btnSend.isEnabled = false

            val backup = et.text?.toString().orEmpty()
            et.setText("")

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    repo.sendText(chatId, t)
                    lastSentText = t
                    lastSendAtMs = android.os.SystemClock.elapsedRealtime()
                } catch (e: Exception) {
                    et.setText(backup)
                    et.setSelection(et.text?.length ?: 0)
                    if (isAdded && view != null) showLongSnack("Не отправлено: ${e.message}")
                } finally {
                    sendInFlight = false
                    btnSend.isEnabled = et.text?.isNotBlank() == true
                }
            }
        }

        // ---- вложение (анти-дубль загрузки; прогресс — в pickImage) ----
        btnAttach.setOnClickListener {
            if (imageInFlight) return@setOnClickListener
            pickImage.launch("image/*")
        }
    }

    // ---- BottomNavConfig ----
    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowBottomNav(): Boolean = false
    override fun shouldShowFullBottomNav(): Boolean = false

    // ---- Toolbar ----
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(
            backArrowVisibility = View.VISIBLE,
            titleResId = 0,
        ) { requireActivity().onBackPressedDispatcher.onBackPressed() }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        currentTitle?.let { (activity as? BaseActivity)?.toolbarHelper?.setTitle(it) }

        @Suppress("DEPRECATION")
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        viewLifecycleOwner.lifecycleScope.launch {
            markChatRead(chatId)
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
        val uid = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore
        try {
            db.collection("chats").document(chatId)
                .update(mapOf("unreadFlags.$uid" to false))
                .await()
        } catch (_: Exception) {}
    }
}
