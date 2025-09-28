package com.example.playlistmaker.ui.chat

import android.annotation.SuppressLint
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
import com.example.playlistmaker.ui.chat.model.ChatItem
import com.example.playlistmaker.ui.chat.model.UserUi

class ChatsAdapter(
    private val myUid: String,
    // как получить профиль по uid (из Map кэша/репозитория)
    private val profileOf: (String) -> UserUi?,
    private val onClick: (ChatItem) -> Unit,
    private val onLongClick: ((ChatItem) -> Unit)? = null
) : ListAdapter<ChatItem, ChatsAdapter.VH>(Diff) {

    /** Точный diff + «частичные» payload’ы, чтобы не перерисовывать весь айтем */
    object Diff : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(a: ChatItem, b: ChatItem): Boolean = a.id == b.id

        override fun areContentsTheSame(a: ChatItem, b: ChatItem): Boolean {
            if (a.title != b.title) return false
            if (a.updatedAt != b.updatedAt) return false
            if (a.lastReadTs != b.lastReadTs) return false
            if (a.lastMsgTs != b.lastMsgTs) return false
            if (a.lastMsgSenderId != b.lastMsgSenderId) return false
            if (a.participants.size != b.participants.size) return false
            return a.participants.zip(b.participants).all { (x, y) -> x == y }
        }

        override fun getChangePayload(old: ChatItem, new: ChatItem): Any? {
            val keys = mutableSetOf<String>()
            if (old.updatedAt != new.updatedAt ||
                old.lastReadTs != new.lastReadTs ||
                old.lastMsgTs != new.lastMsgTs ||
                old.lastMsgSenderId != new.lastMsgSenderId
            ) keys += "badge"

            if (old.title != new.title || old.participants != new.participants) {
                keys += listOf("title", "subtitle", "avatar")
            }
            return if (keys.isEmpty()) null else keys
        }
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivAvatar: ImageView = v.findViewById(R.id.ivAvatar)
        private val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView? = v.findViewById(R.id.tvSubtitle)
        private val tvBadge: TextView = v.findViewById(R.id.tvBadge)

        @SuppressLint("SetTextI18n")
        fun bindFull(item: ChatItem) {
            tvTitle.text = computeTitle(item)
            bindSubtitle(item)
            bindBadge(item)
            loadAvatar(item)

            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick?.invoke(item)
                onLongClick != null // true => событие «съедено» если колбэк передан
            }
        }

        fun bindPartial(item: ChatItem, keys: Set<String>) {
            if ("title" in keys) tvTitle.text = computeTitle(item)
            if ("subtitle" in keys) bindSubtitle(item)
            if ("badge" in keys) bindBadge(item)
            if ("avatar" in keys) loadAvatar(item)
        }

        private fun computeTitle(item: ChatItem): CharSequence {
            if (item.title.isNotBlank()) return item.title
            val peerUid = item.participants.firstOrNull { it != myUid } ?: return "Chat"
            return profileOf(peerUid)?.name?.takeIf { it.isNotBlank() } ?: "Chat"
        }

        private fun bindSubtitle(item: ChatItem) {
            val others = item.participants.filter { it != myUid }
            val text = when (others.size) {
                0 -> ""
                1 -> profileOf(others.first())?.status ?: ""
                else -> "${item.participants.size} участников"
            }
            tvSubtitle?.apply {
                this.text = text
                visibility = if (text.isBlank()) View.GONE else View.VISIBLE
            }
        }

        private fun bindBadge(item: ChatItem) {
            val lastRead = item.lastReadTs ?: 0L
            val lastMsgTs = item.lastMsgTs ?: item.updatedAt // fallback на updatedAt, если вдруг нет lastMsgTs
            val lastFromOther = item.lastMsgSenderId?.let { it != myUid } ?: true
            val hasUnseen = lastFromOther && lastMsgTs > lastRead

            android.util.Log.d(
                "Badge",
                "chat=${item.id} lastTs=${item.lastMsgTs ?: item.updatedAt} " +
                        "read=${item.lastReadTs ?: 0L} fromOther=${item.lastMsgSenderId?.let { it != myUid } ?: true} unseen=$hasUnseen"
            )

            tvBadge.text = "\uD83D\uDD25"
            tvBadge.visibility = if (hasUnseen) View.VISIBLE else View.GONE
        }

        private fun loadAvatar(item: ChatItem) {
            val peerUid = item.participants.firstOrNull { it != myUid }
            val photoUrl = peerUid?.let { profileOf(it)?.photoUrl }
            if (photoUrl.isNullOrBlank()) {
                ivAvatar.setImageResource(R.drawable.ic_user_placeholder)
            } else {
                Glide.with(ivAvatar)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(ivAvatar)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return VH(v)
    }

    // единая точка биндинга: если payloads пуст — полный биндинг
    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (payloads.isEmpty()) {
            holder.bindFull(item)
        } else {
            val keys = payloads.flatMap {
                when (it) {
                    is String -> listOf(it)
                    is Collection<*> -> it.filterIsInstance<String>()
                    else -> emptyList()
                }
            }.toSet()
            holder.bindPartial(item, keys)
        }
    }

    // делегируем в версию с payloads, чтобы не дублировать логику
    override fun onBindViewHolder(holder: VH, position: Int) =
        onBindViewHolder(holder, position, mutableListOf())
}
