package com.example.playlistmaker.ui.createPlaylist

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.BaseFragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.createPlaylist.CreatePlaylistViewModel
import com.example.playlistmaker.presentation.utils.ToolbarConfig
import com.example.playlistmaker.ui.main.BottomNavConfig
import com.example.playlistmaker.utils.NavKeys
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : BaseFragment(), BottomNavConfig {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val vm: CreatePlaylistViewModel by viewModel()
    private val imageLoader: ImageLoader by inject()

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onCoverPicked(uri) }

    private var exitDialogShown = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BaseActivity)?.enableEdgeToEdge(false)

        // Фон тулбара
        val blueColor = ContextCompat.getColor(requireContext(), R.color.white_textColor)
        getBaseActivity()?.toolbarHelper?.setToolbarBackgroundColor(blueColor)

        // Цвет заголовка
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.textColor_white)
        getBaseActivity()?.toolbarHelper?.setTitleTextColor(whiteColor)


        // первичное восстановление
        vm.state.value.let { s ->
            if (binding.etName.text?.toString() != s.name) binding.etName.setText(s.name)
            if (binding.etDesc.text?.toString() != s.desc) binding.etDesc.setText(s.desc)
            applyCover(s.coverUri)
            val hasName = s.name.isNotBlank()
            binding.btnCreate.isEnabled = hasName
            binding.tilName.applyFilledFlatAppearance(hasName)
            binding.tilDesc.applyFilledFlatAppearance(s.desc.isNotBlank())
        }

        // ввод
        binding.etName.doAfterTextChanged { vm.onNameChanged(it?.toString().orEmpty()) }
        binding.etDesc.doAfterTextChanged { vm.onDescChanged(it?.toString().orEmpty()) }
        binding.etName.filters = arrayOf(InputFilter { src, start, end, _, _, _ ->
            // запретим переносы строк
            val out = StringBuilder()
            for (i in start until end) {
                val ch = src[i]
                if (ch != '\n' && ch != '\r') out.append(ch)
            }
            out.toString()
        })

        // выбор обложки
        binding.ivCover.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Системная кнопка Back и жест назад шаг 5
        // системный Back / жест «назад»
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = handleBack()
            }
        )

        // кнопка «Создать» — реал логику добавим позже
        binding.btnCreate.setOnClickListener {
            vm.save(requireContext())
        }

        // Подписки на VM: один repeatOnLifecycle, внутри — два launch
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1) Состояние экрана
                launch {
                    vm.state.collect { st ->
                        val hasName = st.name.isNotBlank()
                        binding.btnCreate.isEnabled = hasName
                        binding.tilName.applyFilledFlatAppearance(st.name.isNotBlank())
                        binding.tilDesc.applyFilledFlatAppearance(st.desc.isNotBlank())
                        applyCover(st.coverUri)
                    }
                }

                // 2) Одноразовые события (успех/ошибка сохранения)
                launch {
                    vm.events.collect { e ->

                        when (e) {
                            is CreatePlaylistViewModel.Event.Saved -> {

                                val args = bundleOf(
                                    NavKeys.PLAYLIST_CREATED_NAME to e.name,
                                    NavKeys.SCROLL_TOP to true,   // одноразовый скролл вверх
                                    NavKeys.SELECT_TAB to 1       // ← индекс вкладки "Playlist" во ViewPager2 (0 или 1 у тебя)
                                )
                                val opts = navOptions {
                                    popUpTo(R.id.mediaLibraryFragment) {
                                        inclusive = true   // удалить старый MediaLibrary из back stack
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                                findNavController().navigate(R.id.mediaLibraryFragment, args, opts)

                            }
                            is CreatePlaylistViewModel.Event.Error -> {
                                // при желании — локальный Snackbar / Toast
                                // Snackbar.make(binding.root, e.message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // скрыть BottomNav
    override fun getBottomNavButtonIndex(): Int? = null
    override fun shouldShowFullBottomNav(): Boolean = false
    override fun shouldShowBottomNav(): Boolean = false

    override fun getToolbarConfig(): ToolbarConfig =
        ToolbarConfig(View.VISIBLE, R.string.create_playlist_title) {
            handleBack()
        }

    private fun TextInputLayout.applyFilledFlatAppearance(hasContent: Boolean) {
        val filledColor = ContextCompat.getColor(context, R.color.switch_thumb_on_color)
        val baseStroke  = ContextCompat.getColor(context, R.color.hintColor)
        val baseHint    = ContextCompat.getColor(context, R.color.textColor)

        val stroke = if (hasContent) filledColor else baseStroke
        val hint   = if (hasContent) filledColor else baseHint

        // одинаковый цвет и в фокусе, и без него → “без подсветки”
        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
            intArrayOf(android.R.attr.state_enabled),
            intArrayOf(-android.R.attr.state_enabled)
        )
        val colors = intArrayOf(stroke, stroke, baseStroke)
        setBoxStrokeColorStateList(ColorStateList(states, colors))

        // хинт (коллапс-заголовок)
        defaultHintTextColor = ColorStateList.valueOf(hint)
    }

    private fun applyCover(uri: Uri?) {
        imageLoader.load(binding.ivCover, uri, R.drawable.cover_create_playlist)
    }

    // единая точка обработки «назад»
    private fun handleBack() {
        if (!isAdded) return
        if (vm.hasUnsavedChanges()) {
            showExitDialog()
        } else {
//            findNavController().popBackStack()
            safePopBack()
        }
    }

    private fun safePopBack() {
        if (!isAdded) return                           // ← на всякий случай
        runCatching { findNavController().popBackStack() }
    }

    private fun showExitDialog() {
        if (exitDialogShown) return
        exitDialogShown = true

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.exit_dialog_title))
            .setMessage(getString(R.string.exit_dialog_message))
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .setPositiveButton(R.string.finish) { d, _ ->
                d.dismiss()
//                findNavController().popBackStack()
                safePopBack()
            }
            .setOnDismissListener { exitDialogShown = false }
            .show()
    }
}