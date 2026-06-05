# Spring Boot CRUD REST API Demo

A production-ready Spring Boot CRUD REST API demo project showcasing modern best practices, designed for **Java 25** and **Gradle 9**.

This project manages a **Product** catalog resource, exposing RESTful endpoints with structured validation, proper HTTP responses, and robust error formats conforming to **RFC 7807 Problem Details**.

---

## Technical Highlights & Best Practices
- **Java 25 & Spring Boot 4.0.6**: Targets the latest Java LTS runtime using Gradle toolchains.
- **Java Records**: Used for immutable Data Transfer Objects (DTOs) (`ProductCreateRequest`, `ProductUpdateRequest`, `ProductResponse`) to isolate internal database entities from the API contract.
- **Jakarta Validation**: Ensures input data integrity (e.g., SKU formatting, non-negative price, required fields) using annotations like `@NotBlank`, `@Size`, `@Pattern`, and `@DecimalMin`.
- **RFC 7807 Problem Detail**: Captures custom domain exceptions (`ProductNotFoundException`, `ProductAlreadyExistsException`) and validation errors globally, returning structured JSON error payloads with timestamps and details.
- **JPA & H2 In-Memory Database**: Enables quick startup and testing without requiring external infrastructure.
- **Testing Architecture**:
  - **Service Layer Unit Tests**: Verifies core business logic using JUnit 5 and Mockito.
  - **Controller Slice Tests**: Isolated testing of web endpoints and request validation using `@WebMvcTest` and MockMvc.
  - **Integration Tests**: End-to-end flow checks running against an actual database using `@SpringBootTest` and MockMvc.
- **Clean Configuration Isolation**: `@EnableJpaAuditing` is configured in `JpaConfig.java` to prevent JPA-related bean initialization failures in slice tests that do not boot the persistence context.

---

## Project Structure
```text
src/
├── main/
│   ├── java/com/example/cruddemo/
│   │   ├── CrudDemoApplication.java
│   │   ├── config/               # Configuration (JPA, Auditing)
│   │   ├── controller/           # REST Controller
│   │   ├── dto/                  # Input/Output Java Records (DTOs)
│   │   ├── exception/            # Global Exception Handling & Custom Exceptions
│   │   ├── model/                # Database JPA Entity
│   │   ├── repository/           # Spring Data JPA Repository
│   │   └── service/              # Business Logic & DTO mapping
│   └── resources/
│       └── application.yml       # Configuration (H2 console, datasource)
└── test/
    └── java/com/example/cruddemo/
        ├── controller/           # Controller slice tests
        ├── integration/          # E2E Integration tests
        └── service/              # Service unit tests
```

---

## API Endpoints Reference

### 1. Create a Product
- **HTTP Method**: `POST`
- **Path**: `/api/v1/products`
- **Request Body**:
  ```json
  {
    "sku": "PROD-123",
    "name": "Mechanical Keyboard",
    "description": "RGB mechanical keyboard with red switches",
    "price": 89.99,
    "stockQuantity": 150
  }
  ```
- **Response**: `201 Created` with a `Location` header pointing to the created resource (e.g. `/api/v1/products/1`).

---

### 2. Get All Products
- **HTTP Method**: `GET`
- **Path**: `/api/v1/products`
- **Query Parameters**:
  - `search` (Optional): Filter products by name (case-insensitive substring match).
  - `page` (Optional, Default: `0`): Page index.
  - `size` (Optional, Default: `10`): Number of items per page.
  - `sort` (Optional, Default: `name,asc`): Field and direction.
- **Response**: `200 OK` returning a paginated JSON list of product resources.

---

### 3. Get Product By ID
- **HTTP Method**: `GET`
- **Path**: `/api/v1/products/{id}`
- **Response**: `200 OK` if found, `404 Not Found` (with RFC 7807 Problem Detail response) if missing.

---

### 4. Update Product
- **HTTP Method**: `PUT`
- **Path**: `/api/v1/products/{id}`
- **Request Body** (Note: SKU is immutable and cannot be updated):
  ```json
  {
    "name": "Updated Keyboard Name",
    "description": "Updated description",
    "price": 99.99,
    "stockQuantity": 120
  }
  ```
- **Response**: `200 OK` with the updated product details.

---

### 5. Delete Product
- **HTTP Method**: `DELETE`
- **Path**: `/api/v1/products/{id}`
- **Response**: `204 No Content`.

---

## Running the Application

Ensure you have **Java 25** installed.

### Start the Application:
Run the following command in the root folder:
```bash
./gradlew bootRun
```
The application will boot up at `http://localhost:8080`.

### Database Console:
Access the in-memory H2 Console directly from your browser:
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:cruddemo`
- **Username**: `sa`
- **Password**: `password`

---

## Running the Tests

To run the full suite of unit and integration tests, run:
```bash
./gradlew test
```
The test coverage includes:
- Request validation constraint validation.
- Standard RFC 7807 response formatting.
- Location header validation on creation.
- Transaction rollback safety.
