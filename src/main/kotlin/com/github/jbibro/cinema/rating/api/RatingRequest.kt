package com.github.jbibro.cinema.rating.api

data class RatingRequest(
    val movieId: String,
    val rating: Int
)
