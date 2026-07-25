# AgroTrack BackEnd

REST API for AgroTrack, an agricultural fleet management system developed as an undergraduate thesis project (TCC). Companion service for the [AgroTrack Frontend](https://github.com/jotapeor/agroTrackFrontEnd).

## Project Status

Under active development. The features currently implemented are:

- User authentication (JWT-based login)
- Password change on first access
- Collaborator registration by a fleet owner (`PROPRIETARIO` role)

Fleet management features (vehicle registration, tracking, maintenance, trip logging, and related functionality) are planned but not yet implemented. This README will be updated as the scope grows.

## Tech Stack

- Java 21
- Spring Boot 4.1 (Web MVC, Data JPA)
- MySQL
- JWT (jjwt) for stateless authentication
- Maven

## Architecture

Layered structure: `controller` -> `service` -> `repository` -> `model`. Authentication is stateless via JWT; user role and profile data are extracted directly from the token payload via `TokenService`.

Two user profiles exist today: `PROPRIETARIO` (fleet owner, can register collaborators) and a standard collaborator profile.

## API Reference

### Authentication — `/api/autenticar`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/logar` | Authenticates a user and returns a JWT |
| GET | `/verificar-email` | Checks if an email is already registered |
| POST | `/alterar-senha` | Changes the authenticated user's password (min. 6 characters) |

### Owner — `/api/proprietario`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/registrar-colaborador` | Registers a new collaborator (owner only) |

## Data Model

**Usuario** — id_usuario, nome, email, senha (hashed), perfil, ativo, primeiro_acesso, data_criacao.

## Requirements

- JDK 21+
- Maven 3.9+
- MySQL 8

## Setup

1. Create the database and import the schema:
   ```bash
   mysql -u root -p < agrotrack_db.sql
   ```
2. Set the required environment variables (see below).
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The API starts on `http://localhost:8080`.

## Environment Variables

| Variable | Description | Required |
|----------|--------------|----------|
| `JWT_SECRET` | Secret key used to sign JWTs | Yes |
| `SPRING_DATASOURCE_URL` | JDBC connection string | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes |

## Related Project

[agroTrackFrontEnd](https://github.com/jotapeor/agroTrackFrontEnd) — Thymeleaf client consuming this API.
