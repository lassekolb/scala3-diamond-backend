# scala3-diamond-backend

[![Scala 3.x](https://img.shields.io/badge/Scala-3.x-red.svg)](https://www.scala-lang.org/)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Motivation & Goals

This repository began as a **personal training project** to master modern Scala 3 and functional programming techniques in a realistic backend setting.

**Primary objectives:**
- Build rigorously testable layers (Domain → Service → Port → Adapter)
- Apply best practices from functional programming (tagless-final, pure domain)
- Deep-dive into the Typelevel ecosystem (Cats Effect, FS2, Skunk, http4s, circe)
- Experiment with real-world dev workflows (Docker, hot-reload, license reports)

---

## Features / Highlights
- Pure FP foundations with Cats & Cats Effect
- PostgreSQL access through Skunk
- Redis cache layer via redis4cats
- REST API built on http4s + circe JSON
- Docker & docker-compose for instant local dev

---

## Architecture

The code follows the Onion / Diamond pattern: pure domain at the core, services orchestrate logic, ports define abstract interfaces, and adapters connect to external systems.

```
+-----------+        Adapter: Skunk, Redis
|  Adapter  | <--- Port (trait) <--- Service <--- Domain (pure)
+-----------+
```

**Ports / Adapters in practice**
- Ports are typeclass-style traits (e.g., `UserRepo[F]`, `Cache[F]`)
- Adapters implement those traits for Postgres, Redis, etc.
- Services and domain remain pure and easily unit-tested
- Tagless-final keeps the effect type (`F[_]`) flexible

---

## Quick Start

**Prerequisites:** JDK 17+, Docker

```bash
git clone https://github.com/lassekolb/scala3-diamond-backend.git
cd scala3-diamond-backend
docker compose up -d     # Postgres + Redis
sbt run                  # HTTP server on :8080
```

Optional Swagger / OpenAPI UI (if enabled): [http://localhost:8080/docs](http://localhost:8080/docs)

---

## Tests & Dev Loop

| Command                   | Purpose                        |
|---------------------------|--------------------------------|
| `sbt test`                | Run all tests (munit)          |
| `sbt ~reStart`            | Hot-reload with sbt-revolver   |
| `sbt scalafmtAll`         | Format codebase                |
| `sbt licenseReportDependency` | Generate third-party license list |

---

## Docker

`docker-compose.yml` spins up the services with sensible defaults:

| Service   | Host / Port         | Credentials (dev only)                |
|-----------|---------------------|---------------------------------------|
| Postgres  | localhost:5432      | user: accepto / pass: accepto / db: accepto |
| Redis     | localhost:6379      | —                                     |

Stop containers with `docker compose down`.

---

## Project Structure

This project is organized as a modular Scala 3 backend, following Onion/Diamond architecture. Here’s an overview of the main folders and their roles:

- `01-core-headers/` — Core types, headers, and shared domain logic (e.g., user/auth types)
- `02-c-json-circe/` — JSON serialization/deserialization using Circe
- `02-c-retries-cats-retry/` — Retry logic with cats-retry
- `02-i-delivery-http-http4s/` — HTTP delivery layer using http4s
- `02-o-auth-jwt-pdi/` — JWT-based authentication (pdi-jwt)
- `02-o-cache-redis-redis4cats/` — Redis cache integration (redis4cats)
- `02-o-client-http-http4s/` — HTTP client logic (http4s)
- `02-o-config-env-ciris/` — Configuration via environment variables (Ciris)
- `02-o-core/` — Core business logic and utilities
- `02-o-core-adapters/` — Adapter implementations for core ports
- `02-o-cryptography-jsr105-api/` — Cryptography utilities (JSR-105 API)
- `02-o-persistence-db-postgres-skunk/` — PostgreSQL persistence using Skunk
- `03-main/` — Main application entry point; wires up all modules and starts the HTTP server
- `modules/core/` — Additional shared core logic and tests
- `meta/` — Data ingestion and integration configs (e.g., Debezium, Druid)
- `project/` — SBT build plugins and settings
- `notes/` — Developer notes and documentation

**Services Used:**
- **PostgreSQL** — Main relational database, managed via Docker Compose
- **Redis** — In-memory cache, managed via Docker Compose
- **http4s** — REST API server and client
- **Skunk** — Functional Postgres driver
- **redis4cats** — Functional Redis client
- **Circe** — JSON serialization/deserialization
- **Ciris** — Type-safe configuration
- **pdi-jwt** — JWT authentication
- **Cats, Cats Effect, FS2** — Functional programming and effect management

All services are configured for local development using Docker Compose. See the `docker-compose.yml` for details on service setup and default credentials.

---

## License

MIT © 2023–2025 Lasse Kolb – see [LICENSE](LICENSE).
Third-party licenses: [NOTICE](NOTICE).
