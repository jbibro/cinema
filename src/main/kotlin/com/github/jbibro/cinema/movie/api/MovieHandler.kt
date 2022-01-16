package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.data.MovieRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body

class MovieHandler(private val repository: MovieRepository) {
    fun findAll() =
        ServerResponse
            .ok()
            .body(
                repository
                    .findAll()
                    .map { MovieResponse(id = it.id, title = it.title) }
            )
}