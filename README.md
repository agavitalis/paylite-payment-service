# Paylite Payment Service

A robust, scalable payment processing service built with Spring Boot that handles payment processing, webhook notifications, and idempotent operations.

## Features

### 🏫 Payment Processing
- **Create Payments**: Process new payment requests with proper validation
- **Idempotent Operations**: Prevent duplicate payments using idempotency keys
- **Multiple Currencies**: Support for various currency types
- **Payment Status Tracking**: Real-time payment status updates (PENDING, SUCCEEDED, FAILED)

### 🔔 Webhook System
- **PSP Webhooks**: Receive and process payment status updates from Payment Service Providers
- **HMAC Signature Verification**: Secure webhook validation using HMAC-SHA256
- **Idempotent Webhook Processing**: Safely handle duplicate webhook deliveries
- **Event Auditing**: Complete audit trail of all webhook events

### 🔒 Security & Authentication
- **API Key Authentication**: Secure API access using X-API-Key headers
- **HMAC Webhook Security**: Verified webhook signatures for PSP callbacks
- **Input Validation**: Comprehensive request validation using Jakarta Bean Validation

### 💾 Data Persistence
- **Payment Records**: Complete payment transaction history
- **Idempotency Keys**: Track and prevent duplicate requests
- **Webhook Events**: Audit trail of all webhook processing
- **Unique Constraints**: Prevent duplicate processing

## Architecture

### Core Components

#### Domain Entities:
- **Payment**: Core payment transaction entity with status tracking
- **IdempotencyKey**: Ensures idempotent operations using request hashing
- **WebhookEvent**: Tracks webhook processing and prevents duplicates

#### Service Layer:
- **PaymentService**: Handles payment creation, retrieval, and status updates
- **IdempotencyService**: Manages idempotency key validation and caching
- **WebhookService**: Processes PSP webhooks with signature verification

#### Security:
- **ApiKeyFilter**: JWT-style API key authentication for protected endpoints
- **HMAC Verification**: Cryptographic signature validation for webhooks

### Key Design Patterns

#### Idempotency Pattern:
```java
// Same key + same payload = return cached response
// Same key + different payload = 409 Conflict
// New key = process normally
```

#### Webhook Security:
```java
// HMAC-SHA256 verification of raw request body
// Idempotent processing using event external IDs
// Complete audit trail of all webhook events
```

## API Endpoints

### Payment Operations
| Method | Endpoint | Description | Headers |
|--------|----------|-------------|---------|
| `POST` | `/api/v1/payments` | Create new payment | `Idempotency-Key: <key>` |
| `GET` | `/api/v1/payments/{id}` | Get payment details | `X-API-Key: <key>` |

### Webhook Operations
| Method | Endpoint | Description | Headers |
|--------|----------|-------------|---------|
| `POST` | `/api/v1/webhooks/psp` | PSP webhook callback | `X-PSP-Signature: <hmac>` |

### Public Endpoints
- `GET /actuator/health` - Health check
- `GET /swagger-ui/**` - API documentation
- `GET /v3/api-docs/**` - OpenAPI specification

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Running the Application

You can run the app in 3 ways:

1. Without Docker
2. Using Docker as an image
3. Using Docker Compose (Recommended)

### Running the app (Without Docker)

Install the application dependencies by running this command:

```bash
mvn clean install -U 
```

After installing the dependencies, ensure you configure the `application.properties` file, start the applications using:

```bash
mvn spring-boot:run
```

### Running the app (Using Docker as an image)

Build the application docker image using the command:

```bash
docker build --platform linux/amd64 -f Dockerfile -t vivvaa/paylite-payment-service .
```

### Default(Using Docker Compose Configs) → application.properties
```bash
docker run -p 8080:8080 --platform linux/amd64 vivvaa/paylite-payment-service
```

### Staging(Development) → application.properties + application-local.properties
```bash
docker run -e SPRING_CONFIG_NAME=application-local -p 8080:8080 --platform linux/amd64 vivvaa/paylite-payment-service
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

You can access the app while running via docker use the following URLs:
- Swagger Docs http://localhost:8080/swagger-ui/index.html
- OpenAPI Specs http://localhost:8080/v3/api-docs
- Health Checks http://localhost:8080/actuator/health

## Pushing to Dockerhub and basic Docker debugging 
Build the application docker image using the command if you have not done so:

```bash
docker build --platform linux/amd64 -f Dockerfile -t vivvaa/paylite-payment-service .
```
Push to dockerhub using the command below(replace `vivvaa` with your dockerhub username)
```bash
docker push  vivvaa/paylite-payment-service
```

Verify that your docker container is running using the command:

```bash
docker container ps
```

To view docker container logs use the command:

```bash
docker logs <container_id>
```

To delete a docker container use the command:

```bash
docker stop <container_id>
```

To delete a docker container use the command:

```bash
docker rm <container_id>
```
## Running the Application Unit Tests

```bash

# Run tests
./mvnw test

# Run tests and generate the report:
mvn clean test

# Generate HTML report:
mvn surefire-report:report
```
Generated test output is in target/site/surefire-report.html.

## API Usage Examples

### Create Payment
```bash
curl --location 'http://localhost:8080/api/v1/payments' \
--header 'X-API-Key: client-api-key' \
--header 'Idempotency-Key: 28fb3584-7bf9-4333-9742-864288125815' \
--header 'Content-Type: application/json' \
--header 'Accept: */*' \
--data-raw '{
  "amount": 5000,
  "currency": "NGN",
  "customerEmail": "agavitalisogbonna@gmail.com",
  "reference": "28fb3584-7bf9-4333-9742-864288125815"
}'
```

### Get Payment
```bash
curl --location 'http://localhost:8080/api/v1/payments/pl_c81c4ce4' \
--header 'X-API-Key: client-api-key' \
--header 'Accept: */*'
```

### PSP Webhook
```bash
curl --location 'http://localhost:8080/api/v1/webhooks/psp' \
--header 'X-PSP-Signature: ZEDtOsY4ivPjVJ/qMbF8+CTNCvAaKjXatUz8LskXTNA=' \
--header 'Accept: */*' \
--header 'Content-Type: application/json' \
--data '{"paymentId":"pl_c81c4ce4","event":"payment.succeeded"}'
```

## Security

### API Key Authentication
- All payment endpoints require `X-API-Key` header
- API keys are configured in application properties
- Invalid or missing keys return 401 Unauthorized

### Webhook Security
- PSP webhooks require `X-PSP-Signature` header
- HMAC-SHA256 verification using shared secret
- Invalid signatures return 401 Unauthorized

## Database Schema

### Key Tables
- `payments` - Payment transactions and status
- `idempotency_keys` - Idempotency key tracking
- `webhook_events` - Webhook processing audit

### Unique Constraints
- `idempotency_keys.key` - Prevent duplicate keys
- `webhook_events.event_external_id` - Prevent duplicate webhooks

## Monitoring & Health

### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/v3/api-docs`

## Error Handling

### Common HTTP Status Codes
- `200 OK` - Success
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Invalid API key or webhook signature
- `404 Not Found` - Resource not found
- `409 Conflict` - Idempotency key conflict
- `500 Internal Server Error` - Server error

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/paylite/paymentservice/
│   │   ├── modules/
│   │   │   ├── payment/     # Payment processing
│   │   │   └── webhook/     # Webhook handling
│   │   ├── common/          # Shared utilities
│   │   └── config/          # Configuration
│   └── resources/           # Application properties
└── test/                   # Comprehensive test suite
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions:
- Create an issue in the repository
- Email: agavitalisogbonna@gmail.com

## License

This project is licensed under the [Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0)](https://creativecommons.org/licenses/by-nc/4.0/).
You may **use, share, and adapt** this software for **non-commercial purposes** only. For commercial use, please contact the author for permission.

---

**Author**: [Ogbonna Vitalis](mailto:agavitalisogbonna@gmail.com)  
**Version**: 1.0.0

