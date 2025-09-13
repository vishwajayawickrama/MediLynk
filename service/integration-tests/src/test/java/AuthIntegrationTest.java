import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * AuthIntegrationTest is an integration test for the authentication service's login endpoint.

 * This test uses RestAssured to send HTTP requests to the auth-service running at http://localhost:4004.
 * It verifies that a valid login request returns a 200 OK status and a non-null JWT token in the response.

 * Key responsibilities:
 * - Set up the base URI for all HTTP requests in the test class.
 * - Send a POST request to /auth/login with a valid email and password payload.
 * - Assert that the response status is 200 and the token field is present in the response body.
 * - Print the generated token for debugging or further use in other tests.

 * This test ensures that the authentication flow works end-to-end, including request handling, validation,
 * and token generation by the auth-service.
 */
public class AuthIntegrationTest {
    /**
     * Sets up the base URI for RestAssured before any tests are run.
     * This ensures all requests are sent to the correct service endpoint.
     */
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    /**
     * Integration test for the /auth/login endpoint.

     * Steps:
     * 1. Arrange: Prepare a valid login payload with email and password.
     * 2. Act: Send a POST request to /auth/login with the payload.
     * 3. Assert: Verify the response status is 200 OK and the token field is present and not null.

     * If successful, prints the generated JWT token to the console.
     */
    @Test
    public void shouldReturnOkWithValidToken() {
        // Prepare the login payload as a JSON string
        String loginPayload =
                """
                    {
                        "email": "testuser@test.com",
                        "password": "password123"
                    }
                """;

        // Send the POST request and validate the response
        Response response = given()
                .contentType("application/json") // Set content type to JSON
                .body(loginPayload)              // Attach the login payload
                .when()
                .post("/auth/login")             // Send POST request to /auth/login
                .then()
                .statusCode(200)                 // Expect HTTP 200 OK
                .body("token", notNullValue())   // Expect a non-null token in the response body
                .extract()
                .response();

        // Print the generated token for debugging or chaining with other tests
        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        // Prepare the login payload as a JSON string
        String loginPayload =
                """
                    {
                        "email": "invalid_user@test.com",
                        "password": "password123"
                    }
                """;

        // Send the POST request and validate the response
        given()
                .contentType("application/json") // Set content type to JSON
                .body(loginPayload)              // Attach the login payload
                .when()
                .post("/auth/login")             // Send POST request to /auth/login
                .then()
                .statusCode(401);                 // Expect HTTP 200 OK
    }


}
