package com.example.playlistmaker.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.roots.main.MainActivity
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class UsersFragment : BaseFragment(), BottomNavConfig {

    private val usersRepo = UsersRepo()
    private val chatRepo = ChatRepo()
    private lateinit var adapter: UsersAdapter

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var listenJob: Job? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_users, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        v.findViewById<View>(R.id.btnEditProfile)?.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        val rv = v.findViewById<RecyclerView>(R.id.rvUsers)

        // ⬇️ ОБЯЗАТЕЛЬНА
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.setHasFixedSize(true)

        adapter = UsersAdapter { user ->
            val me = Firebase.auth.currentUser?.uid ?: return@UsersAdapter
            viewLifecycleOwner.lifecycleScope.launch {
                val chatId = chatRepo.createOrOpenDm(me, user.uid)
                findNavController().navigate(
                    R.id.chatFragment,
                    Bundle().apply { putString("chatId", chatId) }
                )
            }
        }
        rv.adapter = adapter

        // 1) Пробуем взять uid сразу
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            afterUidReady(uid)
            return
        }

        // 2) Если uid ещё нет — подписываемся и ждём
        val l = FirebaseAuth.AuthStateListener { auth ->
            val u = auth.currentUser?.uid ?: return@AuthStateListener
            // получили uid один раз — снимаем листенер и продолжаем
            authListener?.let { Firebase.auth.removeAuthStateListener(it) }
            authListener = null
            afterUidReady(u)
        }
        authListener = l
        Firebase.auth.addAuthStateListener(l)
    }

    private fun afterUidReady(uid: String) {
        // создадим/обновим users/{uid} (merge — безопасно)
        viewLifecycleOwner.lifecycleScope.launch {
            try { usersRepo.ensureCurrentUser(uid) } catch (_: Exception) {}
        }
        // слушаем список пользователей (без самого себя)
        listenJob?.cancel()
        listenJob = viewLifecycleOwner.lifecycleScope.launch {
            usersRepo.listenUsers { list ->
                adapter.submitList(list.filter { it.uid != uid })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authListener?.let { Firebase.auth.removeAuthStateListener(it) }
        authListener = null
        listenJob?.cancel()
        listenJob = null
    }

    // ---- BottomNavConfig ----
    override fun getBottomNavButtonIndex(): Int = 5
    override fun shouldShowBottomNav(): Boolean = true
    override fun shouldShowFullBottomNav(): Boolean = false

    // ---- Toolbar ----
    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.users_title) {
            (requireActivity() as? MainActivity)?.apply {
                buttonIndex = -1
                switchFragment(buttonIndex)
                bottomNavigationHelper.selectButton(buttonIndex)
                bottomNavigationHelper.setBottomNavigationVisibility()
            }
        }
}
