package com.example.playlistmaker.presentation.utils

class ColorManager(
    private val colorApplierHelper: ColorApplierHelper,
    private val colorPersistenceHelper: ColorPersistenceHelper,
    // Лямбда, которая возвращает ТЕКУЩИЙ scope (экран/вкладка или активити)
    private val getScope: () -> String,
    private val recreateActivity: () -> Unit
) {
    /** Применить сохранённые цвета для ТЕКУЩЕГО scope (фрагмент/таба или активити) */
    fun applySavedColors(range: IntRange = 0..4, scopeOverride: String? = null) {
        val scope = scopeOverride ?: getScope()
        for (index in range) {
            val color = colorPersistenceHelper.load(scope, index)
            color
                ?.takeIf { it != -1 }
                ?.let { c -> colorApplierHelper.apply(index, c) }
        }
    }

    /** Очистить все слоты для ТЕКУЩЕГО scope (фрагмент/таба или активити) */
    fun clearAllColors(range: IntRange = 0..4, scopeOverride: String? = null) {
        val scope = scopeOverride ?: getScope()
        colorPersistenceHelper.clear(scope, range)
        recreateActivity()
    }
}
