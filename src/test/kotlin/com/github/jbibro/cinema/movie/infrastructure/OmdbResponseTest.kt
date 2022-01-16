package com.github.jbibro.cinema.movie.infrastructure

import com.github.jbibro.cinema.movie.domain.MovieDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

internal class OmdbResponseTest {

    @Test
    fun `should correctly parse omdb format to domain one`() {
        // given
        val omdbFormat = OmdbResponse(
            title = "title",
            description = "description",
            released = "22 Jun 2001",
            imdbRating = "5.6",
            runtime = "120 mins"
        )

        val expectedDomainFormat = MovieDetails(
            title = "title",
            description = "description",
            releaseDate = LocalDate.of(2001, 6, 22),
            runtime = Duration.ofMinutes(120),
            imdbRating = 5.6
        )

        // when
        val domainFormat = omdbFormat.toDomain()

        // then
        assertEquals(expectedDomainFormat, domainFormat)
    }
}
