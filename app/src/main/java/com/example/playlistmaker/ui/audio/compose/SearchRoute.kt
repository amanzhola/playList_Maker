package com.example.playlistmaker.ui.search.compose

import androidx.compose.runtime.Composable
<<<<<<< Updated upstream
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
=======
>>>>>>> Stashed changes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import com.example.playlistmaker.ui.audio.compose.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
<<<<<<< Updated upstream
fun SearchRoute(
    // НОВОЕ: опциональный внешний фон всего экрана поиска
    screenBackgroundOverride: Color? = null,
    rowTextColorOverride: Color? = null,
    rowIconColorOverride: Color? = null,
    rowBackgroundOverride: Color? = null,
    vm: SearchViewModel = koinViewModel()
) {
    val state: SearchUiState by vm.uiState.collectAsStateWithLifecycle()


    SearchScreen(
        state = state,
=======
fun SearchRoute() {
    val vm: SearchViewModel = koinViewModel()
    val state: SearchUiState = vm.uiState.collectAsStateWithLifecycle().value

    SearchScreen(
        state = state,                 // ← передаём реальный стейт из VM
>>>>>>> Stashed changes
        onQueryChange   = vm::onQueryChanged,
        onClear         = vm::clearSearchInput,
        onRetry         = vm::onSearchActionDone,
        onHistoryClear  = vm::clearHistory,
        onItemClick     = vm::onTrackClicked,
        onRemoveClick   = vm::removeTrack,
<<<<<<< Updated upstream
        onFocusChanged  = vm::setInputFocused,

        // НОВОЕ: прокидываем дальше
        screenBackgroundOverride = screenBackgroundOverride,
        rowTextColorOverride     = rowTextColorOverride,
        rowIconColorOverride     = rowIconColorOverride,
        rowBackgroundOverride    = rowBackgroundOverride
=======
        onFocusChanged  = vm::setInputFocused
>>>>>>> Stashed changes
    )
}
