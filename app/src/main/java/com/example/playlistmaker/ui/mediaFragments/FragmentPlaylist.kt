package com.example.playlistmaker.ui.mediaFragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.presentation.ImageLoader
import com.example.playlistmaker.presentation.SpacesItemDecoration
import com.example.playlistmaker.presentation.media.PlaylistViewModel
import com.example.playlistmaker.presentation.utils.screenKeyOrDefault
import com.example.playlistmaker.utils.NavKeys
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentPlaylist : Fragment() {

    private var lastTopId: Long? = null  //запоминаем, кто был на вершине раньше
    private lateinit var gridLm: GridLayoutManager

    private var scrollTopOnce = false
    private var pendingSnackbarName: String? = null

    private lateinit var binding: FragmentPlaylistBinding
    private val viewModel: PlaylistViewModel by viewModel()
    private val imageLoader: ImageLoader by inject()

    // Адаптер создаём один раз; не восстанавливаем состояние,
    // пока список пустой (иначе RecyclerView может «залипнуть» на старой позиции).
    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        PlaylistGridAdapter(imageLoader) { playlist ->
            /* onClick: no-op (или добавь нужный переход) sprint 22*/

            // playlist.id должен быть Long
            val b = bundleOf("playlistId" to playlist.id)
            findNavController().navigate(R.id.action_global_to_playlistInfoFragment, b)

        }.apply {
            stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ловим запрос на скролл вверх и имя созданного плейлиста
        parentFragmentManager.setFragmentResultListener("playlist_scroll_top", this) { _, b ->
            scrollTopOnce = b.getBoolean("scrollTop", false)
            pendingSnackbarName = b.getString("name")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // fail подключён через <include>
        val failTextView: TextView = view.findViewById(R.id.fail)
        val topPadding = resources.getDimensionPixelSize(R.dimen.track_45)
        failTextView.setPadding(0, topPadding, 0, 0)
        failTextView.text = getString(R.string.noPlayList)
        failTextView.isEnabled = true
        failTextView.visibility = View.VISIBLE

        // Кнопка «Новый плейлист»
        binding.btnUpdate.text = getString(R.string.newPlaylist)
        binding.btnUpdate.visibility = View.VISIBLE
        binding.btnUpdate.setOnClickListener {
            findNavController().navigate(
                R.id.action_global_to_createPlaylistFragment,
                bundleOf("from_playlist" to true)
            )
        }
        // сразу сделать кнопку контрастной к текущей теме
        paintBtnContrasted()

        // RecyclerView: span из ресурсов (2 — телефоны, 3 — sw600dp)
        val span = resources.getInteger(R.integer.playlist_grid_span_count)
        gridLm = GridLayoutManager(requireContext(), span)
        binding.rvPlaylists.layoutManager = gridLm
        binding.rvPlaylists.adapter = adapter

        val space = resources.getDimensionPixelSize(R.dimen.line_margin)
        binding.rvPlaylists.addItemDecoration(SpacesItemDecoration(space))

        // Snackbar после создания (через savedStateHandle)
        // Snackbar после создания плейлиста (без скролла)
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
            ?.observe(viewLifecycleOwner) { name ->
                showCreationSnackbar(name)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
            }

        // Подписка на список плейлистов
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.playlists.collect { list ->
//                    // Даём новый snapshot, чтобы DiffUtil точно увидел изменения
//                    adapter.submitList(list.toList()) {
//
//                        if (scrollTopOnce) {
//                            binding.rvPlaylists.stopScroll()
//                            lm.scrollToPositionWithOffset(0, 0)
//                            binding.rvPlaylists.post { lm.scrollToPositionWithOffset(0, 0) }
//                            scrollTopOnce = false
//
//                            pendingSnackbarName?.let { showCreationSnackbar(it) }
//                            pendingSnackbarName = null
//                        }
//                    }
//
//                    val isEmpty = list.isEmpty()
//                    binding.rvPlaylists.isVisible = !isEmpty
//                    failTextView.isVisible = isEmpty
//                    binding.btnUpdate.isVisible = true
//                }
//            }
//        }

        // fixing SCROLL ON TOP for ImportPreviewFragment -> CreatePlaylistFragment -> ImportPreviewFragment
        // previously done for FragmentPlaylist -> CreatePlaylistFragment -> FragmentPlaylist
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collect { list ->
                    // кто сейчас наверху
                    val currentTopId = list.firstOrNull()?.id   // id: Long?

                    // нужно ли форсировать скролл вверх:
                    //  - пришёл твой одноразовый флаг ИЛИ
                    //  - реально сменился верхний элемент
                    val needForceTop =
                        scrollTopOnce || (currentTopId != null && currentTopId != lastTopId)

                    // даём новый snapshot
                    adapter.submitList(list.toList()) {
                        val isEmpty = list.isEmpty()
                        binding.rvPlaylists.isVisible = !isEmpty
                        failTextView.isVisible = isEmpty
                        binding.btnUpdate.isVisible = true

                        if (needForceTop && !isEmpty) {
                            binding.rvPlaylists.stopScroll()
                            gridLm.scrollToPositionWithOffset(0, 0)
                            // на следующий кадр — чтобы перебить restore/Insets
                            binding.rvPlaylists.post { gridLm.scrollToPositionWithOffset(0, 0) }
                        }

                        // показать снэкбар только если это был твой одноразовый «скролл наверх»
                        if (scrollTopOnce) {
                            pendingSnackbarName?.let { showCreationSnackbar(it) }
                            pendingSnackbarName = null
                        }

                        // погасим флаг и запомним новый top
                        scrollTopOnce = false
                        lastTopId = currentTopId
                    }
                }
            }
        }
    }

    private fun showCreationSnackbar(name: String, durationMs: Int = 4000) {
        val root = requireActivity().findViewById<View>(android.R.id.content)
        val msg = getString(R.string.playlist_created, name)
        val sb = com.google.android.material.snackbar.Snackbar
            .make(root, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
        sb.duration = durationMs
        sb.show()
    }

    companion object {
        fun newInstance(args: Bundle?): FragmentPlaylist {
            val fragment = FragmentPlaylist()
            fragment.arguments = args
            return fragment
        }
    }

    // toolbar save and apply background color
    override fun onResume() {
        super.onResume()

        // активна ли эта вкладка?
        val isActive =
            (parentFragment as? com.example.playlistmaker.presentation.utils.ActiveChildProvider)
                ?.getActiveChildFragment() === this
        if (!isActive) return

        val act = activity as? BaseActivity ?: return
        val defaultBg = ContextCompat.getColor(requireContext(), R.color.white_textColor)

        val scope = screenKeyOrDefault()   // ← без any is-проверок
        act.applySavedForScopeOrDefault(scope, defaultBg)

        // после перекрасок родителя — красим кнопку последней
        requireActivity().window.decorView.post { paintBtnContrasted() }
    }

    // если активити не пересоздаётся на смену темы — вернуть цвета после конфиг-чейнджа
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        requireActivity().window.decorView.post { paintBtnContrasted() }
    }

    // === контрастная покраска кнопки (без attr/селекторов) ===
    @SuppressLint("ObsoleteSdkInt")
    private fun paintBtnContrasted() {
        val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        // фон контрастом к теме
        val bgColorRes = if (night) R.color.white else R.color.textColor
        val fgColorRes = if (night) R.color.textColor else R.color.white

        val bg = ContextCompat.getColor(requireContext(), bgColorRes)
        val fg = ContextCompat.getColor(requireContext(), fgColorRes)

        // текст/иконка
        binding.btnUpdate.setTextColor(fg)
        (binding.btnUpdate as? MaterialButton)?.iconTint = ColorStateList.valueOf(fg)

        // собственный фон с радиусом
        val radius = resources.getDimension(R.dimen.track_45)

        val content = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(bg)
        }

        // маска для ripple с тем же радиусом (чтобы не «квадратило» при нажатии)
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(0xFFFFFFFF.toInt()) // цвет не важен — это форма
        }

        val rippleColor = ColorStateList.valueOf(fg)
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RippleDrawable(rippleColor, content, mask).also { it.mutate() }
        } else content

        binding.btnUpdate.background = background

        // ⬇️ КЛЮЧЕВОЕ: свой OutlineProvider, чтобы форма ВСЕГДА была скруглённой
        binding.btnUpdate.clipToOutline = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.btnUpdate.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: android.graphics.Outline) {
                    // размеры могут быть 0 во время вызова — пересчёт ниже в doOnLayout
                    val w = view.width
                    val h = view.height
                    if (w > 0 && h > 0) {
                        outline.setRoundRect(0, 0, w, h, radius)
                    } else {
                        // запасной прямоугольник (почти не случится)
                        outline.setRoundRect(0, 0, 1, 1, radius)
                    }
                }
            }
            // пересчитать контур уже после установки бекграунда
            binding.btnUpdate.invalidateOutline()
        }

        // гарантируем, что при любом изменении размеров (в т.ч. после смены темы)
        // контур пересчитается и останется круглым
        binding.btnUpdate.removeOnLayoutChangeListener(layoutFixListener)
        binding.btnUpdate.addOnLayoutChangeListener(layoutFixListener)
    }

    // один раз объяви слушатель в классе (полем):
    @SuppressLint("ObsoleteSdkInt")
    private val layoutFixListener = View.OnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.invalidateOutline()
        }
    }

    override fun onDestroyView() {
        binding.btnUpdate.removeOnLayoutChangeListener(layoutFixListener)
        super.onDestroyView()
    }

}

//package com.example.playlistmaker.ui.mediaFragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.core.os.bundleOf
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.playlistmaker.BaseActivity
//import com.example.playlistmaker.R
//import com.example.playlistmaker.databinding.FragmentPlaylistBinding
//import com.example.playlistmaker.presentation.ImageLoader
//import com.example.playlistmaker.presentation.SpacesItemDecoration
//import com.example.playlistmaker.presentation.media.PlaylistViewModel
//import com.example.playlistmaker.presentation.utils.screenKeyOrDefault
//import com.example.playlistmaker.utils.NavKeys
//import kotlinx.coroutines.launch
//import org.koin.android.ext.android.inject
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
//class FragmentPlaylist : Fragment() {
//
//    private var scrollTopOnce = false
//    private var pendingSnackbarName: String? = null
//
//    private lateinit var binding: FragmentPlaylistBinding
//    private val viewModel: PlaylistViewModel by viewModel()
//    private val imageLoader: ImageLoader by inject()
//
//    // Адаптер создаём один раз; не восстанавливаем состояние,
//    // пока список пустой (иначе RecyclerView может «залипнуть» на старой позиции).
//    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
//        PlaylistGridAdapter(imageLoader) {playlist ->
//            /* onClick: no-op (или добавь нужный переход) sprint 22*/
//
//            // playlist.id должен быть Long
//            val b = bundleOf("playlistId" to playlist.id)
//            findNavController().navigate(R.id.action_global_to_playlistInfoFragment, b)
//
//        }.apply {
//            stateRestorationPolicy =
//                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // ловим запрос на скролл вверх и имя созданного плейлиста
//        parentFragmentManager.setFragmentResultListener("playlist_scroll_top", this) { _, b ->
//            scrollTopOnce = b.getBoolean("scrollTop", false)
//            pendingSnackbarName = b.getString("name")
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentPlaylistBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // fail подключён через <include>
//        val failTextView: TextView = view.findViewById(R.id.fail)
//        val topPadding = resources.getDimensionPixelSize(R.dimen.track_45)
//        failTextView.setPadding(0, topPadding, 0, 0)
//        failTextView.text = getString(R.string.noPlayList)
//        failTextView.isEnabled = true
//        failTextView.visibility = View.VISIBLE
//
//        // Кнопка «Новый плейлист»
//        binding.btnUpdate.text = getString(R.string.newPlaylist)
//        binding.btnUpdate.visibility = View.VISIBLE
//        binding.btnUpdate.setOnClickListener {
//
//            findNavController().navigate(R.id.action_global_to_createPlaylistFragment,
//                bundleOf("from_playlist" to true))
//        }
//
//        // RecyclerView: span из ресурсов (2 — телефоны, 3 — sw600dp)
//        val span = resources.getInteger(R.integer.playlist_grid_span_count)
//        val lm = GridLayoutManager(requireContext(), span)
//        binding.rvPlaylists.layoutManager = lm
//        binding.rvPlaylists.adapter = adapter
//
//        val space = resources.getDimensionPixelSize(R.dimen.line_margin)
//        binding.rvPlaylists.addItemDecoration(SpacesItemDecoration(space))
//
//        // Snackbar после создания (через savedStateHandle)
//        // Snackbar после создания плейлиста (без скролла)
//        findNavController().currentBackStackEntry
//            ?.savedStateHandle
//            ?.getLiveData<String>(NavKeys.PLAYLIST_CREATED_NAME)
//            ?.observe(viewLifecycleOwner) { name ->
//                showCreationSnackbar(name)
//                findNavController().currentBackStackEntry
//                    ?.savedStateHandle
//                    ?.remove<String>(NavKeys.PLAYLIST_CREATED_NAME)
//            }
//
//        // Подписка на список плейлистов
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.playlists.collect { list ->
//                    // Даём новый snapshot, чтобы DiffUtil точно увидел изменения
//                    adapter.submitList(list.toList()) {
//
//                        if (scrollTopOnce) {
//                            binding.rvPlaylists.stopScroll()
//                            lm.scrollToPositionWithOffset(0, 0)
//                            binding.rvPlaylists.post { lm.scrollToPositionWithOffset(0, 0) }
//                            scrollTopOnce = false
//
//                            pendingSnackbarName?.let { showCreationSnackbar(it) }
//                            pendingSnackbarName = null
//                        }
//                    }
//
//                    val isEmpty = list.isEmpty()
//                    binding.rvPlaylists.isVisible = !isEmpty
//                    failTextView.isVisible = isEmpty
//                    binding.btnUpdate.isVisible = true
//                }
//            }
//        }
//    }
//
//    private fun showCreationSnackbar(name: String, durationMs: Int = 4000) {
//        val root = requireActivity().findViewById<View>(android.R.id.content)
//        val msg = getString(R.string.playlist_created, name)
//        val sb = com.google.android.material.snackbar.Snackbar
//            .make(root, msg, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
//        (sb.view.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(0, 0, 0, 0)
//        sb.duration = durationMs
//        sb.show()
//    }
//
//    companion object {
//        fun newInstance(args: Bundle?): FragmentPlaylist {
//            val fragment = FragmentPlaylist()
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    // toolbar save and apply background color
//    override fun onResume() {
//        super.onResume()
//
//        // активна ли эта вкладка?
//        val isActive = (parentFragment as? com.example.playlistmaker.presentation.utils.ActiveChildProvider)
//            ?.getActiveChildFragment() === this
//        if (!isActive) return
//
//        val act = activity as? BaseActivity ?: return
//        val defaultBg = ContextCompat.getColor(requireContext(), R.color.white_textColor)
//
//        val scope = screenKeyOrDefault()   // ← без any is-проверок
//        act.applySavedForScopeOrDefault(scope, defaultBg)
//    }
//}