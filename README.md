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

### Running with Docker Compose (Recommended)

1. **Clone and setup:**
```bash
git clone <repository-url>
cd paylite-payment-service

