# 📱 Social Media Platform — Microservices Architecture

A full-stack social media platform built with **Java Spring Boot** (backend) and **React** (frontend) using a microservices architecture. Features real-time chat, user authentication, post creation, file uploads, and full-text search.

**Language Composition:** Java 71.7% | JavaScript 28% | Other 0.3%

---

## 🎯 Quick Start

### Prerequisites
- Java 22
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8, MongoDB, Neo4j, Elasticsearch (see [Infrastructure](#infrastructure))

### Setup & Run

```bash
# 1. Start infrastructure services
docker-compose up -d

# 2. Start backend services (in this order)
cd identity-service && ./mvnw spring-boot:run &
cd profile-service && ./mvnw spring-boot:run &
cd api-gateway && ./mvnw spring-boot:run &
# Start other services in parallel: post-service, file-service, chat-service, notification-service, search-service

# 3. Start frontend
cd web-app
npm install
npm start
```

App will be available at **http://localhost:3000**

---

## 🏗️ Architecture Overview

### System Diagram
```
Client (React SPA)
       │
       ▼
  API Gateway :8888          ← Single entry point, JWT validation
       │
  ┌────┼────────────────────────────────────────┐
  ▼    ▼         ▼          ▼         ▼         ▼
Identity Profile  Post    File      Chat     Search
 :8088   :8089   :8091   :8092     :8093     :8095
  │                │                           ▲
  └──── Kafka ─────┘──── Notification :8090    │
                    └────────────────────────── ┘
```

### Communication Patterns
| Pattern | Usage | Details |
|---------|-------|---------|
| **REST** | Synchronous service-to-service | Spring Cloud OpenFeign |
| **Kafka** | Async event-driven messaging | Email on register, search indexing |
| **WebSocket (STOMP)** | Real-time chat | Direct connection to chat-service |

### Data Storage (Polyglot Persistence)
| Database | Service | Use Case |
|----------|---------|----------|
| **MySQL** | identity-service | Users, roles, permissions, token blacklist |
| **Neo4j** | profile-service | Social graph (friends, followers) |
| **MongoDB** | post, chat, file, notification | Flexible document storage |
| **Elasticsearch** | search-service | Full-text search on users & posts |

---

## 🔧 Services

### 1. **API Gateway** — Port `8888`
Single entry point with JWT validation and request routing.

- **Routes requests** to all downstream services
- **Validates JWT tokens** via introspect endpoint
- **Public endpoints** (no auth required):
  - `POST /api/v1/identity/auth/**`
  - `POST /api/v1/identity/users/registration`
  - `GET /api/v1/file/media/download/**`

**Route Mapping:**
| Prefix | Target Service |
|--------|-----------------|
| `/api/v1/identity/**` | localhost:8088 |
| `/api/v1/profile/users/**` | localhost:8089 |
| `/api/v1/post/**` | localhost:8091 |
| `/api/v1/file/**` | localhost:8092 |
| `/api/v1/chat/**` | localhost:8093 |
| `/api/v1/search/**` | localhost:8095 |

---

### 2. **Identity Service** — Port `8088`
Authentication, authorization, and user account management.

**Stack:** Spring Boot, Spring Security, JWT (Nimbus JOSE), MySQL

**Key Endpoints:**
```
POST   /identity/auth/token          # Login → JWT token
POST   /identity/auth/introspect     # Token validation
POST   /identity/auth/refresh        # Refresh token
POST   /identity/auth/logout         # Logout

POST   /identity/users/registration  # Register (public)
GET    /identity/users/myInfo        # Get current user
PUT    /identity/users/{userId}      # Update user
DELETE /identity/users/{userId}      # Delete user
```

**Features:**
- HMAC-SHA signed JWT tokens
- Access token TTL: 1 hour | Refresh token TTL: 24 hours
- On registration: publishes Kafka event → triggers welcome email & creates user profile

---

### 3. **Profile Service** — Port `8089`
User profiles and social graph management.

**Stack:** Spring Boot, Spring Data Neo4j, Kafka consumer

**Key Endpoints:**
```
GET  /profile/users/my-profile       # Get user's profile
PUT  /profile/users/my-profile       # Update profile
PUT  /profile/users/avatar           # Upload avatar
GET  /profile/users/{profileId}      # Get profile by ID
POST /profile/users/search           # Search users
```

---

### 4. **Post Service** — Port `8091`
User posts creation and retrieval.

**Stack:** Spring Boot, Spring Data MongoDB, Kafka producer

**Key Endpoints:**
```
POST /post/create                    # Create post
GET  /post/my-posts                  # Get user's posts (paginated)
```

- Publishes Kafka events → search-service indexes documents

---

### 5. **File Service** — Port `8092`
Media file upload and download management.

**Stack:** Spring Boot, Spring Data MongoDB

**Key Endpoints:**
```
POST /file/media/upload              # Upload file (max 5MB)
GET  /file/media/download/{name}     # Download file (public)
```

- Files stored in `upload/` directory
- Metadata stored in MongoDB

---

### 6. **Chat Service** — Port `8093`
Real-time messaging with WebSocket support.

**Stack:** Spring Boot, MongoDB, WebSocket (STOMP), OpenFeign

**REST Endpoints:**
```
POST /chat/conversations/create           # Create conversation
GET  /chat/conversations/my-conversations # List conversations
POST /chat/messages/create                # Send message
GET  /chat/messages                       # Get conversation history
```

**WebSocket:**
- **Endpoint:** `ws://localhost:8093/chat/ws`
- **Auth:** JWT in STOMP CONNECT headers
- **Subscribe:** `/user/queue/messages` for real-time messages

---

### 7. **Notification Service** — Port `8090`
Email notifications via Kafka events and Brevo API.

**Stack:** Spring Boot, Spring Kafka, Brevo API

**Key Endpoint:**
```
POST /notification/email/send        # Send email directly (public)
```

---

### 8. **Search Service** — Port `8095`
Full-text search for users and posts.

**Stack:** Spring Boot, Elasticsearch, Kafka consumer

**Key Endpoints:**
```
GET /search/users?q={keyword}        # Search users
GET /search/posts?q={keyword}        # Search posts
```

---

### 9. **Web App** — Port `3000`
React SPA frontend.

**Stack:** React, React Router, Material UI, STOMP.js

**Features:** Login, Home Feed, Profile, Chat, Search

---

## 💻 Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 22, JavaScript/React |
| **Framework** | Spring Boot 4.0.6 |
| **Gateway** | Spring Cloud Gateway (WebFlux) |
| **Security** | Spring Security, JWT |
| **Persistence** | JPA/Hibernate, MongoDB, Neo4j |
| **Search** | Elasticsearch |
| **Messaging** | Apache Kafka (KRaft mode) |
| **Real-time** | WebSocket (STOMP) |
| **HTTP Client** | Spring Cloud OpenFeign, WebClient |
| **Build** | Maven |
| **Frontend** | React, Material UI |

---

## 🚀 Infrastructure

All services run on `192.168.0.111`

| Service | Port | Purpose |
|---------|------|---------|
| **MySQL** | 3308 | identity-service database |
| **MongoDB** | 27017 | post, chat, file, notification, search services |
| **Neo4j** | 7687 | profile-service social graph |
| **Elasticsearch** | 9200 | search-service indexing |
| **Kafka** | 9094 (ext), 9092 (int) | Message broker (KRaft, 3 partitions) |

### Start Infrastructure
```bash
docker-compose up -d
```

---

## 📁 Project Structure

```
social-media/
├── api-gateway/              # Spring Cloud Gateway
├── identity-service/         # Auth & user management (MySQL)
├── profile-service/          # Profiles & social graph (Neo4j)
├── post-service/             # Posts (MongoDB + Kafka)
├── file-service/             # Media management (MongoDB + disk)
├── chat-service/             # Real-time chat (MongoDB + WebSocket)
├── notification-service/     # Email notifications (Kafka + Brevo)
├── search-service/           # Full-text search (Elasticsearch + Kafka)
├── web-app/                  # React frontend
├── upload/                   # Local file storage
└── docker-compose.yml        # Infrastructure config
```

---

## ⚙️ Environment Variables

| Variable | Service | Description |
|----------|---------|-------------|
| `SENDINBLUE_API_KEY` | notification-service | Brevo email API key |

> **⚠️ Security Note:** Development credentials in `application.yaml` are for local use only. Use environment variables for production deployments.

---

## 📋 Service Start Order

1. **identity-service** (required first - issues tokens)
2. **profile-service** (dependency on identity)
3. **post-service**, **file-service**, **notification-service**, **search-service** (parallel)
4. **chat-service**
5. **api-gateway** (last - routes to all services)

---

## 📝 License

This project is part of a microservices learning platform.

---

## 👤 Author

**Nguyễn Duy Thành** — Java Backend Developer  
📧 tnguyenduy587@gmail.com  
🔗 [github.com/nguyenduythanh929](https://github.com/nguyenduythanh929)

---

## 🤝 Support

For issues, questions, or contributions, please open an issue on [GitHub Issues](https://github.com/nguyenduythanh929/Social-Media/issues).