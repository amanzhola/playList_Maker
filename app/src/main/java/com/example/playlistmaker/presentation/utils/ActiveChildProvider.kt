package com.example.playlistmaker.presentation.utils

import androidx.fragment.app.Fragment

interface ActiveChildProvider {
    fun getActiveChildFragment(): Fragment?
}