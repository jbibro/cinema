package com.github.jbibro.cinema.movie.domain

import java.time.Clock
import java.time.LocalDateTime

data class Movie(
    val id: String,
    val title: String,
    val showTimes: List<LocalDateTime>,
    val imdbId: String,
    val ratings: List<RatingToCount>
) {
    fun details(imdb: Imdb) = imdb.details(imdbId)
    fun isNowPlaying(clock: Clock) = futureShowTimes(clock).isNotEmpty()
    fun futureShowTimes(clock: Clock) = showTimes.filter { it.isAfter(LocalDateTime.now(clock)) }

    fun averageRating(): Double {
        val avg = ratings.sumOf { it.rating * it.count } / ratings.sumOf { it.count }.toDouble()
        return "%.${2}f".format(avg).toDouble()
    }
}

data class RatingToCount(
    val rating: Int,
    val count: Int
)
