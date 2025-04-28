package com.example.playlistmaker.extraOption

import androidx.recyclerview.widget.DiffUtil
import com.example.playlistmaker.search.Track

class TracksDiffCallbackAudio(
    private val oldList: List<Track>,
    private val newList: List<Track>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].trackId == newList[newItemPosition].trackId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        val changes = mutableListOf<String>()

        if (oldItem.isPlaying != newItem.isPlaying) { // 👨‍💻✨
            changes.add("isPlaying")
        }

        if (oldItem.playTime != newItem.playTime) { // 👨‍💻✨
            changes.add("playTime")
        }

        return if (changes.isEmpty()) null else changes
    }
}
