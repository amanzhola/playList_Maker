package com.example.playlistmaker.ui.audioPosters.sheet

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "noRippleClickable" }
) {
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) { onClick() }
    )
}
