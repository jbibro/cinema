package com.github.jbibro.cinema.rating.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("ratings")
@TypeAlias("rating")
data class MongoUserRatings(
    @Id val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val ratings: Map<String, Int>
)
