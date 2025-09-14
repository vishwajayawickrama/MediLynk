# MediLynk

MediLynk is a modular healthcare platform built with a microservices architecture using Java, Spring Boot, gRPC, Kafka, and AWS CDK. It provides scalable, secure, and extensible services for patient management, billing, analytics, authentication, and more.

---

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Services](#services)
- [Infrastructure](#infrastructure)
- [API Examples](#api-examples)
- [Development & Build](#development--build)
- [Testing](#testing)
- [Contributing](#contributing)

---

## Architecture Overview

```
								+-------------------+
								|   API Gateway     |
								+-------------------+
												 |
	 +---------------------+---------------------+
	 |           |             |           |
	 v           v             v           v
Auth   Patient Service  Billing Service  Analytics
Service                                    Service
```

- **API Gateway**: Central entry point for all client requests (Spring Cloud Gateway).
- **Auth Service**: Handles authentication and authorization.
- **Patient Service**: Manages patient records and CRUD operations.
- **Billing Service**: Handles billing accounts and transactions (gRPC enabled).
- **Analytics Service**: Processes and analyzes healthcare data (Kafka integration).
- **Infrastructure**: AWS CDK for cloud resources, LocalStack for local dev.
- **Integration Tests**: REST-assured and JUnit for end-to-end testing.

---

## Services

### 1. API Gateway
- **Path**: `service/api-gateway`
- **Tech**: Spring Cloud Gateway
- **Purpose**: Routes and secures requests to backend services.

### 2. Auth Service
- **Path**: `service/auth-service`
- **Tech**: Spring Boot
- **Purpose**: User authentication, JWT, and validation.

### 3. Patient Service
- **Path**: `service/patient-service`
- **Tech**: Spring Boot, JPA, gRPC, Protobuf
- **Purpose**: CRUD for patient data, gRPC endpoints.

### 4. Billing Service
- **Path**: `service/billing-service`
- **Tech**: Spring Boot, gRPC, Protobuf, Lombok
- **Purpose**: Billing account management, gRPC APIs.

### 5. Analytics Service
- **Path**: `service/analytics-service`
- **Tech**: Spring Boot, Kafka, Protobuf
- **Purpose**: Consumes Kafka events, analytics processing.

### 6. Infrastructure
- **Path**: `service/infrastructure`
- **Tech**: AWS CDK (Java), LocalStack
- **Purpose**: Infrastructure as code, local cloud emulation.

### 7. Integration Tests
- **Path**: `service/integration-tests`
- **Tech**: JUnit, REST-assured
- **Purpose**: End-to-end and contract testing.

---

## API Examples

### Auth Service
**Login:**
```http
POST /auth/login
{
	"username": "user",
	"password": "pass"
}
```

**Validate Token:**
```http
POST /auth/validate
Authorization: Bearer <token>
```

### Patient Service
**Create Patient:**
```http
POST /patients
{
	"name": "John Doe",
	"dob": "1990-01-01",
	"gender": "M"
}
```

**Get Patients:**
```http
GET /patients
```

### Billing Service (gRPC)
**Create Billing Account:**
```http
POST /grpc/billing/createBillingAccount
{
	"patientId": "123",
	"plan": "premium"
}
```

---

## Development & Build

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker (for LocalStack)
- Node.js (for AWS CDK CLI)

### Build All Services
```sh
cd service
mvn clean install
```

### Run Locally
Each service can be run independently:
```sh
cd service/<service-name>
./mvnw spring-boot:run
```

### Infrastructure (LocalStack)
```sh
cd service/infrastructure
sh localstack-deploy.sh
```

---

## Testing

### Integration Tests
```sh
cd service/integration-tests
mvn test
```

---

## Contributing

1. Fork the repo and create a feature branch.
2. Commit your changes with clear messages.
3. Open a pull request.

---

## License

This project is licensed under the MIT License.