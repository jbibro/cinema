package com.github.jbibro.cinema.movie.api

import java.time.LocalDateTime

data class MovieUpdateRequest(
    val price: Int,
    val showTimes: List<LocalDateTime>
)
