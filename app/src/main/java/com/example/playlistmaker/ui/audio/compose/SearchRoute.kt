package com.example.playlistmaker.ui.search.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.playlistmaker.presentation.searchViewModels.SearchViewModel
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import com.example.playlistmaker.ui.audio.compose.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(
    // НОВОЕ: опциональный внешний фон всего экрана поиска
    screenBackgroundOverride: Color? = null,
    rowTextColorOverride: Color? = null,
    rowIconColorOverride: Color? = null,
    rowBackgroundOverride: Color? = null
) {
    val vm: SearchViewModel = koinViewModel()
    val state: SearchUiState = vm.uiState.collectAsStateWithLifecycle().value

    SearchScreen(
        state = state,
        onQueryChange   = vm::onQueryChanged,
        onClear         = vm::clearSearchInput,
        onRetry         = vm::onSearchActionDone,
        onHistoryClear  = vm::clearHistory,
        onItemClick     = vm::onTrackClicked,
        onRemoveClick   = vm::removeTrack,
        onFocusChanged  = vm::setInputFocused,

        // НОВОЕ: прокидываем дальше
        screenBackgroundOverride = screenBackgroundOverride,
        rowTextColorOverride     = rowTextColorOverride,
        rowIconColorOverride     = rowIconColorOverride,
        rowBackgroundOverride    = rowBackgroundOverride
    )
}
