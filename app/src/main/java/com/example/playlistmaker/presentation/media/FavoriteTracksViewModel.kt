package com.example.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor // ❤️ Интерактор
) : ViewModel() {

    private val _state = MutableLiveData(FavoriteTracksState())
    val state: LiveData<FavoriteTracksState> = _state

    init {
        observeFavorites()
    }

    // Подписка на Flow из интерактора
    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteTracksInteractor.getAllFavorites().collect { tracks ->
                if (tracks.isEmpty()) {
                    _state.postValue(FavoriteTracksState(isEmpty = true))
                } else {
                    _state.postValue(FavoriteTracksState(isEmpty = false, tracks = tracks))
                }
            }
        }
    }

    fun reloadFavorites() {
        viewModelScope.launch {
            favoriteTracksInteractor.getAllFavorites().firstOrNull()?.let { tracks ->
                _state.postValue(
                    if (tracks.isEmpty()) FavoriteTracksState(isEmpty = true)
                    else FavoriteTracksState(isEmpty = false, tracks = tracks)
                )
            }
        }
    }

}
