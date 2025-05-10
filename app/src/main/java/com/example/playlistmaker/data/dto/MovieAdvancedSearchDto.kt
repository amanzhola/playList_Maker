package com.example.playlistmaker.data.dto

data class MovieAdvancedSearchDto(val id: String,
                                  val resultType: String,// ❓
                                  val image: String,
                                  val title: String,
                                  val description: String?, // ✨ ⭐ 👤(searchMovies) or 📅 (getAdvancedSearch)
                                  val runtimeStr: String, // 🕐
                                  val genres: String? = null, // 📚
                                  val plot: String? = null, // 📝
                                  val imDbRating: String? = null // ⭐
)