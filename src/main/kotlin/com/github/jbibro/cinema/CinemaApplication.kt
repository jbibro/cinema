package com.github.jbibro.cinema

import com.github.jbibro.cinema.movie.MovieHandler
import com.github.jbibro.cinema.movie.api.MovieApi
import com.github.jbibro.cinema.movie.infrastructure.OmdbClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import java.time.Clock

@SpringBootApplication
@ConfigurationPropertiesScan
class CinemaApplication

fun main(args: Array<String>) {
    runApplication<CinemaApplication>(*args)
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) {
        beans().initialize(context)
    }
}

fun beans() = beans {
    // movie
    bean {
        MovieHandler(ref(), ref(), ref())
    }
    bean {
        MovieApi(ref()).router()
    }
    bean {
        OmdbClient(ref())
    }
    bean {
        Clock.systemUTC()
    }
}
