package com.medilynk.apigateway.exception;

import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JwtValidationException is a global exception handler for JWT validation errors in the API Gateway.
 *
 * This class uses Spring's @RestControllerAdvice to intercept exceptions thrown by controller methods.
 * It specifically handles cases where the authentication service responds with HTTP 401 Unauthorized,
 * typically due to an invalid or missing JWT token in the request.
 *
 * Key responsibilities:
 * - Catch WebClientResponseException.Unauthorized exceptions thrown when the gateway calls the auth-service.
 * - Set the HTTP response status to 401 Unauthorized, signaling to the client that authentication failed.
 * - Complete the response without returning any body content, as is standard for 401 errors.
 *
 * This approach centralizes JWT error handling, ensuring consistent responses for authentication failures
 * across all routes managed by the API Gateway.
 */
@RestControllerAdvice // Enables global exception handling for REST controllers
public class JwtValidationException {
    /**
     * Handles exceptions of type WebClientResponseException.Unauthorized.
     * This exception is thrown when the auth-service returns a 401 status code,
     * indicating that the JWT token provided by the client is invalid or missing.
     *
     * @param serverWebExchange the current server exchange, used to set the response status
     * @return a Mono<Void> that completes the response with 401 Unauthorized
     */
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<Void> handleUnauthorizedException(ServerWebExchange serverWebExchange) {
        // Set the HTTP response status to 401 Unauthorized
        serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        // Complete the response without a body
        return serverWebExchange.getResponse().setComplete();
    }
}
