# Health Insurance Claims Management System

A comprehensive microservices-based health insurance claims management system built with Spring Boot, implementing modern architectural patterns and technologies.

## 🏗️ Architecture

This system implements a microservices architecture with the following components:

### Services

1. **BFF Service** (Port 8080) - Backend for Frontend layer
   - API Gateway functionality
   - JWT Authentication
   - Request routing to downstream services
   - Response aggregation

2. **Claim Service** (Port 8081) - Health Insurance Claims Management
   - Claims processing and management
   - Saga orchestration for distributed transactions
   - Event-driven communication via Kafka
   - Redis caching for performance

3. **Policy Service** (Port 8082) - Health Insurance Policy Management
   - Policy lifecycle management
   - Policy validation and verification
   - Saga participation for claim processing
   - Redis caching for performance

## 🚀 Technologies Used

### Core Technologies
- **Java 21** - Modern Java with latest features
- **Spring Boot 4.1.1** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **Spring Kafka** - Event-driven messaging
- **Spring Data Redis** - Caching layer

### Security & Authentication
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Spring Security** - Comprehensive security framework

### Data & Messaging
- **PostgreSQL** - Relational database
- **Redis** - In-memory caching
- **Apache Kafka** - Event streaming platform

### Documentation & Monitoring
- **Swagger/OpenAPI 3.0** - API documentation
- **Spring Boot Actuator** - Application monitoring
- **Prometheus** - Metrics collection

### Testing & Quality
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing with containers
- **Trivy** - Security scanning

### DevOps & Deployment
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **GitHub Actions** - CI/CD pipeline

## 🎯 Key Features

### Saga Pattern Implementation
The system implements the Saga pattern for distributed transactions:
- **Claim Submission Saga**: When a claim is submitted, it triggers a policy verification process
- **Policy Verification**: The policy service validates the policy and responds with verification results
- **Claim Processing**: Based on policy verification, claims are approved or rejected automatically
- **Compensation**: Failed transactions trigger appropriate compensation actions

### Health Insurance Domain Models

#### Policy Types
- **Elementary Health**: Basic health coverage for essential medical needs
- **Complementary Health**: Additional coverage to complement SGK
- **Private Health**: Comprehensive private health insurance
- **Family Health**: Family coverage plans
- **Senior Health**: Specialized coverage for elderly
- **Student Health**: Affordable coverage for students

#### Claim Types
- **Inpatient**: Hospitalization and surgery coverage
- **Outpatient**: Doctor visits and consultations
- **Emergency**: Emergency medical services
- **Checkup**: Preventive health checkups
- **Surgery**: Surgical procedures
- **Medical Imaging**: Diagnostic imaging services
- **Physiotherapy**: Rehabilitation services
- **Dental**: Dental treatments

### Intelligent Caching
- Redis caching for frequently accessed data
- Configurable TTL (Time To Live) for different data types
- Cache invalidation on data updates
- Performance optimization for high-volume operations

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 15+
- Redis 7+
- Apache Kafka 7.5+

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd micro
```

### 2. Start Infrastructure Services
```bash
docker-compose up -d postgres redis zookeeper kafka
```

### 3. Build Services
```bash
# Build claim service
cd claim-service
mvn clean package

# Build policy service
cd ../policy-service
mvn clean package

# Build BFF service
cd ../bff-service
mvn clean package
```

### 4. Start All Services
```bash
cd ..
docker-compose up -d
```

### 5. Verify Services
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## 📚 API Documentation

### Swagger UI
- **BFF Service**: http://localhost:8080/swagger-ui.html
- **Claim Service**: http://localhost:8081/swagger-ui.html
- **Policy Service**: http://localhost:8082/swagger-ui.html

### OpenAPI Docs
- **BFF Service**: http://localhost:8080/v3/api-docs
- **Claim Service**: http://localhost:8081/v3/api-docs
- **Policy Service**: http://localhost:8082/v3/api-docs

## 🔐 Authentication

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### Use JWT Token
```bash
curl -X GET http://localhost:8080/api/bff/policies/1 \
  -H "Authorization: Bearer <your-jwt-token>"
```

## 🧪 Testing

### Run Unit Tests
```bash
# Claim service tests
cd claim-service
mvn test

# Policy service tests
cd ../policy-service
mvn test

# BFF service tests
cd ../bff-service
mvn test
```

### Run Integration Tests
```bash
# Start all services
docker-compose up -d

# Run integration tests
mvn verify
```

## 📊 Monitoring

### Actuator Endpoints
- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Logs
Logs are written to:
- Console output
- `logs/claim-service.log`
- `logs/policy-service.log`
- `logs/bff-service.log`

## 🔄 CI/CD Pipeline

The GitHub Actions workflow includes:
1. **Build & Test**: Compiles and tests each service
2. **Security Scan**: Runs Trivy vulnerability scanner
3. **Docker Build**: Builds and pushes Docker images
4. **Integration Tests**: Runs end-to-end tests
5. **Deployment**: Deploys to production (on main branch)

## 🗂️ Project Structure

```
micro/
├── bff-service/           # Backend for Frontend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── claim-service/         # Claims Management
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── policy-service/        # Policy Management
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── docker-compose.yml
└── README.md
```

## 🎓 Usage Examples

### Create a Policy
```bash
curl -X POST http://localhost:8082/api/policies \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "insuredPersonName": "John Doe",
    "insuredPersonTcNo": "12345678901",
    "policyType": "ELEMENTARY_HEALTH",
    "annualPremium": 5000.00,
    "coverageLimit": 100000.00
  }'
```

### Submit a Claim
```bash
curl -X POST http://localhost:8081/api/claims \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "policyId": "policy-uuid",
    "claimType": "INPATIENT",
    "incidentDate": "2024-01-15T10:00:00",
    "hospitalName": "Test Hospital",
    "diagnosis": "Test Diagnosis",
    "claimedAmount": 10000.00
  }'
```

### Get Claim Statistics
```bash
curl -X GET http://localhost:8081/api/claims/statistics \
  -H "Authorization: Bearer <token>"
```

## 🔧 Configuration

### Application Properties
Each service has its own `application.yaml` with configuration for:
- Database connections
- Redis configuration
- Kafka settings
- JWT settings
- Logging configuration
- Actuator endpoints

### Environment Variables
Override configuration using environment variables:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

## 🚨 Troubleshooting

### Common Issues

1. **Services won't start**: Check if infrastructure services are running
   ```bash
   docker-compose ps
   ```

2. **Kafka connection issues**: Ensure Kafka is fully started
   ```bash
   docker-compose logs kafka
   ```

3. **Database connection errors**: Verify PostgreSQL is accessible
   ```bash
   docker-compose exec postgres psql -U postgres -d insurance_db
   ```

4. **Redis connection errors**: Check Redis status
   ```bash
   docker-compose exec redis redis-cli ping
   ```

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and concise

### Git Workflow
- Create feature branches from `develop`
- Use descriptive commit messages
- Ensure all tests pass before pushing
- Create pull requests for review

### Testing
- Write unit tests for business logic
- Write integration tests for API endpoints
- Maintain test coverage above 80%
- Use Testcontainers for integration tests

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests for your changes
5. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0.

## 👥 Team

- **Development Team**: Insurance Development Team
- **Contact**: dev@insurance.com

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka community for the messaging platform
- Redis community for the caching solution
- All contributors to the open-source libraries used

---

**Note**: This system implements real-world insurance domain concepts including the Turkish insurance market requirements such as SGK integration, elementary health insurance (elementer sağlık), and comprehensive claims management following the insurance damage module (sigortacılık hasar modülü) patterns.