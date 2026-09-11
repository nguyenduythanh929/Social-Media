# Social Media Platform — Microservices Architecture

A social media backend built with Java Spring Boot using a microservice architecture. The system supports user authentication, social profiles, posting, real-time chat, notifications, file uploads, and full-text search.

---

## Architecture Overview

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
- **REST** — synchronous calls between services (via Spring Cloud OpenFeign)
- **Kafka** — async event-driven: identity → notification (email on register), post/profile → search (index documents)
- **WebSocket (STOMP)** — real-time chat, client connects directly to chat-service

### Persistence (Polyglot)
| Store | Used By | Reason |
|---|---|---|
| MySQL | identity-service | Relational data: users, roles, permissions, token blacklist |
| Neo4j | profile-service | Graph database for social relationships (friends/followers) |
| MongoDB | post, chat, file, notification services | Flexible document storage |
| Elasticsearch | search-service | Full-text search on users and posts |

---

## Services

### API Gateway — port `8888`
Single entry point for all HTTP traffic. Routes requests to downstream services and validates JWT tokens.

- **Global auth filter:** validates Bearer token by calling the identity-service introspect endpoint
- **Public endpoints** (no auth required):
  - `POST /api/v1/identity/auth/**`
  - `POST /api/v1/identity/users/registration`
  - `POST /api/v1/notification/email/send`
  - `GET  /api/v1/file/media/download/**`
- Returns `{ code: 1407, message: "Unauthenticated" }` with HTTP 401 on invalid/missing token

**Route table:**
| Prefix | Downstream |
|---|---|
| `/api/v1/identity/**` | `localhost:8088` |
| `/api/v1/profile/users/**` | `localhost:8089` |
| `/api/v1/notification/**` | `localhost:8090` |
| `/api/v1/post/**` | `localhost:8091` |
| `/api/v1/file/**` | `localhost:8092` |
| `/api/v1/chat/**` | `localhost:8093` |
| `/api/v1/search/**` | `localhost:8095` |

---

### Identity Service — port `8088`
Handles authentication, authorization, user accounts, roles, and permissions.

**Tech:** Spring Boot, Spring Data JPA, Spring Security, Nimbus JOSE+JWT, Kafka producer, MySQL

**Key APIs:**
```
POST /identity/auth/token          # Login → JWT access token
POST /identity/auth/introspect     # Validate token (called by gateway)
POST /identity/auth/refresh        # Refresh access token
POST /identity/auth/logout         # Invalidate token

POST /identity/users/registration  # Register new user (public)
GET  /identity/users               # List all users (admin)
GET  /identity/users/myInfo        # Get current user info
PUT  /identity/users/{userId}      # Update user
DELETE /identity/users/{userId}    # Delete user

GET/POST/DELETE /identity/permissions
GET/POST/DELETE /identity/roles
```

**JWT:** HMAC-SHA signed, access token TTL 1h, refresh token TTL 24h

**On registration:** publishes a Kafka event → notification-service sends a welcome email. Also calls profile-service to create the user profile record.

---

### Profile Service — port `8089`
Manages user profiles and social graph relationships stored in Neo4j.

**Tech:** Spring Boot, Spring Data Neo4j, Kafka consumer

**Key APIs:**
```
GET  /profile/users/my-profile     # Get authenticated user's profile
PUT  /profile/users/my-profile     # Update profile
PUT  /profile/users/avatar         # Upload avatar (multipart)
GET  /profile/users/{profileId}    # Get profile by ID
POST /profile/users/search         # Search users by criteria
```

---

### Post Service — port `8091`
Manages user posts stored in MongoDB.

**Tech:** Spring Boot, Spring Data MongoDB, Kafka producer

**Key APIs:**
```
POST /post/create                  # Create a post
GET  /post/my-posts?page=1&size=10 # Get authenticated user's posts (paginated)
```

Publishes Kafka events on post creation/update → search-service indexes the document.

---

### File Service — port `8092`
Handles media file uploads and downloads. Stores metadata in MongoDB, files on disk.

**Tech:** Spring Boot, Spring Data MongoDB

**Key APIs:**
```
POST /file/media/upload            # Upload file (multipart, max 5MB)
GET  /file/media/download/{name}   # Download file (public, no auth)
```

Files are stored under the `upload/` directory on the server.

---

### Chat Service — port `8093`
Handles real-time 1-on-1 and group messaging using WebSocket (STOMP) backed by MongoDB.

**Tech:** Spring Boot, Spring Data MongoDB, Spring WebSocket (STOMP), Spring Security (JWT), OpenFeign

**REST APIs:**
```
POST /chat/conversations/create           # Create a conversation
GET  /chat/conversations/my-conversations # List user's conversations
POST /chat/messages/create               # Send a message
GET  /chat/messages?conversationId=...   # Get messages in a conversation
```

**WebSocket:**
- Endpoint: `ws://localhost:8093/chat/ws`
- Auth: JWT sent in STOMP `CONNECT` headers
- Subscribe: `/user/queue/messages` — receives incoming messages in real time
- Message flow: sender calls REST → saved to MongoDB → pushed via STOMP to all participants

---

### Notification Service — port `8090`
Sends email notifications by consuming Kafka events. Uses Brevo (Sendinblue) email API.

**Tech:** Spring Boot, Spring Kafka consumer, Spring Data MongoDB, Brevo API

**Kafka topic:** `notification-delivery`

**REST API:**
```
POST /notification/email/send      # Send email directly (public endpoint)
```

---

### Search Service — port `8095`
Provides full-text search over users and posts using Elasticsearch.

**Tech:** Spring Boot, Spring Data Elasticsearch, Kafka consumer, Spring Security (JWT)

**Kafka:** Consumes events from post-service and profile-service to index documents.

**Key APIs:**
```
GET /search/users?q={keyword}      # Search users
GET /search/posts?q={keyword}      # Search posts
```

---

### Web App (React SPA)
Frontend single-page application.

**Tech:** React, React Router, Material UI, `@stomp/stompjs`

**Pages:** Login, Home (feed), Profile, Chat, Search

**Connects to:**
- API Gateway: `http://localhost:8888/api/v1`
- Chat WebSocket (direct): `ws://localhost:8093/chat/ws`

---

## Tech Stack Summary

| Layer | Technology |
|---|---|
| Language | Java 22 |
| Framework | Spring Boot 4.0.6 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Security | Spring Security, JWT (Nimbus JOSE) |
| ORM | Spring Data JPA (Hibernate), Spring Data MongoDB, Spring Data Neo4j |
| Search | Spring Data Elasticsearch |
| Messaging | Apache Kafka (KRaft mode) |
| Real-time | WebSocket (STOMP) |
| HTTP Client | Spring Cloud OpenFeign, WebClient |
| Build | Maven |
| Frontend | React, Material UI |

---

## Infrastructure

All infrastructure services run on `192.168.0.111`.

| Service | Port | Notes |
|---|---|---|
| MySQL | 3308 | identity-service database |
| MongoDB | 27017 | post, chat, file, notification, search services |
| Neo4j | 7687 | profile-service (social graph) |
| Elasticsearch | 9200 | search-service |
| Kafka | 9094 (external), 9092 (internal) | KRaft mode, 3 partitions |

### Start Kafka with Docker Compose
```bash
docker-compose up -d
```

---

## Local Development Setup

### Prerequisites
- Java 22
- Maven 3.9+
- Node.js 18+
- MySQL 8, MongoDB, Neo4j, Elasticsearch running (see Infrastructure above)
- Docker (for Kafka)

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Start Backend Services
Run each service in order. Identity must start before others since it issues tokens.

```bash
# From each service directory:
./mvnw spring-boot:run
```

Start order recommendation:
1. `identity-service`
2. `profile-service`
3. `post-service`, `file-service`, `notification-service`, `search-service`
4. `chat-service`
5. `api-gateway`

### 3. Start Frontend
```bash
cd web-app
npm install
npm start
```

App will be available at `http://localhost:3000`

---

## Environment Variables

| Variable | Service | Description |
|---|---|---|
| `SENDINBLUE_API_KEY` | notification-service | Brevo email API key |

> **Note:** The JWT signing key and database credentials in `application.yaml` files are for local development only. Replace them with environment variables before deploying to any shared or production environment.

---

## Project Structure

```
social-media/
├── api-gateway/          # Spring Cloud Gateway — routing & auth
├── identity-service/     # Auth, users, roles, permissions (MySQL)
├── profile-service/      # User profiles, social graph (Neo4j)
├── post-service/         # Posts (MongoDB + Kafka)
├── file-service/         # Media upload/download (MongoDB + disk)
├── chat-service/         # Real-time chat (MongoDB + WebSocket)
├── notification-service/ # Email notifications (Kafka consumer + Brevo)
├── search-service/       # Full-text search (Elasticsearch + Kafka)
├── web-app/              # React SPA frontend
├── upload/               # Local media file storage
└── docker-compose.yml    # Kafka infrastructure
```

---

## Author

**Nguyễn Duy Thành** — Java Backend Developer  
📧 tnguyenduy587@gmail.com  
🔗 [github.com/nguyenduythanh929](https://github.com/nguyenduythanh929)
