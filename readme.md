# Features

## Database Integration

* Uses SQLite as the relational database.
* Hibernate auto schema update enabled.
* Database file generated locally as:

```text
sample.db
```

---

## Kafka Integration

Two Kafka topics are configured:

| Topic         | Purpose        |
| ------------- | -------------- |
| payment-topic | Payment events |
| order-topic   | Order events   |

### Supported Event Flow

```text
REST API
   ↓
Kafka Producer
   ↓
Kafka Topic
   ↓
Kafka Consumer
   ↓
Business Logic
```

---

# Prerequisites

Install the following before running the application:

* Java 21+
* Maven 3.9+
* Docker Desktop
* Apache Kafka (or use Docker Compose)

---

# Run Kafka Using Docker

The project contains a `docker-compose.yml` file.

Start Kafka and Zookeeper:

```bash
docker compose up -d
```

Check running containers:

```bash
docker ps
```

Stop containers:

```bash
docker compose down
```

---

# Run the Application

The application will start on:

```text
http://localhost:8080
```

---

# Swagger API Documentation

Open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Open API Docs:

```text
http://localhost:8080/v3/api-docs
```

---

# Troubleshooting

## Docker Compose Not Found

Use:

```bash
docker compose up -d
```

instead of:

```bash
docker-compose up -d
```

---