package com.example.playlistmaker.presentation.utils

class ColorManager(
    private val colorApplierHelper: ColorApplierHelper,
    private val colorPersistenceHelper: ColorPersistenceHelper,
    private val recreateActivity: () -> Unit
) {

    fun applySavedColors(range: IntRange = 0..4) {
        for (index in range) {
            val color = colorPersistenceHelper.load(index)
            if (color != -1) {
                color?.let { colorApplierHelper.apply(index, it) }
            }
        }
    }

    fun clearAllColors(range: IntRange = 0..4) {
        colorPersistenceHelper.clear(range)
        recreateActivity()
    }
}
