package com.github.jbibro.cinema.movie.api

import com.github.jbibro.cinema.movie.data.MongoMovie
import com.github.jbibro.cinema.rating.api.RatingRequest
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
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8081)
@Testcontainers
@AutoConfigureRestDocs
class RatingApiTest {

    @Autowired
    lateinit var webClient: WebTestClient

    @Autowired
    lateinit var mongoTemplate: ReactiveMongoTemplate

    @Test
    fun `movie rating should be updated`() {
        // given some movie
        val userId = "1"
        val movieId = UUID.randomUUID().toString()
        val rating = 3
        val movie = MongoMovie(
            id = movieId,
            title = "Fast and the Furious",
            imdbId = "tt0232500"
        )
        insertMovies(listOf(movie))

        // and it's in Omdb database
        stubOmdb(movie)

        // when I rate it
        rate(userId, movieId, rating)

        // then
        getMovie(movie.id)
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .consumeWith { println(it.toString()) }
            .jsonPath("$.userRating")
            .isEqualTo(rating)
    }

    @Test
    fun `user can change rating`() {
        // given some movie
        val userId = "1"
        val movieId = UUID.randomUUID().toString()
        val rating = 3
        val newRating = 1
        val movie = MongoMovie(
            id = movieId,
            title = "Fast and the Furious",
            imdbId = "tt0232500"
        )
        insertMovies(listOf(movie))

        // and it's in Omdb database
        stubOmdb(movie)

        // when I rate it
        rate(userId, movieId, rating)
        rate(userId, movieId, newRating)

        // then
        getMovie(movie.id)
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .jsonPath("$.userRating")
            .isEqualTo(newRating)
    }

    @Test
    fun `should return correct average value`() {
        // given some movie
        val firstUser = "1"
        val secondUser = "2"
        val movieId = UUID.randomUUID().toString()
        val firstUserRating = 3
        val secondUserRating = 4
        val movie = MongoMovie(
            id = movieId,
            title = "Fast and the Furious",
            imdbId = "tt0232500"
        )
        insertMovies(listOf(movie))

        // and it's in Omdb database
        stubOmdb(movie)

        // when I rate it
        rate(firstUser, movieId, firstUserRating)
        rate(secondUser, movieId, secondUserRating)

        // then
        getMovie(movie.id)
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .jsonPath("$.userRating")
            .isEqualTo(3.5)
    }

    private fun getMovie(id: String) = webClient
        .get()
        .uri("/api/movie/$id")
        .exchange()

    private fun rate(userId: String, movieId: String, rating: Int) {
        webClient
            .post()
            .uri("/api/users/{id}/movie-ratings", userId)
            .bodyValue(RatingRequest(movieId, rating))
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .consumeWith(document("rate-movie"))
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
