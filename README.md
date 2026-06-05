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

---

## Notes on `spring.jpa.hibernate.ddl-auto`

In Spring Boot, the property:

```properties
spring.jpa.hibernate.ddl-auto=<value>
```

controls how Hibernate handles the **database schema (DDL = Data Definition Language)** when your application starts.

It tells Hibernate whether it should create, update, validate, or ignore database tables based on your JPA entity classes.

---

## Common Values

### 1. `none`

```properties
spring.jpa.hibernate.ddl-auto=none
```

Hibernate does nothing to the database schema.

* No table creation
* No updates
* No validation

Use when:

* Database schema is managed manually
* Using migration tools such as Flyway or Liquibase

---

### 2. `validate`

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate checks that:

* Tables exist
* Columns match entity definitions

If something doesn't match, application startup fails.

Example:

```java
@Entity
public class User {
    @Id
    private Long id;

    private String name;
}
```

If the `name` column is missing in the database:

```
Schema-validation: missing column [name]
```

Use when:

* Production environments
* Database is managed externally
* You want safety checks

---

### 3. `update`

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate automatically updates the schema to match entities.

Example:

Initial entity:

```java
@Entity
public class User {
    @Id
    private Long id;
}
```

Table:

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY
);
```

Later you add:

```java
private String email;
```

On startup Hibernate executes something similar to:

```sql
ALTER TABLE user
ADD COLUMN email VARCHAR(255);
```

Advantages:

* Convenient during development

Disadvantages:

* Can produce unexpected schema changes
* Doesn't handle complex migrations well

Use when:

* Local development
* Prototyping

---

### 4. `create`

```properties
spring.jpa.hibernate.ddl-auto=create
```

Hibernate:

1. Drops existing tables
2. Creates new tables

every time the application starts.

Example:

```text
Startup
 ├─ DROP TABLE user
 └─ CREATE TABLE user
```

All existing data is lost.

Use when:

* Temporary test databases
* Learning/demo projects

Avoid in production.

---

### 5. `create-drop`

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

Hibernate:

* Creates schema at startup
* Drops schema when application shuts down

Useful for:

* Unit tests
* Integration tests
* In-memory databases like H2 Database

---

## Example

Given this entity:

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private Long id;

    private String name;

    private String email;
}
```

With:

```properties
spring.jpa.hibernate.ddl-auto=create
```

Hibernate generates something similar to:

```sql
CREATE TABLE customers (
    id BIGINT NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    PRIMARY KEY (id)
);
```

---

## Typical Environment Configuration

### Local Development

```properties
spring.jpa.hibernate.ddl-auto=update
```

or

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

---

### Testing

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

---

### Production

```properties
spring.jpa.hibernate.ddl-auto=validate
```

or

```properties
spring.jpa.hibernate.ddl-auto=none
```

along with a migration tool such as Flyway or Liquibase.

---

## Recommendation

For modern Spring Boot applications:

| Environment       | Recommended Setting           |
| ----------------- | ----------------------------- |
| Development       | `update`                      |
| Automated Tests   | `create-drop`                 |
| Production        | `validate` or `none`          |
| Schema Migrations | Flyway/Liquibase + `validate` |

A common production setup is:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

and all schema changes are managed through versioned migration scripts in Flyway or Liquibase. This gives predictable, auditable database changes and avoids accidental schema modifications by Hibernate.

