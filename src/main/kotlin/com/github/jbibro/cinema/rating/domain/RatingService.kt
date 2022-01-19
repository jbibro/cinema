package com.github.jbibro.cinema.rating.domain

import com.github.jbibro.cinema.CinemaException
import com.github.jbibro.cinema.ErrorCode
import com.github.jbibro.cinema.movie.domain.MovieService
import com.github.jbibro.cinema.rating.data.MongoUserRatings
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.update
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

class RatingService(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val movieService: MovieService
) {

    fun rate(userId: String, movieId: String, rating: Int): Mono<Void> {
        return Mono
            .just(rating)
            .filter { it in 1..5 }
            .switchIfEmpty { Mono.error(CinemaException(ErrorCode.INVALID_RATING, "Rating should be between 1 and 5")) }
            .flatMap {
                Flux.concat(
                    updateMovieRating(userId, movieId, rating),
                    updateUserProfile(userId, movieId, rating)
                )
                    .then()
            }
    }

    private fun updateMovieRating(userId: String, movieId: String, rating: Int) =
        previousUserRating(userId, movieId)
            .defaultIfEmpty(-1)
            .flatMap { oldRating ->
                movieService.updateMovieRating(movieId, rating, oldRating.takeIf { it > 0 })
            }

    private fun previousUserRating(userId: String, movieId: String): Mono<Int> {
        return mongoTemplate
            .findOne<MongoUserRatings>(query(where("userId").`is`(userId)))
            .mapNotNull {
                it.ratings[movieId]
            }
    }

    private fun updateUserProfile(userId: String, movieId: String, rating: Int) = mongoTemplate
        .update<MongoUserRatings>()
        .matching(query(where("userId").`is`(userId)))
        .apply(Update().set("ratings.$movieId", rating))
        .upsert()
        .then()
}
