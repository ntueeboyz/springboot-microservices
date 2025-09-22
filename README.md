# Spring Boot 3.0 Microservices (Java 17)

A realistic microservices starter with:

- **Discovery**: Eureka server (`discovery-service`)
- **API Gateway**: Spring Cloud Gateway (`api-gateway`)
- **Services**: Employee + Department (each with Postgres + Flyway)
- **DB**: Postgres connection is shared, with **separate schemas** per service
- **Migrations**: Flyway (`classpath:/db/migration`)

# Run on the Docker
### Prerequisites

- Docker

### Start the Containers
```bash
# 1) Build the images
docker compose build --no-cache

# 2) Start the docker container
docker compose up
```

# Run on the Local Machine
### Prerequisites

- Java 17
- Maven 3.8+
- Postgres running locally and accessible at (you can set it up by yourself):
  - `jdbc:postgresql://localhost:5432/postgres`
  - username: `postgres`
  - password: `123456!`

> Adjust credentials in each service's `application.yml` if yours differ.

### Order to Run (separate terminals)

```bash
# 1)Start the Config Server:
mvn -pl config-server spring-boot:run

# 2) Start discovery
mvn -pl discovery-service spring-boot:run

# 3) Start gateway
mvn -pl api-gateway spring-boot:run

# 4) Start services (can be in any order after discovery is up)
mvn -pl department-service spring-boot:run
mvn -pl employee-service spring-boot:run
```

Eureka dashboard: http://localhost:8761

Gateway will expose routes:

- `GET http://localhost:8080/departments` (rewritten to `DEPARTMENT-SERVICE /api/v1/departments`)
- `GET http://localhost:8080/employees` (rewritten to `EMPLOYEE-SERVICE /api/v1/employees`)

OpenAPI UIs (service level):

- Employee: http://localhost:8081/swagger-ui.html
- Department: http://localhost:8082/swagger-ui.html

## Notes

- Each service uses **Flyway** and its own schema (`employee`, `department`) with separate history tables.
- The `employee-service` uses **OpenFeign** to enrich employees with department details.
- Health endpoints: `/actuator/health`
- Default ports:
  - Discovery: 8761
  - Gateway: 8080
  - Employee: 8081
  - Department: 8082
- After you start the services, please make sure you are able to execute the following code:
  - Employees Service
    - curl -s http://localhost:8080/employees
    - curl -s http://localhost:8080/employees/1
    - curl -s -X POST http://localhost:8080/employees \
      -H "Content-Type: application/json" \
      -d '{ "firstName": "Dina", "lastName": "Khan", "email": "dina@example.com", "departmentId": 1 }'
  - Department Service
    - curl -s http://localhost:8080/departments
    - curl -s http://localhost:8080/departments/1
    - curl -s -X POST http://localhost:8080/departments/ \
      -H "Content-Type: application/json" \
      -d '{ "name": "Finance", "description": "Money things" }'
