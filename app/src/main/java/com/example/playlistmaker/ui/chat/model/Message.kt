package com.example.playlistmaker.ui.chat.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String? = null,
    val mediaUrl: String? = null,
    val createdAt: Long = 0L
)
