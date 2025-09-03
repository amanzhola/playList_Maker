package com.example.playlistmaker.ui.createPlaylist

import android.app.Activity
import android.content.Intent
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
import androidx.annotation.StringRes
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
import com.example.playlistmaker.utils.ARG_EDIT_COVER
import com.example.playlistmaker.utils.ARG_EDIT_DESC
import com.example.playlistmaker.utils.ARG_EDIT_ID
import com.example.playlistmaker.utils.ARG_EDIT_NAME
import com.example.playlistmaker.utils.NavKeys
import com.example.playlistmaker.utils.NavKeys.SCROLL_TOP
import com.example.playlistmaker.utils.showWithSquareWhiteStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : BaseFragment(), BottomNavConfig {

    // ★ Признак режима редактирования (bundle-based, без SafeArgs)
    private val isEditMode: Boolean
        get() = arguments?.containsKey(ARG_EDIT_ID) == true

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

        // ★ Если открылись в режиме редактирования — один раз передадим VM исходные данные
        if (isEditMode) {
            val id    = requireArguments().getLong(ARG_EDIT_ID)
            val name  = requireArguments().getString(ARG_EDIT_NAME).orEmpty()
            val desc  = requireArguments().getString(ARG_EDIT_DESC)
            val cover = requireArguments().getString(ARG_EDIT_COVER) // может быть null
            vm.enterEditModeIfNeeded(id, name, desc, cover) // ← см. патч VM ниже
        }

        // ★ Текст кнопки в зависимости от режима
        binding.btnCreate.text = getString(if (isEditMode) R.string.save else R.string.create)

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
                                val nav = findNavController()
                                val fromPlaylist = arguments?.getBoolean("from_playlist") == true
                                val fromPreview  = arguments?.getBoolean("from_preview")  == true   // 👈 вместо cameFromPreview

                                // ★ В режиме редактирования — просто назад на экран плейлиста,
                                // он получает обновления из БД сам
                                if (isEditMode) {
                                    nav.popBackStack()
                                    return@collect
                                }

                                when {
                                    // 1) из FragmentPlaylist → в MediaLibrary
                                    fromPlaylist -> {
                                        val args = bundleOf(
                                            NavKeys.PLAYLIST_CREATED_NAME to e.name,
                                            SCROLL_TOP to true,
                                            NavKeys.SELECT_TAB to 1
                                        )
                                        val opts = navOptions {
                                            popUpTo(R.id.mediaLibraryFragment) {
                                                inclusive = true
                                                saveState = false
                                            }
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                        nav.navigate(R.id.mediaLibraryFragment, args, opts)
                                    }

                                    // 3) НОВОЕ: пришли из TrackPreviewFragment (другая Activity)
                                    fromPreview -> {
                                        // Внутри CreatePlaylistFragment после успешного сохранения:
                                        requireActivity().setResult(
                                            Activity.RESULT_OK,
                                            Intent().putExtra("playlist_created_name", e.name)
                                        )
                                        requireActivity().finish() // закрываем MainActivity и возвращаемся назад
                                    }

                                    // ExtraOption (тот же граф)
                                    else -> {
                                        // твой текущий кейс: savedStateHandle + popBackStack()
                                        findNavController().previousBackStackEntry?.savedStateHandle?.set(NavKeys.PLAYLIST_CREATED_NAME, e.name)
                                        findNavController().popBackStack()
                                    }
                                }
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

    override fun onResume() {
        super.onResume()
        // fixing theme on emulator and real mobile difference
        (activity as? BaseActivity)?.applyToolbarThemeColors()
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
        // ★ Динамический заголовок
        ToolbarConfig(View.VISIBLE, if (isEditMode) R.string.edit_playlist_title else R.string.create_playlist_title) {
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

        val fromPlaylist = arguments?.getBoolean("from_playlist") == true
        val fromPreview  = arguments?.getBoolean("from_preview")  == true

        val doExit = {
            when {
                // ★ В РЕЖИМЕ РЕДАКТИРОВАНИЯ — ВСЕГДА закрываем без диалога/сохранения
                isEditMode   -> safePopBack()
                fromPlaylist -> safePopBack()              // вернёмся по графу
                fromPreview  -> {
                    // Возвращаемся в TrackPreviewActivity БЕЗ результата (отмена)
                    requireActivity().setResult(Activity.RESULT_CANCELED)
                    requireActivity().finish()
                } // вернуться в TrackDetailActivity
                else         -> safePopBack()              // обычный случай (ExtraOption и др.)
            }
        }

        val hasChanges = vm.hasUnsavedChanges()

        if (isEditMode) { // ★ Диалог подтверждения — только в режиме СОЗДАНИЯ + РЕДАКТИРОВАНИЕ
            // 🔔 Режим редактирования: если есть несохранённые изменения — спросим подтверждение
            if (hasChanges) {
                showExitDialog(
                    titleRes = R.string.exit_dialog_edit,             // 👈 другой заголовок
                    onConfirm = doExit
                )
            } else {
                doExit()
            }
        } else {
            // 🔔 Режим создания: как и было — диалог только если есть изменения
            if (hasChanges) {
                showExitDialog(onConfirm = doExit)
            } else {
                doExit()
            }
        }
    }

    private fun safePopBack() {
        if (!isAdded) return // фрагмент уже не присоединён — выходим

        // Пытаемся получить NavController (без краша)
        val nav = runCatching { findNavController() }.getOrNull()

        // Сначала пробуем navigateUp (корректнее для графа),
        // если не получилось — пробуем popBackStack вручную.
        val handled = when {
            nav == null -> false
            nav.navigateUp() -> true
            nav.popBackStack() -> true
            else -> false
        }

        // Если внутри графа «назад» не обработался — закрываем Activity
        if (!handled) {
            requireActivity().finish()
        }
    }

    private fun showExitDialog(
        @StringRes titleRes: Int = R.string.exit_dialog_title,
        @StringRes messageRes: Int = R.string.exit_dialog_message,
        onConfirm: () -> Unit = { safePopBack() },
        onCancel: (() -> Unit)? = null
    ) {
        if (exitDialogShown) return
        exitDialogShown = true

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(titleRes))
            .setMessage(getString(messageRes))
            .setNegativeButton(R.string.cancel) { d, _ ->
                d.dismiss()
                onCancel?.invoke()
            }
            .setPositiveButton(R.string.finish) { d, _ ->
                d.dismiss()
                onConfirm()
            }
            .create()

        dialog.setOnDismissListener { exitDialogShown = false }
        dialog.showWithSquareWhiteStyle(requireContext())
    }
}