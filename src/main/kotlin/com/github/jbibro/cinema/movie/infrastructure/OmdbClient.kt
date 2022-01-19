package com.github.jbibro.cinema.movie.infrastructure

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jbibro.cinema.movie.domain.Imdb
import com.github.jbibro.cinema.movie.domain.MovieDetails
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class OmdbClient(
    private val omdbSettings: OmdbSettings
) : Imdb {

    var httpClient = HttpClient
        .create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, omdbSettings.connectionTimeoutMs)
        .doOnConnected { conn ->
            conn.addHandlerLast(ReadTimeoutHandler(omdbSettings.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS))
        }

    private val webClient = WebClient
        .builder()
        .baseUrl(omdbSettings.url)
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .build()

    // todo resilience4j
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
