package com.github.jbibro.cinema.movie

import com.github.jbibro.cinema.CinemaException
import com.github.jbibro.cinema.asException
import com.github.jbibro.cinema.movie.api.MovieDetailsResponse
import com.github.jbibro.cinema.movie.api.MovieResponse
import com.github.jbibro.cinema.movie.api.MovieShowTimes
import com.github.jbibro.cinema.movie.api.MovieUpdateRequest
import com.github.jbibro.cinema.movie.data.MovieRepository
import com.github.jbibro.cinema.movie.domain.MovieService
import com.github.jbibro.cinema.toServerResponse
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

class MovieHandler(
    private val movieService: MovieService,
    private val repository: MovieRepository,
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
                movieService.findOne(request.pathVariable("id"))
                    .map { MovieDetailsResponse.fromDomain(it.t1, it.t2) }
            )

    fun findShowTimes(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        return movieService
            .findShowTimes(id)
            .flatMap {
                ServerResponse.ok().bodyValue(MovieShowTimes(id, it))
            }
            .onErrorResume(
                { it is CinemaException },
                { it.asException<CinemaException>().toServerResponse() }
            )
    }

    fun updatePriceAndShowTimes(request: ServerRequest) =
        request
            .bodyToMono<MovieUpdateRequest>()
            .flatMap {
                movieService.updatePriceAndShowTimes(
                    request.pathVariable("id"),
                    it.price,
                    it.showTimes
                )
            }
            .flatMap { ServerResponse.ok().build() }
}
