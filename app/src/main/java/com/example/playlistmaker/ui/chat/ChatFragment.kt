package com.example.playlistmaker.ui.chat

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ChatFragment : BaseFragment(), BottomNavConfig {

    private val repo = ChatRepo()
    private lateinit var adapter: MessageAdapter

    private val chatId by lazy { requireArguments().getString("chatId") ?: "demo" }
    private val myId get() = Firebase.auth.currentUser?.uid ?: "me"

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) viewLifecycleOwner.lifecycleScope.launch {
                repo.sendImage(chatId, uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        val rv = view.findViewById<RecyclerView>(R.id.rvMessages)
        val et = view.findViewById<EditText>(R.id.etInput)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)
        val btnAttach = view.findViewById<ImageButton>(R.id.btnAttach)
        val inputBar = view.findViewById<View>(R.id.inputBar)

        // список сообщений
        rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rv.setHasFixedSize(true)
        adapter = MessageAdapter(myId)
        rv.adapter = adapter

        // автоскролл при приходе новых сообщений
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
            }
        })

        // ЕДИНСТВЕННЫЙ обработчик инсетсов:
        // поднимаем inputBar над клавиатурой и даём паддинг списку
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            // поднимаем панель ввода над клавиатурой
            inputBar.translationY = -imeBottom.toFloat()

            // нижний паддинг списку = высота клавиатуры + высота панели
            val barHeight = if (inputBar.height > 0) inputBar.height else inputBar.measuredHeight
            rv.updatePadding(bottom = imeBottom + barHeight)

            // если клавиатура видна — докручиваем в конец
            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
                rv.scrollToPosition(adapter.itemCount - 1)
            }
            insets
        }

        // подписка на сообщения
        repo.listenMessages(chatId) { list ->
            adapter.submit(list)
            if (list.isNotEmpty()) rv.scrollToPosition(list.size - 1)
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
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Не отправлено: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
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

    // ---- ToolbarConfig ----
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.chat_title) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.updateSegmentTexts()
        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()

        @Suppress("DEPRECATION")
        requireActivity().window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )
    }

}

//package com.example.playlistmaker.ui.chat
//
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageButton
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.doOnLayout
//import androidx.core.view.updateLayoutParams
//import androidx.core.view.updatePadding
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.playlistmaker.BaseActivity
//import com.example.playlistmaker.BaseFragment
//import com.example.playlistmaker.R
//import com.example.playlistmaker.presentation.utils.ToolbarConfig
//import com.example.playlistmaker.ui.main.BottomNavConfig
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.launch
//
//class ChatFragment : BaseFragment(), BottomNavConfig {
//
//    private val repo = ChatRepo()
//    private lateinit var adapter: MessageAdapter
//
//    private val chatId by lazy { requireArguments().getString("chatId") ?: "demo" }
//    private val myId get() = Firebase.auth.currentUser?.uid ?: "me"
//
//    private val pickImage =
//        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) viewLifecycleOwner.lifecycleScope.launch {
//                repo.sendImage(chatId, uri)
//            }
//        }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View = inflater.inflate(R.layout.fragment_chat, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        (activity as? BaseActivity)?.enableEdgeToEdge(false)
//
//        val rv = view.findViewById<RecyclerView>(R.id.rvMessages)
//        val et = view.findViewById<EditText>(R.id.etInput)
//        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)
//        val btnAttach = view.findViewById<ImageButton>(R.id.btnAttach)
//        val inputBar = view.findViewById<View>(R.id.inputBar)
//
//        // список
//        rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
//        rv.setHasFixedSize(true)
//        adapter = MessageAdapter(myId)
//        rv.adapter = adapter
//
//        // автоскролл при приходе новых сообщений
//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
//            }
//        })
//
//        // ЕДИНСТВЕННЫЙ обработчик инсетсов: поднимаем inputBar и даём паддинг списку
//        view.doOnLayout {
//            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
//                val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//                val sysBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//                val extra = (imeBottom - sysBottom).coerceAtLeast(0)
//
//                // 1) панель ввода НАД клавиатурой
//                inputBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                    bottomMargin = extra
//                }
//                // 2) нижний паддинг списку = высота клавы + высота панели
//                rv.updatePadding(bottom = extra + inputBar.height)
//
//                // 3) если клава видна — докрутить к низу
//                if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
//                    rv.scrollToPosition(adapter.itemCount - 1)
//                }
//                insets
//            }
//        }
//
//        // подписка на сообщения
//        repo.listenMessages(chatId) { list ->
//            adapter.submit(list)
//            if (list.isNotEmpty()) rv.scrollToPosition(list.size - 1)
//        }
//
//        // отправка текста
//        btnSend.setOnClickListener {
//            val t = et.text.toString().trim()
//            if (t.isNotEmpty()) {
//                viewLifecycleOwner.lifecycleScope.launch {
//                    try {
//                        repo.sendText(chatId, t)
//                        et.setText("")
//                    } catch (e: Exception) {
//                        android.widget.Toast.makeText(
//                            requireContext(),
//                            "Не отправлено: ${e.message}",
//                            android.widget.Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            }
//        }
//
//        // вложение
//        btnAttach.setOnClickListener { pickImage.launch("image/*") }
//    }
//
//    // ---- BottomNavConfig ----
//    override fun getBottomNavButtonIndex(): Int? = null
//    override fun shouldShowBottomNav(): Boolean = false
//    override fun shouldShowFullBottomNav(): Boolean = false
//
//    // ---- ToolbarConfig ----
//    override fun getToolbarConfig(): ToolbarConfig =
//        ToolbarConfig(View.VISIBLE, R.string.chat_title) {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//
//    override fun onResume() {
//        super.onResume()
//        (activity as? BaseActivity)?.updateSegmentTexts()
//        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()
//    }
//}

//package com.example.playlistmaker.ui.chat
//
//import android.net.Uri
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageButton
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsAnimationCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.doOnLayout
//import androidx.core.view.updateLayoutParams
//import androidx.core.view.updatePadding
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.playlistmaker.BaseActivity
//import com.example.playlistmaker.BaseFragment
//import com.example.playlistmaker.R
//import com.example.playlistmaker.presentation.utils.ToolbarConfig
//import com.example.playlistmaker.ui.main.BottomNavConfig
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.launch
//import kotlin.math.max
//
//
//class ChatFragment : BaseFragment(), BottomNavConfig {
//
//    private val repo = ChatRepo()
//    private lateinit var adapter: MessageAdapter
//
//    private val chatId by lazy { requireArguments().getString("chatId") ?: "demo" }
//    private val myId get() = Firebase.auth.currentUser?.uid ?: "me"
//
//    private val pickImage =
//        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) viewLifecycleOwner.lifecycleScope.launch {
//                repo.sendImage(
//                    chatId,
//                    uri
//                )
//            }
//        }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View = inflater.inflate(R.layout.fragment_chat, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        (activity as? BaseActivity)?.enableEdgeToEdge(false)
//
//        val rv = view.findViewById<RecyclerView>(R.id.rvMessages)
//        val et = view.findViewById<EditText>(R.id.etInput)
//        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)
//        val btnAttach = view.findViewById<ImageButton>(R.id.btnAttach)
//        val inputBar = view.findViewById<View>(R.id.inputBar)
//
//        // layoutManager
//        rv.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
//        rv.setHasFixedSize(true)
//        adapter = MessageAdapter(myId)
//        rv.adapter = adapter
//
//        // Когда view уже измерена, сможем корректно учесть высоту inputBar
//        view.doOnLayout {
//            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
//                val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//                val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//
//                // Чистая высота клавиатуры сверх системной нижней панели
//                val extra = (ime - sys).coerceAtLeast(0)
//
//                // 1) Поднимаем панель ввода НАД клавиатурой:
//                inputBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                    bottomMargin = extra
//                }
//
//                // 2) Даем списку нижний паддинг: высота панели + клавиатура
//                rv.updatePadding(bottom = extra + inputBar.height)
//
//                // 3) Если клавиатура видна — докрутим к последнему сообщению
//                if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
//                    rv.scrollToPosition(adapter.itemCount - 1)
//                }
//
//                insets
//            }
//        }
//
//        // Автоскролл, когда приходят новые элементы
//        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
//            }
//        })
//
//        // 1) Реакция на вставки (IME + системные бары): вычисляем нижний отступ
//        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
//            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
//            val bottom = max(ime, sys)
//
//            // поднимаем inputBar, даём паддинги списку
//            inputBar.updatePadding(bottom = bottom)
//            rv.updatePadding(bottom = bottom + inputBar.height)
//
//            // если IME видно — докрутим к последнему
//            if (insets.isVisible(WindowInsetsCompat.Type.ime()) && adapter.itemCount > 0) {
//                rv.scrollToPosition(adapter.itemCount - 1)
//            }
//            insets
//        }
//
//        // 2) Анимация вместе с клавиатурой (плавное движение)
//        ViewCompat.setWindowInsetsAnimationCallback(
//            view,
//            object : WindowInsetsAnimationCompat.Callback(
//                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
//            ) {
//                override fun onProgress(
//                    insets: WindowInsetsCompat,
//                    runningAnimations: List<WindowInsetsAnimationCompat>
//                ): WindowInsetsCompat {
//                    val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//                    // можно анимировать translationY у inputBar, но чаще достаточно паддингов выше
//                    // оставим автоскролл:
//                    if (imeBottom > 0 && adapter.itemCount > 0) {
//                        rv.scrollToPosition(adapter.itemCount - 1)
//                    }
//                    return insets
//                }
//            }
//        )
//
//        // Подписка на сообщения
//        repo.listenMessages(chatId) { list ->
//            adapter.submit(list)
//            if (list.isNotEmpty()) rv.scrollToPosition(list.size - 1)
//        }
//
//        btnSend.setOnClickListener {
//            val t = et.text.toString().trim()
//            if (t.isNotEmpty()) {
//                viewLifecycleOwner.lifecycleScope.launch {
//                    try {
//                        repo.sendText(chatId, t)
//                        et.setText("")
//                    } catch (e: Exception) {
//                        android.widget.Toast.makeText(requireContext(), "Не отправлено: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }
//
//        btnAttach.setOnClickListener { pickImage.launch("image/*") }
//    }
//
//    // ---- BottomNavConfig ----
//    override fun getBottomNavButtonIndex(): Int? = null
//    override fun shouldShowBottomNav(): Boolean = false
//    override fun shouldShowFullBottomNav(): Boolean = false
//
//    // ---- ToolbarConfig ----
//    override fun getToolbarConfig(): ToolbarConfig =
//        ToolbarConfig(View.VISIBLE, R.string.chat_title) {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//
//    override fun onResume() {
//        super.onResume()
//        (activity as? BaseActivity)?.updateSegmentTexts()
//        (activity as? BaseActivity)?.applyThemeThenRestoreSaved()
//    }
//}
