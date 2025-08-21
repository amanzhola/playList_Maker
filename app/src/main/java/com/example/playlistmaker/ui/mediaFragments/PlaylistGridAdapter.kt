package com.example.playlistmaker.ui.mediaFragments

import android.content.Context
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

class PlaylistGridAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (Playlist) -> Unit = {}
) : ListAdapter<Playlist, PlaylistGridAdapter.VH>(Diff()) {

    class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivCover)
        val name: TextView = view.findViewById(R.id.tvName)
        val count: TextView = view.findViewById(R.id.tvCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.name.text = item.name
        holder.count.text = pluralizeTracks(holder.itemView.context, item.tracksCount)

        // загрузка обложки с плейсхолдером (если coverPath == null)
        val uri = item.coverPath?.let { Uri.fromFile(File(it)) }
        imageLoader.load(holder.iv, uri, R.drawable.placeholder)

        holder.itemView.setOnClickListener { onClick(item) } // переход на экран плейлиста не требуется по ТЗ
    }

    private fun pluralizeTracks(ctx: Context, count: Int): String =
        ctx.resources.getQuantityString(R.plurals.tracks_count, count, count)

    class Diff : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(o: Playlist, n: Playlist) = o.id == n.id
        override fun areContentsTheSame(o: Playlist, n: Playlist) = o == n
    }
}
