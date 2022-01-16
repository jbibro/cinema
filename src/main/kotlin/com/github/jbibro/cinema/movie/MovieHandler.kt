package com.github.jbibro.cinema.movie

import com.github.jbibro.cinema.movie.api.MovieDetailsResponse
import com.github.jbibro.cinema.movie.api.MovieResponse
import com.github.jbibro.cinema.movie.data.MovieRepository
import com.github.jbibro.cinema.movie.infrastructure.OmdbClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body

class MovieHandler(
    private val repository: MovieRepository,
    private val omdbClient: OmdbClient
) {
    fun findAll() =
        ServerResponse
            .ok()
            .body(
                repository
                    .findAll()
                    .map { MovieResponse(id = it.id, title = it.title) }
            )

    fun findOne(request: ServerRequest) =
        ServerResponse
            .ok()
            .body(
                repository
                    .findById(request.pathVariable("id"))
                    .map { it.toDomain() }
                    .zipWhen { it.details(omdbClient) }
                    .map { MovieDetailsResponse.fromDomain(it.t1, it.t2) }
            )
}
