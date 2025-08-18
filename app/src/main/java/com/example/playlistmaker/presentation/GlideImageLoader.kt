package com.example.playlistmaker.presentation

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

class GlideImageLoader : ImageLoader {
    override fun load(target: ImageView, uri: Uri?, @DrawableRes placeholder: Int) {
        if (uri == null) {
            target.setImageResource(placeholder)
            return
        }
        Glide.with(target)
            .load(uri)
            .centerCrop()
            .placeholder(placeholder)
            .into(target)
    }
}