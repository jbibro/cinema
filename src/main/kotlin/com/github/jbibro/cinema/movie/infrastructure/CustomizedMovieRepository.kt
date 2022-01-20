package com.github.jbibro.cinema.movie.infrastructure

import com.github.jbibro.cinema.movie.data.MongoMovie
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.update
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface CustomizedMovieRepository {
    fun updatePriceAndShowTimes(price: Int, showTimes: List<LocalDateTime>, movieId: String): Mono<Void>
    fun incAndDecRatings(ratingToInc: Int, ratingToDec: Int?, movieId: String): Mono<Void>
}

class CustomizedMovieRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate
) : CustomizedMovieRepository {
    override fun updatePriceAndShowTimes(price: Int, showTimes: List<LocalDateTime>, movieId: String): Mono<Void> =
        mongoTemplate
            .update<MongoMovie>()
            .matching(query(where("id").`is`(movieId)))
            .apply(
                Update()
                    .set("price", price)
                    .set("showTimes", showTimes)
            )
            .first()
            .then()

    override fun incAndDecRatings(ratingToInc: Int, ratingToDec: Int?, movieId: String) =
        mongoTemplate
            .update<MongoMovie>()
            .matching(query(where("id").`is`(movieId)))
            .apply(
                Update()
                    .inc("ratings.$ratingToInc", 1)
                    .apply {
                        ratingToDec?.let {
                            inc("ratings.$it", -1)
                        }
                    }
            )
            .first()
            .then()
}
