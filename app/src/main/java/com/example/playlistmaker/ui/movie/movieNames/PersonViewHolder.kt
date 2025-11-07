package com.example.playlistmaker.ui.movie.movieNames

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.moviePerson.Person

class PersonViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item_person, parent, false)) {

    var photo: ImageView = itemView.findViewById(R.id.photo)
    var name: TextView = itemView.findViewById(R.id.name)
    var description: TextView = itemView.findViewById(R.id.description)

    fun bind(person: Person) {

        val url = person.photoUrl.takeIf { it.isNotBlank() }

        Glide.with(photo)
            .load(url)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)       // если загрузка не удалась/404
            .fallback(R.drawable.ic_person)    // если модель == null (после takeIf)
            .circleCrop()
            .into(photo)

        name.text = person.name
        description.text = person.description
    }
}