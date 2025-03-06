# Room Reservation System

A modern web application for managing room reservations.

<img width="1800" alt="Image" src="https://github.com/user-attachments/assets/60c3807e-089b-4cce-9679-b694e18fddb1" />

## Technology Stack

### Backend

- Java 21
- Spring Boot 3.4
- Spring Modulith
- PostgreSQL
- Flyway
- Testcontainers for development environment

### Frontend

- React 18
- TypeScript
- Vite
- PrimeReact UI
- React Router

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose

## Getting Started

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.docker.compose.enabled=true
```

This will:

- Build the Frontend app
- Start PostgreSQL database
- Run database migrations
- Start the Spring Boot application

