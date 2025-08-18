package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.GlideImageLoader
import com.example.playlistmaker.presentation.ImageLoader
import org.koin.dsl.module

val imageLoaderModule = module {
    single<ImageLoader> { GlideImageLoader() }
}