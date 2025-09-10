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

    static {
        // Start MongoDB container before all tests. It is static to ensure it runs once for the class.
        mongoDBContainer.start();
        // Note: Logger calls in static block removed as they may not work properly
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        // Logger call moved to a method that runs after Spring context is loaded
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        logger.info("RestAssured configured with baseURI: {} and port: {}", RestAssured.baseURI, port);
        logger.info("MongoDB Testcontainer running at: {}", mongoDBContainer.getReplicaSetUrl());
    }

    @Test
    void shouldCreatePatient() {
        String requestBody = """
            {
                "firstName": "vishwa",
                "lastName": "Jayawickrama",
                "email": "vishwa@gmail.com",
                "phone": "0123456",
                "dob": "2000-01-01",
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
                .post("/api/patients") // Added leading slash
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("vishwa"))
                .body("lastName", equalTo("Jayawickrama"))
                .body("email", equalTo("vishwa@gmail.com"))
                .body("dob", equalTo("2000-01-01"))
                .body("phone", equalTo("0123456"))
                .body("address", equalTo("colombo"))
                .body("age", equalTo(23))
                .body("status", equalTo("ACTIVE"));
        logger.info("Patient creation test passed and response validated.");
    }

    @Test
    void shouldGetAllPatients() {
        // First create a patient to ensure the database is not empty
        createTestPatient();

        logger.info("Sending GET request to /api/patients");

        RestAssured.given()
                .when()
                .get("/api/patients") // Added leading slash
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", greaterThan(0))
                .body("[0]", hasKey("id"))
                .body("[0]", hasKey("firstName"));
        logger.info("Get all patients test passed and response validated.");
    }

    @Test
    void shouldGetPatientById() {
        // First create a patient to get its ID
        String patientId = createTestPatient();
        logger.info("Created test patient with ID: {}", patientId);

        logger.info("Sending GET request to /api/patients/{}", patientId);

        RestAssured.given()
                .queryParam("id", patientId)
                .when()
                .get("/api/patient") // Added leading slash
                .then()
                .statusCode(200)
                .body("id", equalTo(patientId));

        logger.info("Get patient by ID  test passed and response validated.");
    }

    @Test
    void shouldUpdatePatient() {
        // First create a patient to get its ID
        String patientId = createTestPatient();
        logger.info("Created test patient with ID: {}", patientId);

        String updateRequestBody = """
            {
                "firstName": "UpdatedName",
                "lastName": "UpdatedLastName",
                "email": "UpdatedEmail@test.com",
                "phone": "1112223333",
                "dob": "1995-12-12",
                "address": "Updated Address",
                "age": 28,
                "status": "INACTIVE"
            }
            """;

        logger.info("Sending PUT request to /api/patients/{} with body: {}", patientId, updateRequestBody);
        RestAssured.given()
                .queryParam("id", patientId)
                .contentType(ContentType.JSON)
                .body(updateRequestBody)
                .when()
                .put("/api/patient")
                .then()
                .statusCode(200)
                .body("id", equalTo(patientId))
                .body("firstName", equalTo("UpdatedName"))
                .body("lastName", equalTo("UpdatedLastName"))
                .body("email", equalTo("UpdatedEmail@test.com"))
                .body("dob", equalTo("1995-12-12"))
                .body("phone", equalTo("1112223333"))
                .body("address", equalTo("Updated Address"))
                .body("age", equalTo(28))
                .body("status", equalTo("INACTIVE"));

        logger.info("Update patient test passed and response validated.");
    }

    @Test
    void shouldDeletePatient() {
        // First create a patient to get its ID
        String patientId = createTestPatient();
        logger.info("Created test patient with ID: {}", patientId);

        logger.info("Sending DELETE request to /api/patients/{}", patientId);
        RestAssured.given()
                .queryParam("id", patientId)
                .when()
                .delete("/api/patient")
                .then()
                .statusCode(200);

        // Verify deletion by attempting to fetch the deleted patient
        RestAssured.given()
                .queryParam("id", patientId)
                .when()
                .get("/api/patient")
                .then()
                .statusCode(500); // Assuming the service returns 500 for not found

        logger.info("Delete patient test passed and deletion verified.");
    }
    // Helper method to create a test patient and return its ID
    private String createTestPatient() {
        String requestBody = """
            {
                "firstName": "Test",
                "lastName": "Patient",
                "email": "test@example.com",
                "phone": "0987654321",
                "dob": "1990-05-15",
                "address": "Test Address",
                "age": 33,
                "status": "ACTIVE"
            }
            """;

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}