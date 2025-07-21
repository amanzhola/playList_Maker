package com.example.playlistmaker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
fun <T> Flow<T>.collectDebouncedIn(
    scope: CoroutineScope,
    debounceMillis: Long,
    action: suspend (T) -> Unit
) = scope.launch {
    this@collectDebouncedIn
        .debounce(debounceMillis)
        .collect { action(it) }
}
