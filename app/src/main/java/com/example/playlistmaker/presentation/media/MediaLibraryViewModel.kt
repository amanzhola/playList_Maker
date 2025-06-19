package com.example.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.media.MediaTab
import com.example.playlistmaker.domain.usecases.media.MediaLibraryInteractor

class MediaLibraryViewModel(
    private val interactor: MediaLibraryInteractor
) : ViewModel() {

    fun getTabs(): List<MediaTab> = interactor.getTabs()
}
