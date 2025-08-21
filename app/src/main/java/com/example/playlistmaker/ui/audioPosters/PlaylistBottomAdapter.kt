package com.example.playlistmaker.ui.audioPosters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.playlist.Playlist
import com.example.playlistmaker.presentation.ImageLoader
import java.io.File

class PlaylistBottomAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistBottomAdapter.VH>(Diff()) {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivCover)
        val name: TextView = v.findViewById(R.id.tvName)
        val count: TextView = v.findViewById(R.id.tvCount)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_playlist_bottom, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.name.text = item.name
        h.count.text = if (item.tracksCount == 1) "1 трек" else "${item.tracksCount} треков"
        val uri = item.coverPath?.let { Uri.fromFile(File(it)) }
        imageLoader.load(h.iv, uri, R.drawable.placeholder)
        h.itemView.setOnClickListener { onClick(item) }
    }

    class Diff : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(a: Playlist, b: Playlist) = a.id == b.id
        override fun areContentsTheSame(a: Playlist, b: Playlist) = a == b
    }
}
