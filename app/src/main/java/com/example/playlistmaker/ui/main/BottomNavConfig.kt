package com.example.playlistmaker.ui.main

interface BottomNavConfig {
    fun getBottomNavButtonIndex(): Int?     // какая кнопка должна быть активной
    fun shouldShowFullBottomNav(): Boolean  // нужно ли показывать все 6 или только 3
    fun shouldShowBottomNav(): Boolean      // нужно ли вообще показывать
}
