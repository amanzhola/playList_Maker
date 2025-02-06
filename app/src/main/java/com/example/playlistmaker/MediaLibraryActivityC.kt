package com.example.playlistmaker

import android.os.Bundle
class MediaLibraryActivityC : BaseActivityC() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media1)

        setupToolbar(R.id.toolbar)
    }
}