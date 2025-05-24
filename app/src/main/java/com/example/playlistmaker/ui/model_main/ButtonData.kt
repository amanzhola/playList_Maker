package com.example.playlistmaker.ui.model_main

import com.example.playlistmaker.R

data class ButtonData(val text: String, val iconRes: Int?, val isIconVisible: Boolean)

fun getButtonPairs(): List<ButtonData> {
    return listOf(
        ButtonData("Button 1", R.id.button1, false),
        ButtonData("Button 2", R.id.button2, false),
        ButtonData("Button 3", R.id.button3, false),
        ButtonData("Button 4", R.id.button4, false),
        ButtonData("Button 5", R.id.button5, false),
        ButtonData("Button 6", R.id.button6, false)
    )
}