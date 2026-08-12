# Payment Service — Spring Boot

A small Spring Boot application built to explore layered architecture,
dependency injection, and testing at the right level. It models a payment
through a simple lifecycle — created, then either completed or failed — with
business rules in the service layer, storage behind an interface, and a REST
API on top.

**Stack:** Java 17 · Spring Boot 4.1.0 · Maven · JUnit 5 · Mockito · AssertJ

---

## Running it

Requires JDK 17 or newer. The Maven wrapper is included, so no local Maven
install is needed.

```bash
./mvnw test              # run the test suite
./mvnw spring-boot:run   # start the server on http://localhost:8080
```

Then open **http://localhost:8080** for a small page that creates payments and
walks them through their lifecycle, or use the API directly:

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 19.99, "currency": "USD"}'
```

There is also a console walkthrough with no web layer involved:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Storage is in-memory, so data resets on restart.

## API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/payments` | Create a payment — `201` with a `Location` header |
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
  "detail": "Payment 2171e7c6… is already COMPLETED and cannot become COMPLETED",
  "instance": "/api/payments/2171e7c6…/complete"
}
```

## Layout

```
src/main/java/com/bharath/core/
├── CoreApplication.java              entry point
├── DemoRunner.java                   console walkthrough, @Profile("demo")
├── model/
│   ├── Payment.java                  immutable record
│   └── PaymentStatus.java            PENDING → COMPLETED | FAILED
├── dao/
│   ├── PaymentDAO.java               persistence boundary
│   └── InMemoryPaymentDAO.java       ConcurrentHashMap implementation
├── services/
│   ├── PaymentService.java           business operations
│   ├── PaymentServiceImpl.java       validation and state transitions
│   └── PaymentNotFoundException.java
└── web/
    ├── PaymentController.java        REST endpoints
    ├── CreatePaymentRequest.java     request DTO with Bean Validation
    └── GlobalExceptionHandler.java   exception → status code mapping

src/main/resources/static/index.html  browser UI, no build step
```

## Design notes

**Constructor injection over field injection.** `PaymentServiceImpl` takes its
`PaymentDAO` as a constructor argument. The field is `final`, the object is
never observed half-built, and the class can be instantiated directly in a test
with a mock — no Spring context required.

**The service depends on the interface, not the implementation.** `InMemoryPaymentDAO`
is a `ConcurrentHashMap` because persistence isn't what this project is about. A
JDBC or JPA implementation drops into the same slot without the service layer
changing, which is the practical argument for the indirection rather than just
the theoretical one.

**`Payment` is an immutable record.** State changes return a new instance via
`withStatus`, so a payment can be shared across threads without defensive
copying. The terminal-state guard lives on the model, where a caller can't
forget to check it.

**Validation is duplicated on purpose.** Bean Validation rejects malformed
requests at the HTTP edge with a useful message; the service re-checks the same
rules so it stays correct when called from anywhere else — as `DemoRunner` does.

**Controllers hold no business logic.** `PaymentController` translates requests
into service calls; `GlobalExceptionHandler` maps domain exceptions onto status
codes. Neither branches on business state.

**Currency is `java.util.Currency`, not `String`.** Validated once at the
boundary; past that point an invalid currency code is unrepresentable.

**Amounts are `BigDecimal`.** Binary floating point can't represent most decimal
fractions exactly, which is the wrong tradeoff for money.

## Tests

25 tests across four classes:

| Class | Scope |
| --- | --- |
| `PaymentServiceImplTest` | Business rules, DAO mocked — no Spring context |
| `InMemoryPaymentDAOTest` | Store behaviour, plain unit tests |
| `PaymentControllerTest` | `@WebMvcTest` slice — routing, JSON, validation, status codes |
| `CoreApplicationTests` | `@SpringBootTest` — the context starts and beans wire together |

The split is deliberate: rules run in milliseconds without a container, the web
slice loads only MVC, and the full-context test covers only what genuinely needs
a full context.

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Notes

Two Boot 4 details worth knowing if you're reading the `pom.xml`:

- Test slices moved out of `spring-boot-test-autoconfigure` into per-technology
  modules, so `@WebMvcTest` needs `spring-boot-webmvc-test` on the test classpath.
- Mockito is passed to Surefire as a `-javaagent` rather than self-attaching at
  runtime, which the JDK warns about today and will disallow later.
