package com.github.jbibro.cinema.movie.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "omdb")
data class OmdbSettings(
    val url: String,
    val apiKey: String,
    val connectionTimeoutMs: Int = 3000,
    val readTimeoutMs: Int = 1000
)
