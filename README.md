# Spring Security + JWT Authentication

A full-stack authentication system built with Spring Boot and React. Implements JWT-based stateless authentication with refresh token support.

---

## Tech Stack

**Backend**
- Java 22
- Spring Boot 4.0.3
- Spring Security 7
- Spring Data JPA
- MySQL
- JWT (jjwt 0.12.6)

**Frontend**
- React 19
- Vite
- Tailwind CSS
- React Router

---

## Features

- User registration and login
- JWT access token (15 minutes)
- Refresh token (7 days) stored in database
- Role-based authorization (ROLE_USER, ROLE_ADMIN)
- Automatic token refresh on expiration
- Logout invalidates refresh token
- Global exception handling
- Input validation
- CORS configuration

---

## Project Structure

```
SpringSecurity/
├── SpringSecurityBackend/
│   └── src/main/java/com/ayhanekin/SpringSecurityBackend/
│       ├── config/
│       │   └── SecurityConfig.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   └── TestController.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   └── RefreshRequest.java
│       │   └── response/
│       │       └── AuthResponse.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Role.java
│       │   └── RefreshToken.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   └── ErrorResponse.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   └── RefreshTokenRepository.java
│       ├── security/
│       │   ├── JwtService.java
│       │   └── JwtFilter.java
│       └── service/
│           ├── AuthService.java
│           ├── UserDetailsServiceImpl.java
│           └── RefreshTokenService.java
└── SpringSecurityFrontend/
    └── src/
        ├── api.js
        ├── components/
        │   └── ProtectedRoute.jsx
        └── pages/
            ├── Login.jsx
            ├── Home.jsx
            ├── Profile.jsx
            ├── About.jsx
            └── Settings.jsx
```

---


## API Endpoints

### Auth (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login and get tokens |
| POST | `/auth/refresh` | Get new access token |
| POST | `/auth/logout` | Logout and invalidate refresh token |

### Protected

| Method | Endpoint | Role |
|--------|----------|------|
| GET | `/` | Authenticated |
| GET | `/user` | ROLE_USER |
| GET | `/admin` | ROLE_ADMIN |

---

## Request / Response Examples

### Register
```json
POST /auth/register
{
  "username": "john",
  "password": "secret123"
}
```

### Login
```json
POST /auth/login
{
  "username": "john",
  "password": "secret123"
}

Response:
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Refresh Token
```json
POST /auth/refresh
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Logout
```json
POST /auth/logout
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Authenticated Request
```
GET /user
Authorization: Bearer eyJhbGci...
```

---

## How Authentication Works

```
1. User logs in → receives access token (15 min) + refresh token (7 days)
2. Every API request → access token sent in Authorization header
3. Access token expires → frontend automatically calls /auth/refresh
4. New access token issued → original request retried transparently
5. User logs out → refresh token deleted from DB → no new tokens can be issued
```

---

