package com.example.playlistmaker.ui.chat

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : BaseFragment(), BottomNavConfig {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var pickedUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pickedUri = uri
        view?.findViewById<ImageView>(R.id.ivAvatar)?.let { iv ->
            if (uri != null) Glide.with(iv).load(uri).circleCrop().into(iv)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    @SuppressLint("UseKtx")
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        val iv = v.findViewById<ImageView>(R.id.ivAvatar)
        val etName = v.findViewById<EditText>(R.id.etName)
        val etStatus = v.findViewById<EditText>(R.id.etStatus)
        val btnPick = v.findViewById<Button>(R.id.btnPickPhoto)
        val btnSave = v.findViewById<Button>(R.id.btnSave)

        // 1) Подтянуть текущий профиль
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Не вошли в Firebase", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val snap = db.collection("profiles").document(uid).get().await()
                val name = snap.getString("displayName").orEmpty()
                val status = snap.getString("status").orEmpty()
                val photo = snap.getString("photoUrl")

                etName.setText(name)
                etStatus.setText(status)
                if (!photo.isNullOrBlank()) {
                    Glide.with(iv).load(photo).circleCrop().into(iv)
                }
            } catch (t: Throwable) {
                // молча: профиль может ещё не быть создан
            }
        }

        // 2) Выбор аватара
        btnPick.setOnClickListener { pickImage.launch("image/*") }

        // 3) Сохранение
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val status = etStatus.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = getString(R.string.required_field)
                return@setOnClickListener
            }

            btnSave.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // (опционально) заливаем аватар
                    var photoUrl: String? = null
                    pickedUri?.let { uri ->
                        val ref = storage.reference.child("userPhotos/$uid/avatar.jpg")
                        ref.putFile(uri).await()
                        photoUrl = ref.downloadUrl.await().toString()
                    }

                    val data = mutableMapOf<String, Any>(
                        "displayName" to name,
                        "status" to status,
                        "lastSeen" to FieldValue.serverTimestamp()
                    )
                    if (photoUrl != null) data["photoUrl"] = photoUrl!!
                    // createdAt только при первом создании — но merge не повредит
                    data.putIfAbsent("createdAt", FieldValue.serverTimestamp())

                    db.collection("profiles").document(uid)
                        .set(data, SetOptions.merge()).await()

                    requireActivity()
                        .getSharedPreferences("chat_prefs", android.content.Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("profile_asked_once", true)
                        .apply()

                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } catch (t: Throwable) {
                    Toast.makeText(requireContext(), "Ошибка: ${t.message}", Toast.LENGTH_LONG).show()
                } finally {
                    btnSave.isEnabled = true
                }
            }
        }
    }

    // Низ прячем
    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowBottomNav() = false
    override fun shouldShowFullBottomNav() = false

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.profile_title) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
}
