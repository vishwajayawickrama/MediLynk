package com.medilynk.patientservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.MongoDBContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import static org.hamcrest.Matchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PatientServiceApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(PatientServiceApplicationTests.class);

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @LocalServerPort
    private int port;

    @BeforeEach // Setup RestAssured before each test
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        logger.info("RestAssured configured with baseURI: {} and port: {}", RestAssured.baseURI, port);
    }

    static { // Start MongoDB container before all tests. It is static to ensure it runs once for the class.
        mongoDBContainer.start();
        logger.info("MongoDB Testcontainer started at: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        logger.info("spring.data.mongodb.uri set to: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @Test
    void shouldCreatePatient() {
        String requestBody =
                        """
                            {
                                "firstName": "vishwa",
                                "lastName": "Jayawickrama",
                                "email": "vishwa@gmail.com",
                                "phone": "0123456",
                                "address": "colombo",
                                "age": 23,
                                "status": "ACTIVE"
                            }
                        """;
        logger.info("Sending POST request to /api/patients with body: {}", requestBody);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("api/patients")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("vishwa"))
                .body("lastName", equalTo("Jayawickrama"))
                .body("email", equalTo("vishwa@gmail.com"))
                .body("phone", equalTo("0123456"))
                .body("address", equalTo("colombo"))
                .body("age", equalTo(23))
                .body("status", equalTo("ACTIVE"));
        logger.info("Patient creation test passed and response validated.");
    }
}
