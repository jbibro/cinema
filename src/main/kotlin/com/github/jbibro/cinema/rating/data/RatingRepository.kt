package com.github.jbibro.cinema.rating.data

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface RatingRepository : ReactiveMongoRepository<MongoUserRatings, String>
