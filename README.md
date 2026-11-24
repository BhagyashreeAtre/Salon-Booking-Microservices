# Salon Booking Microservices

A microservices-based salon booking application developed using Java and Spring Boot. The project is designed to manage salon operations, service offerings, customer bookings, payments, and user authentication through independently organized services.

## Technology Stack

- **Language:** Java
- **Backend:** Spring Boot, Spring Cloud
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Inter-service Communication:** OpenFeign
- **Authentication and Authorization:** Spring Security, Keycloak, JWT
- **Messaging:** RabbitMQ
- **Databases:** MySQL
- **Containerization:** Docker, Docker Compose
- **Build Tool:** Maven

## Architecture

The application follows a microservices architecture in which business capabilities are separated into individual services.

```mermaid
flowchart TD
    Client[Client / API Consumer] --> Gateway[API Gateway :5000]
    Gateway --> Eureka[Eureka Server :8070]

    Eureka --> User[User Service :5001]
    Eureka --> Booking[Booking Service :5005]
    Eureka --> Category[Category Service :5006]
    Eureka --> Payment[Payment Service :5007]
    Eureka --> Salon[Salon Service :5008]
    Eureka --> Offering[Service Offering :5009]

    User --> UserDB[(User MySQL)]
    Booking --> BookingDB[(Booking MySQL)]
    Category --> CategoryDB[(Category MySQL)]
    Payment --> PaymentDB[(Payment MySQL)]
    Salon --> SalonDB[(Salon MySQL)]
    Offering --> ServiceDB[(Service Offering MySQL)]

    User -. JWT authentication .-> Keycloak[Keycloak]
    Booking -. Messaging .-> RabbitMQ[RabbitMQ]
    RabbitMQ -. Events .-> Notifications[Notifications Service]
```

*The diagram represents the intended application integrations. Keycloak and RabbitMQ require separate infrastructure setup; they are not services defined in the current Docker Compose file.*

## Microservices

| Service | Port | Responsibility |
|---|---:|---|
| User Service | 5001 | User management and authentication integration |
| Booking Service | 5005 | Salon booking operations |
| Category Service | 5006 | Salon service categories |
| Payment Service | 5007 | Payment processing |
| Salon Service | 5008 | Salon management |
| Service Offering | 5009 | Salon service offerings |
| Notifications | Separate configuration | Notification-related functionality |
| Review | Separate configuration | Review-related functionality |
| Eureka Server | 8070 | Service discovery |
| API Gateway | 5000 | Centralized API routing |

## Key Features and Integrations

### 1. Microservices Architecture
- Business functionality is organized into independent services.
- Each primary business service has its own MySQL database.
- Services register with the Eureka discovery server.

### 2. API Gateway and Service Discovery
- Spring Cloud Gateway provides a centralized entry point.
- Eureka supports service registration and discovery.

### 3. Authentication and Authorization
- Spring Security is used for application security.
- Keycloak provides identity and access management integration.
- JWT tokens are used for securing protected APIs.
- Application roles include CUSTOMER, SALON_OWNER, and ADMIN.

### 4. Inter-service Communication
- OpenFeign is used for declarative service-to-service HTTP communication.
- RabbitMQ configuration is present in the booking, payment, and notifications services for messaging integration.

### 5. Database Management
- MySQL is used for persistent storage.
- Separate database containers are configured for the primary business services.

## Project Structure

```text
Salon-Booking-Microservices/
|
|-- user-service/
|-- booking-service/
|-- category-service/
|-- payment-service/
|-- salon-service/
|-- service_offering/
|-- notifications/
|-- review/
|
|-- eureka-server/
|-- gateway-server/
|
|-- docker-compose/
|   |-- default/
|       |-- docker-compose.yml
|
|-- README.md
```

## Getting Started

### Prerequisites

Install the following before running the application:

- Java JDK compatible with the project configuration
- Maven
- Docker Desktop
- Docker Compose
- MySQL (if running services outside Docker)
- Keycloak
- RabbitMQ

### Configuration

Application configuration uses environment variables for database, messaging, payment, and identity-provider settings.

Configure the required environment variables before starting the services.

Examples of configuration variable names:

```text
DB_USERNAME
DB_PASSWORD
PAYMENT_DB_URL

RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD

KEYCLOAK_ISSUER_URI
KEYCLOAK_BASE_URL
KEYCLOAK_REALM
KEYCLOAK_CLIENT_ID

RAZORPAY_API_KEY
RAZORPAY_API_SECRET
STRIPE_API_KEY
```

Use your own local values. Never commit passwords, API keys, client secrets, or production credentials to GitHub.

Refer to the individual service application.yml files for the exact configuration required by each service.

### Start Docker Compose Services

From the project root, run:

```bash
docker compose -f docker-compose/default/docker-compose.yml config
```

To start the configured containers:

```bash
docker compose -f docker-compose/default/docker-compose.yml up -d
```

To check container status:

```bash
docker compose -f docker-compose/default/docker-compose.yml ps
```

To view logs:

```bash
docker compose -f docker-compose/default/docker-compose.yml logs -f
```

To stop the containers:

```bash
docker compose -f docker-compose/default/docker-compose.yml down
```

### Important Deployment Notes

The current Docker Compose configuration defines the following:

- Eureka Server
- API Gateway
- User, Booking, Category, Payment, Salon, and Service Offering services
- Five MySQL database containers

Keycloak and RabbitMQ are not defined as services in this Compose configuration. They must be configured and made reachable separately.

In particular, the Booking Service Compose configuration references RabbitMQ using the hostname `salon-rabbitmq`. Ensure that this hostname resolves from the Booking Service container before expecting messaging functionality to work.

The Notifications and Review modules are present in the repository but are not included in the current Compose service list.

## API Documentation

The application services can expose API documentation through Swagger UI when the corresponding Springdoc configuration is enabled.

Typical Springdoc endpoints are:

```text
/swagger-ui/index.html
/v3/api-docs
```

Check the individual service configuration and application startup logs for the actual documentation URL and availability.

## Testing

Unit tests have been executed for selected business services.

| Service | Test Result |
|---|---:|
| Booking Service | 28 tests passed |
| Payment Service | 6 tests passed |

These results cover the reported service-level unit tests.

## Future Improvements

- Improve centralized logging and monitoring.
- Complete automated container orchestration for external dependencies.
- Add comprehensive API documentation.
- Expand automated test coverage.
- Add CI/CD pipeline integration.

## Author

**Bhagyashree Atre**

Java | Spring Boot | Microservices | Spring Cloud

[GitHub Profile](https://github.com/BhagyashreeAtre)
