package com.github.jbibro.cinema.movie.domain

import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate

interface Imdb {
    fun details(id: String): Mono<MovieDetails>
}

data class MovieDetails(
    val title: String,
    val description: String,
    val releaseDate: LocalDate,
    val runtime: Duration,
    val imdbRating: Double
)
