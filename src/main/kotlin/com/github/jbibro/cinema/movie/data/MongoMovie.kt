package com.github.jbibro.cinema.movie.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("movies")
@TypeAlias("movie")
data class MongoMovie(
    @Id val id: String = UUID.randomUUID().toString(),
    val title: String,
    val imdbId: String
)
