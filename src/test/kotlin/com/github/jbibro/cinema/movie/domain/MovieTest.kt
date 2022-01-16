package com.github.jbibro.cinema.movie.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

internal class MovieTest {

    private val now = LocalDateTime.of(2022, 1, 10, 13, 30)
    private val clock = Clock.fixed(
        now.toInstant(UTC), UTC
    )

    @Test
    fun `should return false if there are no upcoming shows`() {
        // given
        val movie = anyMovie().copy(
            showTimes = listOf(
                now.minusMinutes(1),
                now.minusHours(1),
                now.minusDays(1)
            )
        )

        // expect
        assertFalse(movie.isNowPlaying(clock))
    }

    @Test
    fun `should return true if there are upcoming shows`() {
        // given
        val movie = anyMovie().copy(
            showTimes = listOf(
                now.minusMinutes(1),
                now.minusHours(1),
                now.minusDays(1),
                now.plusHours(1) // <- this one
            )
        )

        // expect
        assertTrue(movie.isNowPlaying(clock))
    }

    @Test
    fun `should return upcoming shows`() {
        // given
        val upcomingShow = now.plusHours(1)
        val movie = anyMovie().copy(
            showTimes = listOf(
                now.minusMinutes(1),
                now.minusHours(1),
                now.minusDays(1),
                upcomingShow // <- this one
            )
        )

        // expect
        assertEquals(listOf(upcomingShow), movie.futureShowTimes(clock))
    }

    private fun anyMovie() = Movie(
        id = "id",
        title = "title",
        imdbId = "imdbId",
        showTimes = emptyList()
    )
}
