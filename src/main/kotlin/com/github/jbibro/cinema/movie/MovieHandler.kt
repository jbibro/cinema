package com.github.jbibro.cinema.movie

import com.github.jbibro.cinema.CinemaException
import com.github.jbibro.cinema.ErrorCode.MOVIE_NOT_FOUND
import com.github.jbibro.cinema.ErrorCode.NO_UPCOMING_SHOWTIMES
import com.github.jbibro.cinema.asException
import com.github.jbibro.cinema.movie.api.MovieDetailsResponse
import com.github.jbibro.cinema.movie.api.MovieResponse
import com.github.jbibro.cinema.movie.api.MovieShowTimes
import com.github.jbibro.cinema.movie.data.MovieRepository
import com.github.jbibro.cinema.movie.infrastructure.OmdbClient
import com.github.jbibro.cinema.toServerResponse
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.time.Clock

class MovieHandler(
    private val repository: MovieRepository,
    private val omdbClient: OmdbClient,
    private val clock: Clock
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
                    .switchIfEmpty(Mono.error(CinemaException(MOVIE_NOT_FOUND)))
                    .map { it.toDomain() }
                    .zipWhen { it.details(omdbClient) }
                    .map { MovieDetailsResponse.fromDomain(it.t1, it.t2) }
            )

    fun findShowTimes(request: ServerRequest): Mono<ServerResponse> =
        repository
            .findById(request.pathVariable("id"))
            .switchIfEmpty(Mono.error(CinemaException(MOVIE_NOT_FOUND)))
            .map { it.toDomain() }
            .filter { it.isNowPlaying(clock) }
            .switchIfEmpty(Mono.error(CinemaException(NO_UPCOMING_SHOWTIMES)))
            .flatMap {
                ServerResponse.ok().bodyValue(MovieShowTimes(it.id, it.futureShowTimes(clock)))
            }
            .onErrorResume(
                { it is CinemaException },
                { it.asException<CinemaException>().toServerResponse() }
            )
}
