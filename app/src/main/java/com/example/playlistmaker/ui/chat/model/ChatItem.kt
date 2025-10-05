package com.example.playlistmaker.ui.chat.model

data class ChatItem(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val lastReadTs: Long?,
    val participants: List<String>,
    val lastMsgTs: Long?,        // 👈 новое
    val lastMsgSenderId: String? // 👈 новое
)
