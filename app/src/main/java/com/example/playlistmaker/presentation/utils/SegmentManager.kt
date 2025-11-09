package com.example.playlistmaker.presentation.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.playlistmaker.BaseActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.base.ThemeInteraction
import com.example.playlistmaker.ui.audio.SearchFragment
import com.example.playlistmaker.ui.audioPosters.AudioPlayerFragment
import com.example.playlistmaker.ui.media.MediaLibraryFragment
import com.example.playlistmaker.ui.movie.SearchMovie
import com.example.playlistmaker.utils.SCOPE_FAV
import com.example.playlistmaker.utils.SCOPE_PL

class SegmentManager(
    private val context: Context,
    private val colorApplierHelper: ColorApplierHelper,
    private val colorPersistenceHelper: ColorPersistenceHelper,
    private val isMainActivity: Boolean,
    private val isDarkThemeEnabled: () -> Boolean,
    private val shareApp: () -> Unit,
    private val writeToSupport: () -> Unit,
    private val openAgreement: () -> Unit,
    private val shareTrackHistoryFromViewModel: () -> Unit,
    private val shareSingleTrack: () -> Unit,
    private val recreate: () -> Unit,
    private val colorManager: ColorManager,
    private val changeLanguage: () -> Unit,
    private val theme: ThemeInteraction,
) {

    fun onSegmentClicked(segmentIndex: Int, isChangedState: Boolean, currentActivity: AppCompatActivity) {
        val randomColor = ColorHelper.getNextColor(context)
        val act = currentActivity as? BaseActivity ?: return

        val currentFragment = act.getCurrentFragment()
        val isMainFragment = currentFragment?.toScreenType() == ScreenType.MAIN_FRAGMENT

        // кто реально на экране, и есть ли медиа
        val (mediaParent, mediaScope) = resolveMediaContext(act)

        if (isChangedState) {
            when (segmentIndex) {
                4 -> { if (!isMainFragment) changeLanguage(); return }
                5 -> { // очистка
                    if (mediaParent != null && mediaScope != null) {
                        clearCurrentMediaTab(act, mediaParent)
                    } else {
                        colorManager.clearAllColors()
                    }
                    return
                }
            }

            // сохранение: если медиа и меняем 1..3 — пишем в таб-скоуп, иначе в текущий screenKey
            val scopeForSave =
                if (mediaScope != null && segmentIndex in 1..3) mediaScope
                else act.getCurrentScreenKey()

            colorPersistenceHelper.save(scopeForSave, segmentIndex, randomColor)
            colorApplierHelper.apply(segmentIndex, randomColor) // только применяет
            return
        }

        // не изменяемый режим (твои действия)
        when (segmentIndex) {
            0 -> {
                val nowDark = theme.isDarkTheme()
                theme.setDarkTheme(!nowDark)
                currentActivity.delegate?.applyDayNight()
                act.applyToolbarThemeColors()
            }
            1 -> {
                val fragment = act.getCurrentFragment()
                when {
                    fragment is AudioPlayerFragment -> shareSingleTrack()
                    currentActivity is SearchMovie -> {
                        AlertDialog.Builder(currentActivity)
                            .setTitle(currentActivity.getString(R.string.share_movie_question))
                            .setMessage(currentActivity.getString(R.string.instruction_movie))
                            .setPositiveButton(currentActivity.getString(R.string.ok)) { d, _ -> d.dismiss() }
                            .show()
                    }
                    fragment is SearchFragment -> fragment.shareTrackHistoryFromViewModel()
                    else -> shareApp()
                }
            }
            2 -> writeToSupport()
            3 -> openAgreement()
            4 -> if (isMainFragment) colorManager.clearAllColors() else (context as? BaseActivity)?.onSegment4Clicked()
            5 -> changeLanguage()
        }
    }

    fun applySavedColors() {
        colorManager.applySavedColors()
    }

    // ---------- helpers ----------
    private fun resolveMediaContext(act: BaseActivity): Pair<MediaLibraryFragment?, String?> {
        val vf = act.getVisibleScreenFragmentOrNull()
        val mediaParent = vf.resolveMediaParent()   // 👈 вместо when(...) с child-фрагментами
        val scope = when (mediaParent?.currentMediaTab()) {
            0 -> SCOPE_FAV
            1 -> SCOPE_PL
            else -> null
        }
        return mediaParent to scope
    }

    private fun clearCurrentMediaTab(act: BaseActivity, mediaParent: MediaLibraryFragment) {
        val scope = when (mediaParent.currentMediaTab()) {
            0 -> SCOPE_FAV
            1 -> SCOPE_PL
            else -> return
        }

        // чистим сохранённые цвета для активного таба
        act.colorPersistenceHelper.clear(scope, 1..3)

        val defBg  = ContextCompat.getColor(act, R.color.white_textColor)
        val defTxt = ContextCompat.getColor(act, R.color.textColor_white)

        // тулбар — только тема
        act.applyToolbarThemeColors()

        // вернуть дефолт ТОЛЬКО активной вкладке
        mediaParent.setActiveBackground(defBg)
        mediaParent.setActiveTextColor(defTxt)
        mediaParent.setActiveIconColor(defTxt) // на табе Playlists это no-op
    }

    private tailrec fun androidx.fragment.app.Fragment?.resolveMediaParent(): MediaLibraryFragment? =
        when (this) {
            null -> null
            is MediaLibraryFragment -> this
            else -> this.parentFragment.resolveMediaParent()
        }

}