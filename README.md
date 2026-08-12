# Payment Service — Spring Boot

[![CI](https://github.com/kelsonbrito50/springboot-payment-api/actions/workflows/ci.yml/badge.svg)](https://github.com/kelsonbrito50/springboot-payment-api/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

A Spring Boot service built to explore layered architecture, dependency
injection, and testing at the right level. It models a payment through a simple
lifecycle created, then either completed or failed, with business rules in the
service layer, PostgreSQL behind a repository interface, and a REST API on top.

**Stack:** Java 17 · Spring Boot 4.1 · PostgreSQL 16 · Spring Data JPA · Flyway ·
Maven · JUnit 5 · Mockito · Testcontainers · AssertJ

---

## Running it

Requires JDK 17+ and Docker. The Maven wrapper is included.

```bash
docker compose up -d      # start PostgreSQL
./mvnw spring-boot:run    # start the API on http://localhost:8080
```

Flyway creates the schema on first start. Open **http://localhost:8080** for a
small page that creates payments and walks them through their lifecycle, or use
the API directly:

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 19.99, "currency": "USD"}'
```

There is also a console walkthrough that exercises the same service:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Tests need Docker running but not the compose stack, because Testcontainers
starts its own database:

```bash
./mvnw verify             # tests + coverage report in target/site/jacoco
```

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/payments` | Create a payment, `201` with a `Location` header |
| `GET` | `/api/payments` | List all payments |
| `GET` | `/api/payments/{id}` | Fetch one payment |
| `POST` | `/api/payments/{id}/complete` | `PENDING` → `COMPLETED` |
| `POST` | `/api/payments/{id}/fail` | `PENDING` → `FAILED` |

Errors come back as RFC 9457 `application/problem+json`:

| Status | When |
| --- | --- |
| `400` | Amount not positive, or currency not a valid ISO 4217 code |
| `404` | No payment with that id |
| `409` | Transition attempted on a payment already in a terminal state |

```json
{
  "title": "Invalid state transition",
  "status": 409,
  "detail": "Payment 2171e7c6 is already COMPLETED and cannot become COMPLETED",
  "instance": "/api/payments/2171e7c6/complete"
}
```

## Layout

```
src/main/java/com/bharath/core/
├── CoreApplication.java              entry point
├── DemoRunner.java                   console walkthrough, @Profile("demo")
├── model/
│   ├── Payment.java                  immutable domain record
│   └── PaymentStatus.java            PENDING → COMPLETED | FAILED
├── dao/
│   ├── PaymentDAO.java               persistence boundary
│   ├── JpaPaymentDAO.java            the only class that knows persistence is JPA
│   ├── PaymentJpaRepository.java     Spring Data, package-private
│   ├── PaymentEntity.java            mutable table representation
│   └── PaymentMapper.java            entity to domain translation
├── services/
│   ├── PaymentService.java           business operations
│   ├── PaymentServiceImpl.java       validation and state transitions
│   └── PaymentNotFoundException.java
└── web/
    ├── PaymentController.java        REST endpoints
    ├── CreatePaymentRequest.java     request DTO with Bean Validation
    └── GlobalExceptionHandler.java   exception to status code mapping

src/main/resources/
├── db/migration/V1__create_payments_table.sql
└── static/index.html                 browser UI, no build step
```

## Design notes

**The domain model and the table are separate types.** `Payment` is an immutable
record using `Currency` and a status enum. `PaymentEntity` is a mutable class
because JPA requires a no-arg constructor and field access, and `PaymentMapper`
translates between them. Annotating the domain record with `@Entity` would force
the domain to bend around persistence, and records cannot be entities anyway.

**Only `JpaPaymentDAO` knows persistence is JPA.** `PaymentJpaRepository` is
package-private, so the service layer depends on the `PaymentDAO` interface and
nothing else. Swapping the implementation would not touch a line of business
logic.

**Flyway owns the schema; Hibernate validates against it.** `ddl-auto=validate`
means an entity that has drifted from the migrations fails at startup rather
than at the first query. Letting Hibernate generate the schema would make the
migrations decorative. This caught a real mismatch during development: the
migration declared `CHAR(3)` while the entity mapped `varchar(3)`.

**`open-in-view` is disabled.** The default leaves the persistence context open
for the whole request, which hides N+1 queries behind lazy loading in the view
layer. Off, they surface during development.

**Constructor injection over field injection.** The dependency is `final`, the
object is never observed half-built, and the class can be instantiated directly
in a test with a mock, needing no Spring context.

**Validation is duplicated on purpose.** Bean Validation rejects malformed
requests at the HTTP edge with a useful message, and the service re-checks the
same rules so it stays correct when called from anywhere else, as `DemoRunner`
does.

**A malformed id is a miss, not a crash.** `GET /api/payments/banana` returns
404 rather than 500, because `JpaPaymentDAO` treats an unparseable UUID as
"not found".

**Amounts are `BigDecimal`, stored as `NUMERIC(19,2)`.** Binary floating point
cannot represent most decimal fractions exactly, which is the wrong tradeoff for
money.

## Tests

29 tests across four classes, layered so each runs at the cheapest level that
can still catch its failure:

| Class | Scope | Needs Docker |
| --- | --- | --- |
| `PaymentServiceImplTest` | Business rules, DAO mocked, no Spring context | no |
| `PaymentControllerTest` | `@WebMvcTest` slice: routing, JSON, validation, status codes | no |
| `JpaPaymentDAOTest` | Real Postgres via Testcontainers: schema, mapping, round trips | yes |
| `CoreApplicationTests` | Full context against a real database | yes |

The database tests run against PostgreSQL 16 with the Flyway migrations applied,
not an embedded substitute. H2 would happily accept a schema that Postgres
rejects, which defeats the purpose of testing the mapping at all.

```
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

JaCoCo reports **75% instruction / 88% branch** coverage to
`target/site/jacoco/index.html`. The business classes sit between 88% and 100%.
The overall figure is held down by `DemoRunner`, which only runs under the
`demo` profile, and by `CoreApplication.main`.

## Notes

Boot 4 modularized heavily, which is worth knowing if you are reading the
`pom.xml`:

- Test slices moved out of `spring-boot-test-autoconfigure` into per-technology
  modules, so `@WebMvcTest` needs `spring-boot-webmvc-test` and `@DataJpaTest`
  needs `spring-boot-data-jpa-test`.
- Flyway's auto-configuration moved to `spring-boot-flyway`. Without that
  module, `flyway-core` sits on the classpath but never runs.
- Testcontainers versions are no longer managed, so its BOM is imported
  explicitly. Version 2.x is required to talk to Docker Engine 29.
- Surefire's `argLine` starts with `@{argLine}` to carry JaCoCo's agent through.
  Omitting it silently produces a zero-coverage report.
