package com.github.jbibro.cinema.rating.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document("ratings")
@TypeAlias("rating")
data class MongoUserRatings(
    @Id val id: String = UUID.randomUUID().toString(),
    @Indexed(unique = true) val userId: String,
    val ratings: Map<String, Int>
)
