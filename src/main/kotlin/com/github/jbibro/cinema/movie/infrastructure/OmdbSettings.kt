package com.github.jbibro.cinema.movie.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "omdb")
data class OmdbSettings(
    val url: String,
    val apiKey: String
)
