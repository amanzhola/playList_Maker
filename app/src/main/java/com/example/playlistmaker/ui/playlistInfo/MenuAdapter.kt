package com.example.playlistmaker.ui.playlistInfo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.playlistInfo.model.MenuRow

// создан чтобы первым строке запукать карту плай листа и далее 3 текста
class MenuAdapter(
    private val onAction: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<MenuRow>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(list: List<MenuRow>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is MenuRow.Header -> 0
        is MenuRow.Action -> 1
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): RecyclerView.ViewHolder {
        return if (vt == 0) {
            val v = LayoutInflater.from(p.context)
                .inflate(R.layout.item_playlist_bottom, p, false)
            HeaderVH(v)
        } else {
            val v = LayoutInflater.from(p.context)
                .inflate(R.layout.item_menu_action, p, false)
            ActionVH(v, onAction)
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        when (val row = data[pos]) {
            is MenuRow.Header -> (h as HeaderVH).bind(row)
            is MenuRow.Action -> (h as ActionVH).bind(row)
        }
    }

    override fun getItemCount() = data.size

    class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val iv = v.findViewById<ImageView>(R.id.ivCover)
        private val name = v.findViewById<TextView>(R.id.tvName)
        private val count = v.findViewById<TextView>(R.id.tvCount)
        fun bind(m: MenuRow.Header) {
            name.text = m.name
            count.text = m.count
            // m.cover может быть Uri/File/URL/Null — как загружаем
            Glide.with(iv)
                .load(m.cover)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .centerCrop()
                .into(iv)
        }
    }

    class ActionVH(v: View, private val onAction: (Int) -> Unit) : RecyclerView.ViewHolder(v) {
        private val tv = v.findViewById<TextView>(R.id.tvTitle)
        fun bind(m: MenuRow.Action) {
            tv.text = m.title
            tv.setOnClickListener { onAction(m.id) }
        }
    }
}
