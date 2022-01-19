package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.MovieHandler
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

class MovieApi(private val handler: MovieHandler) {
    fun router() = router {
        "/api".nest {
            accept(APPLICATION_JSON).nest {
                GET("/movies") { handler.findAll() }
                GET("/movie/{id}", handler::findOne)
                GET("/movie/{id}/show-times", handler::findShowTimes)
                PUT("/movie/{id}", handler::updatePriceAndShowTimes)
            }
        }
    }
}
