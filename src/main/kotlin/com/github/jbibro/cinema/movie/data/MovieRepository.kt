package com.github.jbibro.cinema.movie.data

import com.github.jbibro.cinema.movie.infrastructure.CustomizedMovieRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface MovieRepository : ReactiveMongoRepository<MongoMovie, String>, CustomizedMovieRepository
