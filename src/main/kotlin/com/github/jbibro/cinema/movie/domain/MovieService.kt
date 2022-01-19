package com.github.jbibro.cinema.movie.domain

import com.github.jbibro.cinema.CinemaException
import com.github.jbibro.cinema.ErrorCode
import com.github.jbibro.cinema.movie.data.MongoMovie
import com.github.jbibro.cinema.movie.data.MovieRepository
import com.github.jbibro.cinema.movie.infrastructure.OmdbClient
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.update
import reactor.core.publisher.Mono
import java.time.Clock
import java.time.LocalDateTime

class MovieService(
    private val repository: MovieRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
    private val omdbClient: OmdbClient,
    private val clock: Clock
) {

    fun findOne(id: String) =
        repository
            .findById(id)
            .map { it.toDomain() }
            .zipWhen { it.details(omdbClient) }

    fun findShowTimes(id: String) =
        repository
            .findById(id)
            .switchIfEmpty(Mono.error(CinemaException(ErrorCode.MOVIE_NOT_FOUND)))
            .map { it.toDomain() }
            .filter { it.isNowPlaying(clock) }
            .switchIfEmpty(Mono.error(CinemaException(ErrorCode.NO_UPCOMING_SHOWS)))
            .map { it.futureShowTimes(clock) }

    fun updatePriceAndShowTimes(id: String, newPrice: Int, newShowTimes: List<LocalDateTime>) =
        mongoTemplate
            .update<MongoMovie>()
            .matching(query(where("id").`is`(id)))
            .apply(
                Update()
                    .set("price", newPrice)
                    .set("showTimes", newShowTimes)
            )
            .first()
            .then()

    fun updateMovieRating(movieId: String, rating: Int, oldUserRating: Int? = null) =
        repository
            .existsById(movieId)
            .filter { it == true }
            .switchIfEmpty(Mono.error(CinemaException(ErrorCode.MOVIE_NOT_FOUND)))
            .flatMap {
                mongoTemplate
                    .update<MongoMovie>()
                    .matching(query(where("id").`is`(movieId)))
                    .apply(
                        Update()
                            .inc("ratings.$rating", 1)
                            .removeOldRating(oldUserRating)
                    )
                    .first()
                    .then()
            }

    private fun Update.removeOldRating(oldUserRating: Int?): Update =
        apply {
            oldUserRating?.let {
                inc("ratings.$it", -1)
            }
        }
}
