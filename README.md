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
mvn clean spring-boot:run
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

## Tests

```bash
mvn test
```

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
