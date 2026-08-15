# Architecture Decisions

This document records the key design decisions for the e-commerce microservices backend,
made up front so they don't need to be re-derived later — and so they can be explained
clearly in interviews.

## 1. Service Boundaries

| Service | Responsibility | Owns |
|---|---|---|
| Auth | Registration, login, JWT issuance, roles (CUSTOMER, ADMIN) | `auth_db` |
| Product/Catalog | Product CRUD, categories, search/filter/pagination | `product_db` |
| Inventory | Stock levels per product, reserve/release stock | `inventory_db` |
| Order | Cart, checkout, order state machine, saga coordination via events | `order_db` |
| Notification | Sends emails on order events (mock or real SMTP sandbox) | `notification_db` |

**Payment:** not a separate service. A real payment gateway integration isn't the point of
this project, so payment is represented as a `paymentStatus` field on the Order entity
(mocked), rather than building and operating a sixth service for it.

## 2. Sync vs Async — per interaction

| Interaction | Type | Why |
|---|---|---|
| Client → Gateway → Auth (login/register) | Sync REST | Client needs an immediate response (the JWT) |
| Gateway → Product/Inventory (browsing) | Sync REST | Read-heavy, caller needs an immediate response |
| Order → Inventory (stock check, Phase 2 stub) | Sync REST via Feign + circuit breaker | Checkout needs an immediate reserve/fail answer before showing a result |
| Order → Kafka `OrderPlaced` → Inventory (Phase 3) | Async | Order shouldn't block on Inventory processing; decouples the two so a slow/down Inventory doesn't take Order down with it |
| Inventory → Kafka `StockReserved` / `StockFailed` → Order | Async | Same reasoning, reverse direction |
| Order → Kafka `OrderConfirmed` / `OrderFailed` → Notification | Async | Sending an email should never block the checkout response |

## 3. Saga Pattern: Choreography, not Orchestration

Checkout only involves 2-3 steps: reserve stock → confirm/cancel order → notify. Choreography
(each service reacts to events, no central coordinator) is simpler to build and has no single
point of failure. Orchestration would make more sense if the saga grew to 5+ steps with complex
branching logic, but isn't worth the added complexity (a dedicated orchestrator service, more
moving parts to operate) for this scope.

**Failure handling:** if a downstream service is down when an event arrives (e.g. Inventory is
unavailable when `OrderPlaced` fires), the message stays on the Kafka topic — Kafka retains it,
so the consumer picks it up once the service is back, rather than the event being lost. Retry
topics / a dead-letter queue are introduced in Phase 3 for messages that fail processing
repeatedly rather than being unavailable.

## 4. Database-per-service

Each service owns its own logical database — no service ever queries another service's
database directly, only through its API. Locally, all 5 databases run inside a single Postgres
container (`docker-compose.yml`) purely for laptop resource reasons; they are still fully
separate schemas with no cross-database queries. This is what allows each service to migrate to
its own RDS instance later (Phase 4) with zero application code changes.

**Why this matters:** each service's schema can evolve independently (add a column, change a
type) without coordinating a migration across services, and no service can be broken by another
service's schema change. The tradeoff is that a "join" across service data (e.g. order + product
details) has to happen at the API layer, not the database layer.

## 5. Local Dev Tooling

Infra (Postgres now; Kafka and Redis in later phases) runs via Docker Compose so nothing needs
to be installed natively and reinstalled between machines. Application services run locally
against JDK 17 during active development for a fast feedback loop; they get Dockerized
separately starting in Phase 4 for AWS deployment.
