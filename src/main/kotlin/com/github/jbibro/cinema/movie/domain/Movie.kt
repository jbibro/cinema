package com.github.jbibro.cinema.movie.domain

data class Movie(
    val id: String,
    val title: String,
    val imdbId: String
) {
    fun details(imdb: Imdb) = imdb.details(imdbId)
}
