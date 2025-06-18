package com.example.playlistmaker.domain.models.media

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.mediaFragments.FragmentFavouriteTracks
import com.example.playlistmaker.ui.mediaFragments.FragmentPlaylist

sealed class MediaTab(@StringRes val titleResId: Int) {
    abstract fun createFragment(): Fragment

    data object Favourites : MediaTab(R.string.favourites) {
        override fun createFragment(): Fragment {
            return FragmentFavouriteTracks.newInstance(Bundle().apply {
                putBoolean("fromTab", true)
            })
        }
    }

    data object Playlists : MediaTab(R.string.playlists) {
        override fun createFragment(): Fragment {
            return FragmentPlaylist.newInstance(Bundle().apply {
                putInt("userId", 42)
            })
        }
    }

    companion object {
        fun allTabs(): List<MediaTab> = listOf(Favourites, Playlists)
    }
}
