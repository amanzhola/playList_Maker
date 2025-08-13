package com.example.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.api.song_db.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.search.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class FavoriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor // ❤️ Интерактор
) : ViewModel() {

    // Если нужен «ручной» рефреш (onResume), оставим триггер:
    private val reload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Базовый поток из базы
    private val favoritesFlow: Flow<List<Track>> =
        favoriteTracksInteractor.getAllFavorites()
            .distinctUntilChanged() // чтобы не дергать UI лишний раз

    // Итоговое состояние:
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FavoriteTracksState> =
    // Объединим «источник из БД» и ручной перезапрос.
    // reload просто «пингуем», чтобы БД переэмичила (если провайдер это поддерживает),
        // а если нет — всё равно не навредит.
        reload
            .onStart { emit(Unit) }     // ⚡ первый запуск без init{}
            .flatMapLatest {
                favoritesFlow
                    .map { FavoriteTracksState(isLoading = false, tracks = it) }
                    .onStart { emit(FavoriteTracksState(isLoading = true)) } // 👈 скрываем "пусто" до прихода данных
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = FavoriteTracksState(isLoading = true) // 👈 по умолчанию loading
            )

    fun reloadFavorites() {
        // Если источник — Room/Flow, перезапуск не обязателен, но раз у тебя вызывается в onResume, просто «пингуем».
        reload.tryEmit(Unit)
    }

    // Если потребуется удалить/добавить трек — можно добавить сюда методы, которые дергают интерактор.
}
