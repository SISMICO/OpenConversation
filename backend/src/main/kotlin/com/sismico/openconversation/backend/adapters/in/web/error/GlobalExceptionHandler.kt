package com.sismico.openconversation.backend.adapters.`in`.web.error

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class, ValidationException::class)
    fun handleBadRequest(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "BAD_REQUEST",
            message = ex.message ?: "Invalid request",
            path = request.requestURI,
        )

    @ExceptionHandler(NoSuchElementException::class, NotFoundException::class, NoResourceFoundException::class)
    fun handleNotFound(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.NOT_FOUND,
            code = "NOT_FOUND",
            message = ex.message ?: "Resource not found",
            path = request.requestURI,
        )

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        HandlerMethodValidationException::class,
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleValidationErrors(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val details =
            when (ex) {
                is MethodArgumentNotValidException ->
                    ex.bindingResult.fieldErrors.map {
                        "${it.field}: ${it.defaultMessage ?: "invalid"}"
                    }

                is HandlerMethodValidationException -> ex.allErrors.map { it.defaultMessage ?: "invalid" }
                is MissingServletRequestParameterException -> listOf("${ex.parameterName} is required")
                is MethodArgumentTypeMismatchException -> listOf("${ex.name} has invalid type")
                else -> emptyList()
            }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = "VALIDATION_ERROR",
            message = "Request validation failed",
            details = details,
            path = request.requestURI,
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class, MultipartException::class, PayloadTooLargeException::class)
    fun handlePayloadTooLarge(
        ex: RuntimeException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.PAYLOAD_TOO_LARGE,
            code = "PAYLOAD_TOO_LARGE",
            message = ex.message ?: "Uploaded file exceeds the maximum allowed size",
            path = request.requestURI,
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> =
        buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            details = listOfNotNull(ex.message),
            path = request.requestURI,
        )

    private fun buildResponse(
        status: HttpStatus,
        code: String,
        message: String,
        details: List<String> = emptyList(),
        path: String,
    ): ResponseEntity<ApiErrorResponse> {
        val detailsWithPath =
            if (path.isNotBlank() && code != "NOT_FOUND") {
                listOf("path: $path") + details
            } else {
                details
            }

        return ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    error =
                        ApiError(
                            code = code,
                            message = message,
                            details = detailsWithPath,
                        ),
                ),
            )
    }
}
