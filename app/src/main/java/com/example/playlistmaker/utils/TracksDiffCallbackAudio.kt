package com.example.playlistmaker.utils

import com.example.playlistmaker.domain.models.Track

class TracksDiffCallbackAudio(
    oldList: List<Track>,
    newList: List<Track>
) : GenericDiffCallback<Track>(oldList, newList) {

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        val changes = mutableListOf<String>()

        if (oldItem.isPlaying != newItem.isPlaying) {
            changes.add("isPlaying")
        }
        if (oldItem.playTime != newItem.playTime) {
            changes.add("playTime")
        }

        return if (changes.isEmpty()) null else changes
    }
}
