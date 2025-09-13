package com.medilynk.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * JwtValidationGatewayFilterFactory is a custom GatewayFilterFactory for Spring Cloud Gateway.
 * It validates JWT tokens on incoming requests by delegating to an external authentication service.
 * If the token is missing or invalid, the request is rejected with HTTP 401 Unauthorized.
 *
 * Key responsibilities:
 * - Intercepts requests at the API Gateway level.
 * - Extracts the Authorization header and checks for a Bearer token.
 * - Calls the /validate endpoint of the auth-service to verify the token's validity.
 * - Allows the request to proceed only if the token is valid.
 *
 * This approach centralizes authentication logic at the gateway, ensuring downstream services receive only validated requests.
 */
@Component // Registers this filter factory as a Spring bean for use in gateway routes
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    // WebClient is used to make HTTP requests to the external auth-service
    private final WebClient webClient;

    /**
     * Constructor initializes the WebClient with the base URL of the auth-service.
     * The URL is injected from application properties using @Value.
     *
     * @param webClientBuilder Spring's reactive WebClient builder
     * @param authServiceUrl   Base URL of the authentication service (e.g., http://localhost:8081)
     */
    public JwtValidationGatewayFilterFactory(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.url}") String authServiceUrl) {
        // Configure WebClient to use the auth-service base URL
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    /**
     * Creates the GatewayFilter that will be applied to incoming requests.
     *
     * Filter logic:
     * 1. Extracts the Authorization header from the request.
     * 2. Checks if the header exists and starts with "Bearer ".
     *    - If not, responds with 401 Unauthorized and terminates the request.
     * 3. If a Bearer token is present, sends a GET request to /validate on the auth-service,
     *    passing the token in the Authorization header.
     * 4. If the token is valid (auth-service returns 2xx), the request proceeds to the next filter/route.
     *    Otherwise, the request is terminated with 401 Unauthorized.
     *
     * @param config Not used in this implementation (can be extended for custom config)
     * @return GatewayFilter instance
     */
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain ) -> {
            // Extract the Authorization header from the incoming request
            String token  = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            // If no token or token does not start with "Bearer ", reject the request
            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            // Call the auth-service /validate endpoint to check token validity
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity() // Only care about the response status, not the body
                    .then(chain.filter((exchange))); // If valid, continue the filter chain
        };
    }
}
