package com.example.playlistmaker.presentation.utils

/** Фрагмент может сообщить какие view НЕ красить рекурсивно (по id). */
interface BackgroundExclusionProvider {
    /** id вьюх, которым НЕ меняем background при сегменте 1 */
    fun backgroundExclusionIds(): Set<Int>
}
