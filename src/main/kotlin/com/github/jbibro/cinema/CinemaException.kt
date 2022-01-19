package com.github.jbibro.cinema

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.server.ServerResponse

data class CinemaException(val errorCode: ErrorCode, override val message: String? = null) : RuntimeException(message)

enum class ErrorCode(val code: Int, val httpCode: HttpStatus) {
    MOVIE_NOT_FOUND(1, NOT_FOUND),
    NO_UPCOMING_SHOWS(2, NOT_FOUND),
    INVALID_RATING(3, BAD_REQUEST),
}

data class CinemaErrorResponse(
    val error: ErrorCode,
    val errorCode: Int,
    val message: String?
)

fun CinemaException.toServerResponse() =
    ServerResponse
        .status(this.errorCode.httpCode)
        .bodyValue(CinemaErrorResponse(this.errorCode, this.errorCode.code, this.message))

inline fun <reified A> Throwable.asException(): A {
    return this as A
}
