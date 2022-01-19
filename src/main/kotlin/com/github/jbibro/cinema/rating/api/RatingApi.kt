package com.github.jbibro.cinema.rating.api

import com.github.jbibro.cinema.rating.RatingHandler
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

class RatingApi(private val handler: RatingHandler) {
    fun router() = router {
        "/api".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                POST("/users/{id}/movie-ratings", handler::rate)
            }
        }
    }
}
