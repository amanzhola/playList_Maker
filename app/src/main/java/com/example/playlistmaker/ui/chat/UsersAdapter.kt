package com.example.playlistmaker.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.chat.model.UserUi

class UsersAdapter(
    private val onClick: (UserUi) -> Unit
) : ListAdapter<UserUi, UsersVH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<UserUi>() {
            override fun areItemsTheSame(a: UserUi, b: UserUi) = a.uid == b.uid
            override fun areContentsTheSame(a: UserUi, b: UserUi) = a == b
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): UsersVH =
        UsersVH(LayoutInflater.from(p.context).inflate(R.layout.item_user, p, false))

    override fun onBindViewHolder(h: UsersVH, pos: Int) =
        h.bind(getItem(pos), onClick)
}

class UsersVH(v: View) : RecyclerView.ViewHolder(v) {

    private val iv: ImageView = v.findViewById(R.id.ivAvatar)
    private val tvName: TextView = v.findViewById(R.id.tvName)
    private val tvSubtitle: TextView = v.findViewById(R.id.tvSubtitle)

    fun bind(u: UserUi, onClick: (UserUi) -> Unit) {
        tvName.text = u.name ?: u.uid
        tvSubtitle.text = u.uid

        if (u.photoUrl.isNullOrBlank()) {
            iv.setImageResource(R.drawable.ic_user_placeholder) // свой плейсхолдер (см. ниже)
        } else {
            Glide.with(iv).load(u.photoUrl).circleCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(iv)
        }

        itemView.setOnClickListener { onClick(u) }
    }
}
