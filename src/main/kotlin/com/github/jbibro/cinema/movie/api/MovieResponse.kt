package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.domain.Movie
import com.github.jbibro.cinema.movie.domain.MovieDetails
import java.time.Duration
import java.time.LocalDate

data class MovieResponse(
    val id: String,
    val title: String
)

data class MovieDetailsResponse(
    val id: String,
    val title: String,
    val description: String,
    val releaseDate: LocalDate,
    val runtime: Duration,
    val imdbRating: Double
) {
    companion object {
        fun fromDomain(movie: Movie, movieDetails: MovieDetails) = MovieDetailsResponse(
            id = movie.id,
            title = movie.title,
            description = movieDetails.description,
            releaseDate = movieDetails.releaseDate,
            runtime = movieDetails.runtime,
            imdbRating = movieDetails.imdbRating
        )
    }
}
