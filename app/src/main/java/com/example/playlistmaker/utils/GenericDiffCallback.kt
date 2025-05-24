package com.example.playlistmaker.utils

import androidx.recyclerview.widget.DiffUtil

open class GenericDiffCallback<T : Identifiable<*>>(
    val oldList: List<T>,
    val newList: List<T>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldId = oldList[oldItemPosition].id
        val newId = newList[newItemPosition].id
        return oldId == newId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}