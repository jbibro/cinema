package com.github.jbibro.cinema.movie.data

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface MovieRepository : ReactiveMongoRepository<MongoMovie, String>
