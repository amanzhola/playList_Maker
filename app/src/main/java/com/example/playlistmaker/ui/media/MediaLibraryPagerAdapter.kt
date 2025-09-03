package com.example.playlistmaker.ui.media


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.domain.models.media.MediaTab

class MediaLibraryPagerAdapter(
    fragment: Fragment, // 🔄 заменили activity → fragment
    private val tabs: List<MediaTab>
) : FragmentStateAdapter(fragment) { // 🔄 fragment, а не activity

    override fun getItemCount(): Int = tabs.size
    override fun createFragment(position: Int): Fragment = tabs[position].createFragment()
}
