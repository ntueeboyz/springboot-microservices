# Spring Boot 3.0 Microservices (Java 17)

A realistic microservices starter with:

- **Discovery Service (`discovery-service`)**
  - Eureka server for service registration & discovery.
- **API Gateway (`api-gateway`)**
  - Spring Cloud Gateway for routing requests to backend services.
- **Employee Service (`employee-service`)**
  - CRUD + search APIs for employees.
- **Department Service (`department-service`)**
  - CRUD APIs for departments.
- **Config Server (`config-server`)**
  - Centralized configuration management using Spring Cloud Config.
- **Postgres**
  - Shared database server with separate schemas per service (`employee`, `department`).
- **ELK Stack**
  - Centralized logging: Elasticsearch, Logstash, Kibana.

## H
### Prerequisites
- Docker

### Start the Containers in Once
```bash
# 1) Build the images
docker compose build --no-cache

# 2) Start the docker container
docker compose up
```

### Running separately
0. **Build the images**
```bash
docker compose build --no-cache
```
1. **Start Postgres**
```bash
docker compose up postgres
```
2. **Start Config Server**
```bash
docker compose up config-server
```
3. **Start Discovery**
```bash
docker compose up discovery-service
```
4. **Start API Gateway**
```bash
docker compose up api-gateway
```
5. **Start Backend Services**
```bash
docker compose up department-service employee-service
```
6. **Start ELK Stack**
```bash
docker compose up elasticsearch logstash kibana
```

## Gateway Routes
Eureka dashboard: http://localhost:8761

Gateway will expose routes:
- `GET http://localhost:8080/departments` (rewritten to `DEPARTMENT-SERVICE /api/v1/departments`)
- `GET http://localhost:8080/employees` (rewritten to `EMPLOYEE-SERVICE /api/v1/employees`)

OpenAPI UIs (service level):
- Employee: http://localhost:8081/swagger-ui.html
- Department: http://localhost:8082/swagger-ui.html

API:
- GET `/employees` → Employee Service `/api/v1/employees`
- GET `/employees/{id}` → Employee Service `/api/v1/employees/{id}`
- GET `/departments` → Department Service `/api/v1/departments`
- GET `/departments/{id}` → Department Service `/api/v1/departments/{id}`

## Current Service Flow
### Diagram
```
                           (clients)
                       Postman / curl
                               │  
                               ▼
+----------------------+   service discovery   +-------------------------+
|   API Gateway (8080) |──────────────────────►|  Eureka (8761)         |
|                      |◄──────────────────────|  discovery-service      |
+----------------------+                       +-------------------------+
          │   routes /employees/**, /departments/**
          │
          ├──────────────► Employee Service (8081) ──────────┐
          │                    /api/v1/employees             │
          │                                                  │JPA
          └──────────────► Department Service (8082) ────────┤
                               /api/v1/departments           │
                                                              ▼
                                                      +---------------+
                                                      |  Postgres     |
                                                      |  (5432)       |
                                                      |  schemas:     |
                                                      |   - employee  |
                                                      |   - department|
                                                      +---------------+

   (config)
   +-------------------------+
   | Config Server (8888)    |
   | Spring Cloud Config     |
   |  - shared defaults      |
   |  - per-service yml      |
   |  - docker overrides     |
   +-------------------------+

   (observability: centralized logs)
   ┌───────────────────────────────────────────────────────────────────────────┐
   │ Each service has logback-spring.xml with a Logstash TCP appender         │
   │ → sends JSON logs to Logstash (port 5044)                                 │
   └───────────────────────────────────────────────────────────────────────────┘

 Employee (8081)  ─┐
 Department (8082) ├─ JSON logs ───────────────────────────────► Logstash (5044)
 API Gateway (8080)│                                              │
 Discovery (8761)  │                                              │ bulk index
 Config Server     │                                              ▼
 (8888)            └────────────────────────────────────► Elasticsearch (9200)
                                                                  │
                                                                  │ queries & dashboards
                                                                  ▼
                                                             Kibana (5601)


```
### What travels where
**Request path (sync)**

1. Client calls API Gateway (8080) at `/employees/**` or `/departments/**`.
2. Gateway uses Eureka to resolve healthy service instances and forwards the request.
3. Employee or Department service handles the request, talks to Postgres, returns JSON.
4. Gateway returns the response to the client.

**Configuration**  

On startup, every service pulls its config from Config Server (8888) using config-repo:
- shared defaults (application.yml)
- per-service (employee-service.yml, department-service.yml, etc.)
- docker overrides (*-docker.yml)

**Centralized logging (ELK)**  

- Each service logs to console and to Logstash (5044) using the Logstash Logback encoder.
- Logstash parses and forwards logs to Elasticsearch (9200).
- Kibana (5601) lets you search logs.

## What services I need in the future ?
1. Load Balancer
- Where: In front of API Gateway.
- Why: Distribute client traffic across multiple Gateway instances for scalability & high availability.
2. Object Storage
- Where: Used by Employee or Department services for file uploads (like photos).
- Why: Externalize file storage, reduce DB bloat.
3. Redis
- Where: Sidecar to Employee and Department services.
- Why: Cache frequently accessed queries to reduce DB load.

**Relative Position**
```
                +-------------------+
                |   Load Balancer   |       <------ Load Balancer
                +-------------------+
                          |
                          v
                  +---------------+
                  |  API Gateway  |
                  +---------------+
                      |         |
                      v         v
             +------------+   +-------------+
             | Employee   |   | Department  |
             | Service    |   | Service     |
             +------------+   +-------------+
                   |                |
                   |                |
                   v                v
           +---------------+   +---------------+
           |  Redis Cache  |   |  Redis Cache  |    <----- Redis
           +---------------+   +---------------+
                   \                /
                    \              /
                     v            v
                    +---------------+
                    |   Postgres    |
                    +---------------+
                           |
                           v
                     +-----------+
                     |    S3     |   <------- Object Storage
                     |   MinIO   |
                     +-----------+
```

## Notes
- Each service uses **Flyway** and its own schema (`employee`, `department`) with separate history tables.
- The `employee-service` uses **OpenFeign** to enrich employees with department details.
- Health endpoints: `/actuator/health`
- Ports:
  - Discovery: 8761
  - Gateway: 8080
  - Employee: 8081
  - Department: 8082
  - Config Server: 8888
  - Postgres: 5432
  - Logstash: 5044
  - Elasticsearch: 9200
  - Kibana UI: 5601
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
