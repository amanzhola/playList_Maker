package com.example.playlistmaker.ui.movie.movieCast

import com.example.playlistmaker.domain.models.movieCast.MovieCastPerson
import com.example.playlistmaker.ui.core.ui.RVItem

//sealed interface MoviesCastRVItem {
//
//    data class HeaderItem(
//        val headerText: String,
//    ) : MoviesCastRVItem
//
//    data class PersonItem(
//        val data: MovieCastPerson,
//    ) : MoviesCastRVItem
//
//}

sealed interface MoviesCastRVItem : RVItem {

    data class HeaderItem(
        val headerText: String,
    ) : MoviesCastRVItem

    data class PersonItem(
        val data: MovieCastPerson,
    ) : MoviesCastRVItem

}