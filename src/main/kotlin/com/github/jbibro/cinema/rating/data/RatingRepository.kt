package com.github.jbibro.cinema.rating.data

import com.github.jbibro.cinema.rating.infrastructure.CustomizedRatingRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface RatingRepository : ReactiveMongoRepository<MongoUserRatings, String>, CustomizedRatingRepository {
    fun findFirstByUserId(userId: String): Mono<MongoUserRatings>
}
