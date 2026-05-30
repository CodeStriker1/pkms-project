# Personal Knowledge Management System

MCA major project: **Design and Development of a Personal Knowledge Management System**.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- Thymeleaf
- Bootstrap 5
- Maven

## Features

- Registration, login, logout, BCrypt password hashing
- Dashboard note statistics
- Notes with rich text editing, auto-save, edit, archive, favorite, trash, restore
- Categories and tags with many-to-many note tagging
- Keyword search, tag filtering, relevance ranking
- PDF and TXT export
- Profile update and password change
- Dark/light mode and responsive UI
- Validation, global exception handling, XSS-safe note sanitization
- Seed account and sample data

## Database Setup

Create a MySQL database user or update credentials in `src/main/resources/application.properties`.

Default settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pkms_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

Hibernate creates/updates tables automatically. The full SQL schema is also available in `src/main/resources/schema.sql`.

## Run

```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
```

Open:

```text
http://localhost:8080
```

Seed login:

```text
Email: student@example.com
Password: password123
```

For a quick demo without MySQL:

```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=demo
```

For production, provide environment variables:

```text
DB_URL=jdbc:mysql://your-host:3306/pkms_db
DB_USERNAME=your_user
DB_PASSWORD=your_password
REMEMBER_ME_KEY=a-long-random-secret
APP_SEED_ENABLED=false
```

## Tests

```bash
mvn test
```

## Docker

Build and run the app with MySQL:

```bash
docker compose up --build
```

Open:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/actuator/health
```

Before deploying publicly, change `REMEMBER_ME_KEY`, database passwords, and set `APP_SEED_ENABLED=false`.

## Project Structure

```text
src/main/java/com/mca/pkms
  config
  controller
  dto
  entity
  exception
  repository
  service
src/main/resources
  static/css
  static/js
  templates
  application.properties
  schema.sql
src/test/java/com/mca/pkms
```
