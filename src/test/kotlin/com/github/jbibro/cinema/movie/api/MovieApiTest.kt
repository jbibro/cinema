package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.data.MongoMovie
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
@Testcontainers
@AutoConfigureRestDocs
internal class MovieApiTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var mongoTemplate: ReactiveMongoTemplate

    @Test
    fun `should return list of movies`() {
        // given some movies
        insertMovies(
            listOf(
                MongoMovie(
                    id = "5b6515a6-a2f4-4844-80ef-a8a097d65e07",
                    title = "Fast and the Furious",
                    imdbId = "tt0232500"
                ),
                MongoMovie(
                    id = "22a60a74-4bf0-4b20-b7f9-0d2cfe136012",
                    title = "2 Fast 2 Furious",
                    imdbId = "tt0322259"
                ),
            )
        )

        // then
        webClient
            .get()
            .uri("/api/movies")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .json(
                """
                 [
                   {"id":"5b6515a6-a2f4-4844-80ef-a8a097d65e07","title":"Fast and the Furious"},
                   {"id":"22a60a74-4bf0-4b20-b7f9-0d2cfe136012","title":"2 Fast 2 Furious"}
                 ]
                """.trimIndent()
            )
            .consumeWith(document("list-movies"))
    }

    @Test
    fun `should return movie details`() {
        // given some movie
        val id = UUID.randomUUID().toString()
        val movie = MongoMovie(
            id = id,
            title = "Fast and the Furious",
            imdbId = "tt0232500",
            ratings = mapOf("5" to 5)
        )
        insertMovies(listOf(movie))

        // and omdb has it in its database
        stubOmdb(movie)

        // then
        webClient
            .get()
            .uri("/api/movies/${movie.id}")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .json(
                """
                    {
                      "id":$id,
                      "title":"Fast and the Furious",
                      "description": "Amazing",
                      "releaseDate": "2001-06-22",
                      "imdbRating": 4.5,
                      "runtime": "PT1H40M"
                    }
                """.trimIndent()
            )
            .consumeWith(document("get-movie"))
    }

    @Test
    fun `should return show times`() {
        // given some movie
        val movieId = UUID.randomUUID().toString()
        val movie = MongoMovie(
            id = movieId,
            title = "Fast and the Furious",
            imdbId = "tt0232500"
        )
        insertMovies(listOf(movie))
        val showTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES)

        // when I update show times
        webClient
            .put()
            .uri("/api/movies/${movie.id}")
            .bodyValue(
                MovieUpdateRequest(
                    price = 10,
                    showTimes = listOf(showTime)
                )
            )
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .consumeWith(document("update-show-times"))

        // then
        webClient
            .get()
            .uri("/api/movies/${movie.id}/show-times")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .json(
                """
                    {
                      "id": $movieId,
                      "showTimes": ["${showTime.format(ISO_DATE_TIME)}"]
                    }
                """.trimIndent()
            )
            .consumeWith(document("get-show-times"))
    }

    private fun stubOmdb(movie: MongoMovie) {
        stubFor(
            get(urlEqualTo("/?apikey=apiKey&i=${movie.imdbId}"))
                .willReturn(
                    okJson(
                        """
                        {
                            "Title": "Fast and the Furious",
                            "Plot": "Amazing",
                            "Released": "22 Jun 2001",
                            "imdbRating": "4.5",
                            "Runtime": "100 min"
                        }      
                        """.trimIndent()
                    )
                )
        )
    }

    private fun insertMovies(movies: List<MongoMovie>) {
        mongoTemplate.insertAll(movies).blockLast()
    }

    companion object {
        @Container
        val mongo: MongoDBContainer = MongoDBContainer("mongo:5.0.5")

        @JvmStatic
        @DynamicPropertySource
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongo.getReplicaSetUrl("cinema") }
        }
    }
}
