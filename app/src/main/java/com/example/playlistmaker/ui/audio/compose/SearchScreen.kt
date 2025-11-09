package com.example.playlistmaker.ui.audio.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.presentation.searchViewModels.ErrorState
import com.example.playlistmaker.presentation.searchViewModels.models.SearchUiState
import com.example.playlistmaker.presentation.utils.deriveFieldBgFromScreen
import com.example.playlistmaker.utils.FailBlock
import com.example.playlistmaker.utils.TrackRow
import com.example.playlistmaker.utils.UpdateButton
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onHistoryClear: () -> Unit,
    onItemClick: (Track) -> Unit,
    onRemoveClick: (Track) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    screenBackgroundOverride: Color? = null,
    rowTextColorOverride: Color? = null,
    rowIconColorOverride: Color? = null,
    rowBackgroundOverride: Color? = null,
    scrollToTopFlow: kotlinx.coroutines.flow.Flow<Unit>? = null  // НОВОЕ:
) {
    val listState = rememberLazyListState()
    val bg = screenBackgroundOverride ?: colorResource(R.color.white_textColor)

    val fieldBgForSearch =
        screenBackgroundOverride?.let { deriveFieldBgFromScreen(it) }

    // НОВОЕ: ловим сигнал и скроллим
    LaunchedEffect(scrollToTopFlow, listState) {
        scrollToTopFlow?.let { flow ->
            flow.collectLatest {
                // даём Compose применить изменения списка
                withFrameNanos { /* один кадр ожидания */ }
                // а теперь прокручиваем к началу (можно animateScrollToItem, если нужна анимация)
                listState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(bg)
    ) {
        SearchInput(
            state = state,
            onQueryChange = onQueryChange,
            onClear = onClear,
            onFocusChanged = onFocusChanged,
            fieldBgOverride = fieldBgForSearch
        )
        // Заголовок "История"
        if (state.showHistory) {
            Text(
                text = stringResource(R.string.search_history),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.arrow_back_24),
                        bottom = dimensionResource(R.dimen.searchPaddingTopBottom_8)
                    ),
                textAlign = TextAlign.Center,
                color = colorResource(R.color.black_white),
                fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Прогресс
        if (state.query.isNotBlank() && state.isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(R.dimen.track_45),
                        bottom = dimensionResource(R.dimen.Padding_16)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorResource(R.color.blue))
            }
        }
        // Ошибки
        val hasError = state.error != ErrorState.NONE
        when (state.error) {
            ErrorState.ERROR   -> FailBlock(R.string.searchFail, enabled = true)
            ErrorState.FAILURE -> FailBlock(R.string.networkFail, enabled = false)
            ErrorState.NONE    -> Unit
        }
        // Флаги кнопки
        val showClean  = state.error == ErrorState.NONE && state.showHistory
        val showUpdate = state.error == ErrorState.FAILURE
        val showBottomButton = showClean || showUpdate

        // Нижний «контейнер»: список + кнопка.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // занимает остаток экрана
        ) {
            // высота, которую нужно зарезервировать под кнопку
            val btnHeight = if (showBottomButton) dimensionResource(R.dimen.searchLineHeight_52) else 0.dp
            val btnVPad   = if (showBottomButton) dimensionResource(R.dimen.searchPaddingTopBottom_8) else 0.dp
            val reserved  = btnHeight + btnVPad
            val maxListHeight = maxHeight - reserved

            Column(Modifier.fillMaxSize()) {
                if (!hasError) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            // <<< ограничение высоты списка
                            .heightIn(max = maxListHeight),
                    ) {
                        items(
                            items = state.displayedTracks,
                            key = { it.trackId }
                        ) { track ->
                            TrackRow(
                                track = track,
                                onClick = { onItemClick(track) },
                                onRemove = { onRemoveClick(track) },
                                nameColorOverride          = rowTextColorOverride ?: state.listTextColor?.let { Color(it) },
                                arrowColorOverride         = rowIconColorOverride ?: state.listArrowColor?.let { Color(it) },
                                rowBackgroundColorOverride = rowBackgroundOverride ?: state.listItemBackgroundColor?.let { Color(it) }
                            )
                        }
                    }
                } else {
                    // когда ошибка — просто занимаем доступную высоту под ошибку
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxListHeight)
                    )
                }
                if (showBottomButton) {
                    UpdateButton(
                        textRes = if (showClean) R.string.clean else R.string.update,
                        onClick = { if (showClean) onHistoryClear() else onRetry() },
                        modifier = Modifier.padding(top = btnVPad),
                        minHeight = btnHeight
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Поле ввода: те же размеры/цвета, не «режет» текст, репортит фокус во VM.
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchInput(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    fieldBgOverride: Color? = null
) {
    val paddingH = dimensionResource(R.dimen.searchPaddingLeftRight_16)
    val paddingV = dimensionResource(R.dimen.searchPaddingTopBottom_8)
    val fieldMinHeight = dimensionResource(R.dimen.searchLineHeight_52)
    val fieldBg = fieldBgOverride ?: colorResource(R.color.hintField_white)
    val hintColor = colorResource(R.color.hintColor_textColor)
    val cursorColor = colorResource(R.color.cursorColor)
    val ysDisplay = remember { FontFamily(Font(R.font.ys_display_regular)) }

    var text by remember(state.query) { mutableStateOf(state.query) }

    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { inter ->
            when (inter) {
                is FocusInteraction.Focus   -> onFocusChanged(true)
                is FocusInteraction.Unfocus -> onFocusChanged(false)
            }
        }
    }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingH, vertical = paddingV)
            .defaultMinSize(minHeight = fieldMinHeight),
        value = text,
        onValueChange = {
            text = it
            onQueryChange(it)
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
            fontFamily = ysDisplay,
            fontWeight = FontWeight.W400,
            platformStyle = PlatformTextStyle(includeFontPadding = true)
        ),
        placeholder = {
            if (state.query.isBlank() && !state.isInputFocused) {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = hintColor,
                    fontSize = dimensionResource(R.dimen.Settings_Text_16).value.sp,
                    fontFamily = ysDisplay,
                    fontWeight = FontWeight.W400
                )
            }
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.search_icon),
                contentDescription = null,
                tint = hintColor
            )
        },
        trailingIcon = {
            if (state.isClearIconVisible) {
                IconButton(onClick = {
                    onClear()
                    text = ""
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.clear_search),
                        tint = hintColor
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            cursorColor = cursorColor
        ),
        shape = MaterialTheme.shapes.small,
        interactionSource = interaction
    )
}