# <p align="center">PAYLITE PAYMENT SERVICE API</p>

## Description

PayLite is a robust, Dockerized Spring Boot payments microservice that provides idempotent payment processing with 
PSP (Payment Service Provider) webhook integration. Built with production-ready features including idempotency, security, and observability.

## Features

- ✅ **Idempotent Payment Processing** - Prevent duplicate payments with idempotency keys
- ✅ **PSP Webhook Integration** - Secure webhook processing with HMAC verification
- ✅ **API Key Authentication** - Secure access to payment endpoints
- ✅ **MySQL Persistence** - Reliable data storage with Flyway migrations
- ✅ **Dockerized** - Complete containerized setup with Docker Compose
- ✅ **Comprehensive Testing** - Unit and integration tests
- ✅ **Structured Logging** - Correlation IDs and key event tracking
- ✅ **Health Monitoring** - Spring Boot Actuator endpoints

## Technology Stack

- **Java 21** with **Spring Boot 3.5.6**
- **MySQL 8.0** - Primary database
- **Flyway** - Database migrations
- **Docker & Docker Compose** - Containerization
- **Maven** - Dependency management
- **ModelMapper** - Object mapping
- **JUnit 5 & Testcontainers** - Testing

## API Endpoints

### Payment Endpoints (Require API Key)

| Method | Endpoint | Headers | Description |
|--------|----------|---------|-------------|
| `POST` | `/api/v1/payments` | `X-API-Key`, `Idempotency-Key` | Create payment intent |
| `GET` | `/api/v1/payments/{paymentId}` | `X-API-Key` | Get payment status |

### Webhook Endpoint

| Method | Endpoint | Headers | Description |
|--------|----------|---------|-------------|
| `POST` | `/api/v1/webhooks/psp` | `X-PSP-Signature` | Process PSP webhook callbacks |

## Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven 3.6+

## Get started Notes:

- Create a copy of `application.properties` file by from `.application.properties.example` file.
- Update `.application.properties` file with the necessary credentials
- Application Endpoint: `http://localhost:8080`

## Installation

You can run the app in 3 ways:

1. Without Docker
2. Using Docker as an image
3. Using Docker Compose (Recommended)

### Running the app (Without Docker)

Install the application dependencies by running this command:

```bash
mvn clean install -U 
```

After installing the dependencies and configuring `application.properties` file, start the applications using:

```bash
mvn spring-boot:run
```

### Running the app (Using Docker as an image)

Build the application docker image using the command:

```bash
docker build --platform linux/amd64 -f Dockerfile -t vivvaa/chillr-user-service .
```

# Default → application.properties
```bash
docker run -p 8080:8080 --platform linux/amd64 vivvaa/chillr-user-service
```

# Staging → application.properties + application-local.properties
```bash
docker run -e SPRING_CONFIG_NAME=application-local -p 8080:8080 --platform linux/amd64 vivvaa/chillr-user-service
```

# Production → application.properties + application-production.properties
```bash
docker run -e SPRING_CONFIG_NAME=application-production -p 8080:8080 --platform linux/amd64 vivvaa/chillr-user-service
```

To push to dockerhub
```bash
docker push  vivvaa/chillr-user-service
```


Verify that your docker container is running using the command:

```bash
docker container ps
```

To delete a docker container use the command:

```bash
docker stop <container_id>
```

To delete a docker container use the command:

```bash
docker rm <container_id>
```

### Running the app (Using Docker Compose -- Recommended)

Build the application docker image using the command:

```bash
docker compose build
```

Run the app using:

```bash
docker compose up 
```

You can also run in detached mood using:

```bash
docker compose up -d
```

To quit and delete use the command:

```bash
docker compose down
```

The access the app while running via docker use the URL: http://0.0.0.0:8070


