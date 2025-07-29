package com.example.playlistmaker.ui.audio

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.TrackItemBinding
import com.example.playlistmaker.domain.api.base.NetworkStatusChecker
import com.example.playlistmaker.domain.models.search.Track
import com.example.playlistmaker.domain.repository.base.ResourceColorProvider
import com.example.playlistmaker.utils.CLICK_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.ClickDebouncer
import com.example.playlistmaker.utils.GenericDiffCallback
import com.google.gson.Gson
import kotlinx.coroutines.MainScope

interface OnTrackClickListener {
        fun onArrowClicked(track: Track)
        fun onTrackClicked(track: Track)
    }

    class TrackAdapter(
        private var tracks: MutableList<Track>,
        resourceProvider: ResourceColorProvider,
        private val networkStatusChecker: NetworkStatusChecker,
        private val listener: OnTrackClickListener,
    ) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

        private val defaultTextColor: Int = resourceProvider.getColor(R.color.hintColor_white, null)
        private val defaultTextNameColor: Int = resourceProvider.getColor(R.color.black_white, null)
        private var textNameColor: Int = defaultTextNameColor
        private var textColor: Int = defaultTextColor
        private var arrowColor: Int = defaultTextColor

        private val clickDebouncer = ClickDebouncer(CLICK_DEBOUNCE_DELAY, MainScope())// ✨

        inner class ViewHolder(private val binding: TrackItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(track: Track) {
                binding.trackName.text = track.trackName
                binding.trackAuthor.text = track.artistName
                binding.trackDuration.text = track.trackDuration

                val radius: Int = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 2f, itemView.context.resources.displayMetrics
                ).toInt()

                val imageViewContext = binding.trackImage.context

                if (networkStatusChecker.isNetworkAvailable()) {
                    Glide.with(imageViewContext)
                        .load(track.artworkUrlSmall)
                        .placeholder(R.drawable.placeholder)
                        .transform(RoundedCorners(radius))
                        .into(binding.trackImage)
                } else {
                    Glide.with(imageViewContext)
                        .load(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .transform(RoundedCorners(radius))
                        .into(binding.trackImage)
                }

                binding.arrowFw.imageTintList = ColorStateList.valueOf(arrowColor)
                binding.trackName.setTextColor(textNameColor)
                binding.trackAuthor.setTextColor(textColor)
                binding.trackDuration.setTextColor(textColor)

                // Кнопка-стрелка
                binding.arrowFw.setOnClickListener {
                    listener.onArrowClicked(track)
                }

                // Сам item
                binding.root.setOnClickListener {
                    clickDebouncer.tryClick {
                        val trackListJson = Gson().toJson(tracks)
                        val bundle = bundleOf(
                            "TRACK_LIST_JSON" to trackListJson,
                            "TRACK_INDEX" to bindingAdapterPosition,
                            "IS_FROM_SEARCH" to true // 🟢 Новый флаг!
                        )
                        binding.root.findNavController().navigate(
                            R.id.action_searchFragment_to_extraOptionFragment,
                            bundle
                        )
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(tracks[position])
        }

        override fun getItemCount(): Int = tracks.size

        fun updateTracks(newTracks: MutableList<Track>) {
            val diffCallback = GenericDiffCallback(tracks, newTracks)
            val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)

            tracks.clear()
            tracks.addAll(newTracks)

            diffResult.dispatchUpdatesTo(this)
        }

        fun reverseTracks() { // 🔍
            val reversedTracks = tracks.reversed().toMutableList()
            updateTracks(reversedTracks)
        }

        fun setArrowColor(color: Int) {
            arrowColor = color
            notifyItemRangeChanged(0, itemCount) // 🧐
        }

        fun setTextColor(color: Int) {
            textColor = color
            textNameColor = color
            notifyItemRangeChanged(0, itemCount) // 🧐
        }
    }
