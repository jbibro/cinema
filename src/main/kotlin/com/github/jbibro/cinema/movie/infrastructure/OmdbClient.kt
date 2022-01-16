package com.github.jbibro.cinema.movie.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jbibro.cinema.movie.domain.Imdb
import com.github.jbibro.cinema.movie.domain.MovieDetails
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OmdbClient(
    private val omdbSettings: OmdbSettings
) : Imdb {

    private val webClient = WebClient.create(omdbSettings.url)

    override fun details(id: String) =
        webClient
            .get()
            .uri("/?apikey={apiKey}&i={id}", omdbSettings.apiKey, id)
            .retrieve()
            .bodyToMono<OmdbResponse>()
            .map { it.toDomain() }
}

data class OmdbResponse(
    @JsonProperty("Title")
    val title: String,
    @JsonProperty("Plot")
    val description: String,
    @JsonProperty("Released")
    val released: String,
    @JsonProperty("imdbRating")
    val imdbRating: String,
    @JsonProperty("Runtime")
    val runtime: String
) {
    fun toDomain(): MovieDetails {
        return MovieDetails(
            title = title,
            description = description,
            releaseDate = LocalDate.parse(released, DateTimeFormatter.ofPattern("dd MMM uuuu")),
            runtime = Duration.ofMinutes(runtime.split(" ")[0].toLong()),
            imdbRating = imdbRating.toDouble()
        )
    }
}
