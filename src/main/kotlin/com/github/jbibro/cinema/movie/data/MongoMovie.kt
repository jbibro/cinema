package com.github.jbibro.cinema.movie.data

import com.github.jbibro.cinema.movie.domain.Movie
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.util.UUID

@Document("movies")
@TypeAlias("movie")
data class MongoMovie(
    @Id val id: String = UUID.randomUUID().toString(),
    val title: String,
    val imdbId: String
) {
    fun toDomain() = Movie(id = id, title = title, imdbId = imdbId)
}
