# Social Media App — Microservices Architecture

A full-stack social media platform built with **Java Spring Boot** microservices and a **React** frontend.
Users can sign up, follow each other, share posts with images, like and comment, chat in real time, and search people and posts.

The whole stack — 8 services, the web app, and MySQL, MongoDB, Neo4j, Elasticsearch and Kafka — starts with **one Docker Compose command**.

---

## Features

| Area | What users can do |
|---|---|
| **Accounts** | Sign up and log in with JWT, welcome email on registration (the API also supports token refresh and server-side logout via a token blacklist) |
| **Profiles** | Edit profile and avatar, view any user's profile with follower / following counts |
| **Social graph** | Follow / unfollow, see followers, following and **friends** (mutual follows) |
| **News feed** | Paginated feed of your own posts and posts from people you follow |
| **Posts** | Create posts with text and up to 4 images, edit and delete your own posts |
| **Interactions** | Like / unlike posts, comment, delete your comments (post owners can moderate comments on their posts) |
| **Chat** | Real-time 1-on-1 and group messaging over WebSocket (STOMP) |
| **Search** | Full-text, typo-tolerant search over users and posts, kept in sync when posts are edited or deleted |

---

## Quick Start (Docker)

**Requirements:** Docker with Docker Compose, and about **8 GB of RAM** free for the containers.

```bash
git clone https://github.com/nguyenduythanh929/Social-Media.git
cd Social-Media
cp .env.example .env        # then set JWT_SIGNER_KEY and SENDINBLUE_API_KEY
docker compose up -d --build
```

Open **http://localhost:3000** (or `http://<server-ip>:3000` when running on another machine).
The first build takes 10–20 minutes while Maven and npm download dependencies.

```bash
docker compose ps                         # check every container is running
docker compose logs -f identity-service   # follow one service's logs
docker compose down                       # stop (add -v to also delete all data)
```

> **Linux hosts:** Elasticsearch needs `sudo sysctl -w vm.max_map_count=262144`
> (persist it in `/etc/sysctl.d/` to survive reboots).

### Configuration (`.env`)

All settings live in `.env`; nothing machine-specific is hard-coded in the repository.

| Variable | Purpose |
|---|---|
| `MYSQL_ROOT_PASSWORD`, `MONGO_PASSWORD`, `NEO4J_PASSWORD` | Database passwords |
| `JWT_SIGNER_KEY` | Secret used to sign JWTs (at least 64 bytes; generate with `openssl rand -base64 64`) |
| `SENDINBLUE_API_KEY` | [Brevo](https://www.brevo.com/) API key for emails |
| `ELASTIC_VERSION` | Elasticsearch image version (matches the Spring Boot client, `9.2.8`) |
| `WEB_PORT` | Port the web app is published on (default `3000`) |
| `KAFKA_EXTERNAL_HOST` | Address Kafka advertises to clients **outside** Docker (see [Local development](#local-development-ide)) |

`.env` is git-ignored; `.env.example` documents every variable.

---

## Architecture

```
                      Browser
                         │  http://<host>:3000
                         ▼
          ┌───────────────────────────────┐
          │  web-app (nginx + React SPA)  │
          └──────┬─────────────────┬──────┘
           /api/*│                 │/chat/ws (WebSocket)
                 ▼                 │
        ┌─────────────────┐        │
        │  API Gateway    │  JWT   │
        │  :8888          │  check │
        └───────┬─────────┘        │
   ┌────────┬───┴────┬────────┬────┼─────────┬──────────┐
   ▼        ▼        ▼        ▼    ▼         ▼          ▼
Identity  Profile   Post     File  Chat    Search   Notification
 :8088     :8089    :8091   :8092  :8093    :8095     :8090
 MySQL     Neo4j    MongoDB MongoDB MongoDB Elastic-  MongoDB
                                            search
   │          │        │                      ▲          ▲
   └──────────┴────────┴──── Kafka ───────────┴──────────┘
```

- **nginx** serves the React app and proxies `/api/*` to the gateway and `/chat/ws` to chat-service, so the browser only ever talks to one origin. The frontend uses relative URLs and works on any host without rebuilding.
- **API Gateway** is the single entry point for REST traffic. A global filter validates every Bearer token through identity-service's introspect endpoint before routing.
- Only `web-app` (3000) and `api-gateway` (8888) are published to the host; the services talk to each other by container name on the Docker network.

### Communication patterns

| Pattern | Used for |
|---|---|
| **REST (OpenFeign)** | Synchronous calls, e.g. identity → profile on sign-up, post → profile for authors and the follow list |
| **Kafka** | Asynchronous events: welcome emails and search indexing |
| **WebSocket (STOMP)** | Real-time chat delivery to each participant's private queue |

**Kafka topics**

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `notification-delivery` | identity | notification | Send welcome email |
| `user-created` | identity | search | Index new user |
| `profile-updated` | profile | search | Re-index user |
| `post-created` / `post-updated` | post | search | Index / re-index post |
| `post-deleted` | post | search | Remove post from index |

### Polyglot persistence

| Store | Service | Why |
|---|---|---|
| **MySQL** | identity | Relational data: users, roles, permissions, token blacklist |
| **Neo4j** | profile | Social graph: `(:user_profile)-[:FOLLOWS]->(:user_profile)` relationships power followers, following and mutual-follow friends |
| **MongoDB** | post, chat, file, notification | Flexible documents: posts, comments, messages, file metadata |
| **Elasticsearch** | search | Full-text, fuzzy search over users and posts |

### Notable design decisions

- **Feed (fan-out on read):** the feed is built per request from the user's follow list (Neo4j) and a paginated MongoDB query, with author details fetched in **one batch call per page**. If profile-service is unavailable the feed degrades to the user's own posts instead of failing.
- **Race-free likes and counters:** likes use MongoDB `$addToSet` / `$pull` and comment counts use `$inc`, so double clicks and concurrent requests never duplicate or lose updates. Post edits update only content fields, so they never overwrite concurrent likes.
- **Follow edges via Cypher:** relationships are written with explicit Cypher queries rather than an entity field, so saving a profile can never rewrite or drop its follow edges.
- **Safe uploads:** file-service only accepts JPEG, PNG, GIF and WebP and serves files with `X-Content-Type-Options: nosniff`, so an uploaded HTML/SVG file cannot run scripts on the site's origin. Posts only accept image URLs issued by our own file-service.
- **Ownership checks:** only a post's author can edit or delete it; comments can be removed by their author or the post owner.

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| **api-gateway** | 8888 | Routing and JWT validation (Spring Cloud Gateway, WebFlux) |
| **identity-service** | 8088 | Registration, login, refresh, logout, roles and permissions |
| **profile-service** | 8089 | Profiles, avatars, follow graph |
| **post-service** | 8091 | Posts, images, feed, likes, comments |
| **file-service** | 8092 | Image upload and download |
| **chat-service** | 8093 | Conversations and real-time messages |
| **notification-service** | 8090 | Emails via Brevo, triggered by Kafka events |
| **search-service** | 8095 | User and post search (Elasticsearch) |
| **web-app** | 3000 | React SPA served by nginx |

All REST endpoints are reached through the gateway at `/api/v1/<service>/...`.

<details>
<summary><b>API reference</b></summary>

**Public endpoints** (no token required): `/identity/auth/**`, `POST /identity/users/registration`, `POST /notification/email/send`, `GET /file/media/download/**`.

**Identity** — `/api/v1/identity`
```
POST   /auth/token                  Log in → access token
POST   /auth/refresh                Refresh access token
POST   /auth/logout                 Invalidate token
POST   /auth/introspect             Validate token (used by the gateway)
POST   /users/registration          Sign up
GET    /users/myInfo                Current user
GET    /users, PUT/DELETE /users/{userId}   Admin only
GET|POST|DELETE /roles, /permissions        Admin only
```

**Profile** — `/api/v1/profile`
```
GET    /users/my-profile            My profile
PUT    /users/my-profile            Update my profile
PUT    /users/avatar                Upload avatar (multipart)
GET    /users/{userId}/detail       Profile + follower/following counts + followedByMe
POST   /users/{userId}/follow       Follow
DELETE /users/{userId}/follow       Unfollow
GET    /users/{userId}/followers    Followers
GET    /users/{userId}/following    Following
GET    /users/{userId}/friends      Mutual follows
```

**Post** — `/api/v1/post`
```
POST   /create                      Create post { content, mediaUrls[] }
GET    /feed?page=&size=            News feed
GET    /my-posts?page=&size=        My posts
GET    /users/{userId}?page=&size=  A user's posts
PUT    /{postId}                    Edit own post
DELETE /{postId}                    Delete own post (and its comments)
POST   /{postId}/like               Like
DELETE /{postId}/like               Unlike
GET    /{postId}/comments           Comments (paginated)
POST   /{postId}/comments           Add comment
DELETE /comments/{commentId}        Delete comment (author or post owner)
```

**File** — `/api/v1/file`
```
POST   /media/upload                Upload image (JPEG/PNG/GIF/WebP, max 5 MB)
GET    /media/download/{fileName}   Download (public)
```

**Chat** — `/api/v1/chat`, WebSocket at `/chat/ws`
```
POST   /conversations/create        Create conversation
GET    /conversations/my-conversations
POST   /messages/create             Send message (saved, then pushed over STOMP)
GET    /messages?conversationId=    Message history
```
WebSocket: send the JWT in the STOMP `CONNECT` headers and subscribe to `/user/queue/messages`.

**Search** — `/api/v1/search`
```
GET    /users?q={keyword}           Search users
GET    /posts?q={keyword}           Search posts
```

</details>

---

## Local development (IDE)

You can run the infrastructure in Docker and the services from IntelliJ for debugging.

1. Start only the infrastructure:
   ```bash
   docker compose up -d mysql mongo neo4j elasticsearch kafka
   ```
2. Set these environment variables in each IntelliJ run configuration:

   | Variable | Value |
   |---|---|
   | `INFRA_HOST` | Host running the containers (`localhost`, or the server's IP / hostname) |
   | `JWT_SIGNER_KEY` | Same key as in `.env` (identity-service and search-service) |
   | `SENDINBLUE_API_KEY` | Your Brevo key (notification-service) |

   If the containers run on **another machine**, also set `KAFKA_EXTERNAL_HOST` in that machine's `.env` to its IP and run `docker compose up -d kafka`, so Kafka advertises an address your IDE can reach.
3. Start the services: identity → profile → post, file, notification, search → chat → api-gateway.
4. Start the frontend (it reads `web-app/.env.development` and talks to `localhost:8888` directly):
   ```bash
   cd web-app
   npm install
   npm start
   ```

**How configuration works:** every `application.yaml` uses `localhost` defaults (for the IDE), written as placeholders such as `${INFRA_HOST:localhost}`. In Docker, `docker-compose.yml` overrides them with environment variables that point at container names (`SPRING_DATASOURCE_URL`, `APP_SERVICES_PROFILE_URL`, …). When adding a new service-to-service call, add both the YAML default and the Compose override.

---

## Testing

```bash
cd identity-service
mvn verify
```

identity-service has unit and web-layer tests (service logic, request validation, context startup) that mock the database, profile-service and Kafka, so they run without any infrastructure. Spotless formats the code during the build.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 22, JavaScript |
| Backend | Spring Boot 4, Spring Security (OAuth2 resource server), Spring Cloud Gateway, OpenFeign |
| Data | Spring Data JPA (MySQL), Spring Data MongoDB, Spring Data Neo4j, Spring Data Elasticsearch |
| Messaging | Apache Kafka (KRaft mode) |
| Real time | WebSocket (STOMP) |
| Frontend | React 18, React Router, Material UI, STOMP.js, Axios |
| Build & quality | Maven, MapStruct, Lombok, Spotless, JaCoCo, JUnit 5, Mockito |
| Infrastructure | Docker, Docker Compose, nginx |

---

## Project structure

```
social-media/
├── api-gateway/            # Spring Cloud Gateway: routing + JWT check
├── identity-service/       # Auth, users, roles (MySQL)
├── profile-service/        # Profiles + follow graph (Neo4j)
├── post-service/           # Posts, feed, likes, comments (MongoDB)
├── file-service/           # Image upload/download (MongoDB metadata + volume)
├── chat-service/           # Real-time chat (MongoDB + WebSocket)
├── notification-service/   # Emails (Kafka consumer + Brevo)
├── search-service/         # Search (Elasticsearch + Kafka consumer)
├── web-app/                # React SPA + nginx config
├── docker-compose.yml      # Full stack: services + infrastructure
└── .env.example            # Configuration template
```

---

## Author

**Nguyễn Duy Thành** — Java Backend Developer
📧 tnguyenduy587@gmail.com
🔗 [github.com/nguyenduythanh929](https://github.com/nguyenduythanh929)
