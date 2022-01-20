package com.github.jbibro.cinema.rating.infrastructure

import com.github.jbibro.cinema.rating.data.MongoUserRatings
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.update
import reactor.core.publisher.Mono

interface CustomizedRatingRepository {
    fun addOrUpdateRating(movieId: String, rating: Int, userId: String): Mono<Void>
}

class CustomizedRatingRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate
) : CustomizedRatingRepository {
    override fun addOrUpdateRating(movieId: String, rating: Int, userId: String) =
        mongoTemplate
            .update<MongoUserRatings>()
            .matching(query(where("userId").`is`(userId)))
            .apply(Update().set("ratings.$movieId", rating))
            .upsert()
            .then()
}
