package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.data.MongoMovie
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
@Testcontainers
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
    }

    @Test
    fun `should return movie details`() {
        // given some movie
        val movie = MongoMovie(
            id = "5b6515a6-a2f4-4844-80ef-a8a097d65e01",
            title = "Fast and the Furious",
            imdbId = "tt0232500"
        )
        insertMovies(listOf(movie))

        // and omdb has it in its database
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

        // then
        webClient
            .get()
            .uri("/api/movie/${movie.id}")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .json(
                """
                    {
                      "id":"5b6515a6-a2f4-4844-80ef-a8a097d65e01",
                      "title":"Fast and the Furious",
                      "description": "Amazing",
                      "releaseDate": "2001-06-22",
                      "imdbRating": 4.5,
                      "runtime": "PT1H40M"
                    }
                """.trimIndent()
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
