package com.example.playlistmaker.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.chat.model.Message

class MessageAdapter(private val myId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<Message>()
    fun submit(list: List<Message>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun getItemCount() = items.size
    override fun getItemViewType(p: Int) = if (items[p].senderId == myId) 1 else 0

    override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(p.context)
        return if (t == 1) OutVH(inf.inflate(R.layout.item_message_out, p, false))
        else InVH(inf.inflate(R.layout.item_message_in, p, false))
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        val m = items[pos]
        val v = h.itemView
        val txt = v.findViewById<TextView>(R.id.txt)
        val img = v.findViewById<ImageView>(R.id.img)

        if (m.text.isNullOrBlank()) txt.visibility = View.GONE
        else { txt.text = m.text; txt.visibility = View.VISIBLE }

        if (m.mediaUrl.isNullOrBlank()) img.visibility = View.GONE
        else { img.visibility = View.VISIBLE; Glide.with(img).load(m.mediaUrl).into(img) }
    }

    class InVH(v: View): RecyclerView.ViewHolder(v)
    class OutVH(v: View): RecyclerView.ViewHolder(v)
}
