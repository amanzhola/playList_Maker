package com.example.playlistmaker.presentation

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes

interface ImageLoader {
    fun load(target: ImageView, uri: Uri?, @DrawableRes placeholder: Int)
}