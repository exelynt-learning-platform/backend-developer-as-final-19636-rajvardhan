# Resource Booking System

Spring Boot 3 + Java 17 + Spring Security + JWT + MySQL.

## Requirements

- Java 17+
- Spring Boot 3+
- MySQL

## Environment variables

```bash
export DB_URL=jdbc:mysql://localhost:3306/resource_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
export DB_USERNAME=
export DB_PASSWORD=
export JWT_SECRET="replace-with-a-long-random-secret"
export JWT_EXPIRATION_MS=3600000
export ADMIN_USERNAME=Admin
export ADMIN_PASSWORD=Admin@123
export USER_USERNAME=User
export USER_PASSWORD=User@123
```

Swagger:

`http://localhost:8080/swagger-ui.html`

## Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "User",
  "password": "User@123"
}
```

Use the returned token:

```http
Authorization: Bearer <JWT>
```

## Resources

USER and ADMIN can read resources.

Only ADMIN can create, update and delete resources.

```http
GET /resources
GET /resources/{id}

POST /resources
PUT /resources/{id}
DELETE /resources/{id}
```

## Reservations

USER can create reservations and view only their own reservations.

ADMIN can view, create, update, and delete all reservations.

```http
POST /reservations
GET /reservations
GET /reservations/{id}

PUT /reservations/{id}
PATCH /reservations/{id}/status
DELETE /reservations/{id}
```

Reservation search supports:

```text
status=PENDING|CONFIRMED|CANCELLED
minPrice=100
maxPrice=1000
page=0
size=20
sort=price,desc
```

Example:

```http
GET /reservations?status=CONFIRMED&minPrice=100&maxPrice=1000&page=0&size=10&sort=price,desc
```

## Reservation request

The USER ID is intentionally not accepted from the request. It is derived from the authenticated JWT.

```json
{
  "resourceId": 1,
  "startTime": "2026-09-01T10:00:00",
  "endTime": "2026-09-01T12:00:00"
}
```

## Security

- Stateless JWT authentication
- BCrypt password hashing
- Role-based authorization
- USER ownership enforcement
- No user ID accepted for USER reservation creation
- Protected endpoints by default
- Validation and centralized error handling
