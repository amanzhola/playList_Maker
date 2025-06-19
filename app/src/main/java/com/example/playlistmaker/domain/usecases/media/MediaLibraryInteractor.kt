package com.example.playlistmaker.domain.usecases.media

import com.example.playlistmaker.domain.models.media.MediaTab

class MediaLibraryInteractor {
    fun getTabs(): List<MediaTab> = MediaTab.allTabs()
}
