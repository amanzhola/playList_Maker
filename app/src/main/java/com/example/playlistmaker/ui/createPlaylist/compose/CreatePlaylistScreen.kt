@file:Suppress("FunctionName")

package com.example.playlistmaker.ui.createPlaylist.compose

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.createPlaylist.CreatePlaylistViewModel
import com.example.playlistmaker.ui.audioPosters.adapter.LegacyTextStyles
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("LocalContextResourcesRead", "ConfigurationScreenWidthHeight")
@Composable
fun CreatePlaylistScreen(
    state: CreatePlaylistViewModel.UiState,
    isEditMode: Boolean,
    onNameChanged: (String) -> Unit,
    onDescChanged: (String) -> Unit,
    onPickCover: (Uri?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,                  // единая точка назад (вкл. диалог)
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coverWidthFraction = context.resources.getFraction(R.fraction.cover_width_percent, 1, 1)
    //*******************************************
    val screenWidthPx = LocalConfiguration.current.screenWidthDp * context.resources.displayMetrics.density
    //*******************************************
    val scroll = rememberScrollState()

    // для каждого поля свой requester
    val nameBring = remember { BringIntoViewRequester() }
    val descBring = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var typingJob by remember { mutableStateOf<Job?>(null) }

    // тянем maxLines из ресурсов (аналог android:maxLines="@integer/qty_lines_create_playlist")
    val maxDescLines = integerResource(id = R.integer.qty_lines_create_playlist)

    // TextStyle без includeFontPadding (аналог android:includeFontPadding="false")
    val text16_noFontPad = LegacyTextStyles.text16_400().copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )

    val paddingH = dimensionResource(id = R.dimen.Padding_16)
    val vGap = dimensionResource(id = R.dimen.vertical_margin_create_playlist)
    val bottomGap = runCatching { dimensionResource(id = R.dimen.bottom_margin_create_playlist) }
        .getOrElse { 24.dp } // fallback, если нет димена

    // цвета как в XML-селекторах
    // 1) Берём нужные цвета из ресурсов (аналог цветов в ваших селекторах)
    val colorActive = colorResource(id = R.color.switch_thumb_on_color)
    val colorHint   = colorResource(id = R.color.hintColor)
    val colorText   = colorResource(id = R.color.textColor)

    // “заполнено?” → как applyFilledFlatAppearance()
    // 2) Признаки "activated" (в XML): считаем, что activated == поле заполнено
    val nameHas = state.name.isNotBlank()
    val descHas = state.desc.isNotBlank()

    // 3) Аналог edit_box_outline_selector (обводка)
    //    focused → colorActive
    //    unfocused + hasContent(activated) → colorActive
    //    unfocused + empty → colorHint
    //    disabled → colorHint
    val nameFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colorActive,
        unfocusedBorderColor = if (nameHas) colorActive else colorHint,
        disabledBorderColor  = colorHint,
        // 4) Аналог hint_color_selector (лейбл/хинт)
        focusedLabelColor    = colorActive,
        unfocusedLabelColor  = if (nameHas) colorActive else colorText,
    )

    val descFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colorActive,
        unfocusedBorderColor = if (descHas) colorActive else colorHint,
        disabledBorderColor  = colorHint,
        focusedLabelColor    = colorActive,
        unfocusedLabelColor  = if (descHas) colorActive else colorText,
    )

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onPickCover(uri) }

    fun scheduleAutoHide() {
        typingJob?.cancel()
        typingJob = scope.launch {
            delay(2000)           // 1 сек после последнего ввода
            keyboard?.hide()      // спрятать IME
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            // фон экрана как в XML: android:background="@color/white_textColor"
            .background(colorResource(id = R.color.white_textColor))
            .systemBarsPadding()
//            .navigationBarsPadding() //
            .imePadding()
            .verticalScroll(scroll)
            .padding(horizontal = paddingH),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(vGap))

        // Обложка: ширина 97% (аналог CoverConstraints width_percent=0.97), квадрат 1:1
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorResource(R.color.white_textColor), // фон как у экрана
            tonalElevation = 0.dp,                           // не тоним, чтобы цвет был чистым
            modifier = Modifier.fillMaxWidth(coverWidthFraction) // <-- percent
        ) {
            val req = remember(state.coverUri) {
                ImageRequest.Builder(context)
                    .data(state.coverUri ?: R.drawable.cover_create_playlist)
                    // ↓↓ ключевые штуки для реальных девайсов ↓↓
                    .size(screenWidthPx.toInt())     // даунсемпл до ширины экрана
                    .allowHardware(false)            // аппаратный битмап иногда чудит на OEM
                    .crossfade(false)                // чтобы не держать лишние кадры
                    .build()
            }
            val painter = rememberAsyncImagePainter(model = req)
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            )
        }

        Spacer(Modifier.height(vGap))

        // Название (обязательное) — стиль Text16 из набора
        OutlinedTextField(
            value = state.name,
            onValueChange = {
                onNameChanged(it)
                scheduleAutoHide()
            },
            label = { Text(text = stringResource(R.string.name_new_playlist), style = LegacyTextStyles.text16_400()) },
            singleLine = true,
            textStyle = LegacyTextStyles.text16_400(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            colors = nameFieldColors, // ← здесь подключён аналог селектора boxStroke + hint
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(nameBring)
                .onFocusEvent { ev ->
                    if (ev.isFocused) {
                        scope.launch {
                            delay(100)                 // дать IME выехать
                            nameBring.bringIntoView()  // прокрутить поле над клавой
                        }
                    } else {
                        keyboard?.hide()
                    }
                }
        )

        Spacer(Modifier.height(12.dp)) // нужно в dimens

        // Описание (опционально) — мультистрочное, minLines=1, maxLines из ресурсов
        OutlinedTextField(
            value = state.desc,
            onValueChange = {
                onDescChanged(it)
                scheduleAutoHide()
            },
            label = { Text(text = stringResource(R.string.description), style = text16_noFontPad) },
            singleLine = false,              // == android:inputType="textMultiLine"
            minLines = 1,                    // == android:minLines="1"
            maxLines = maxDescLines,         // == android:maxLines="@integer/qty_lines_create_playlist"
            textStyle = text16_noFontPad,    // == style="@style/Text16" + includeFontPadding="false"
            colors = descFieldColors, // ← здесь подключён аналог селектора boxStroke + hint
            keyboardOptions = KeyboardOptions( // multiline: не форсируем Done
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(descBring)
                .onFocusEvent { ev ->
                    if (ev.isFocused) {
                        scope.launch {
                            delay(100)
                            descBring.bringIntoView()
                        }
                    }else {
                        keyboard?.hide()
                    }
                }
        )

        Spacer(Modifier.weight(1f))

        // Кнопка “Создать/Сохранить” — 1:1 с XML-селектором backgroundTint
        Button(
            onClick = onSave,
            enabled = state.name.isNotBlank(),
            shape = RectangleShape, // ← квадратная, без скруглений
            colors = ButtonDefaults.buttonColors(
                // enabled → switch_thumb_on_color
                containerColor         = colorResource(R.color.switch_thumb_on_color),
                contentColor           = colorResource(R.color.white1),
                // disabled → hintColor
                disabledContainerColor = colorResource(R.color.hintColor),
                disabledContentColor   = colorResource(R.color.white1)  // или полупрозрачным, если нужно
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp,0.dp,0.dp,0.dp,0.dp),
            modifier = Modifier
                .fillMaxWidth()
                // ↓↓↓ добавляем только нижний inset от клавиатуры
                .windowInsetsPadding(WindowInsets.ime.only(WindowInsetsSides.Bottom))
                .padding(bottom = bottomGap)
        ) {
            Text(
                text  = stringResource(if (isEditMode) R.string.save else R.string.create),
                style = LegacyTextStyles.text16_500()
            )
        }
    }
}
