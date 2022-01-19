package com.github.jbibro.cinema.rating

import com.github.jbibro.cinema.CinemaException
import com.github.jbibro.cinema.asException
import com.github.jbibro.cinema.rating.api.RatingRequest
import com.github.jbibro.cinema.rating.domain.RatingService
import com.github.jbibro.cinema.toServerResponse
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

class RatingHandler(private val service: RatingService) {
    fun rate(request: ServerRequest): Mono<ServerResponse> {
        val userId = request.pathVariable("id")
        return request
            .bodyToMono<RatingRequest>()
            .flatMap { service.rate(userId, it.movieId, it.rating) }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume(
                { it is CinemaException },
                { it.asException<CinemaException>().toServerResponse() }
            )
    }
}
