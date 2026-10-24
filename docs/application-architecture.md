# Razorpay Clone: Application Flow and Design

## 1. Overview

This project is a Spring Boot payment-gateway application that models the core merchant-facing parts of a payment platform:

- Merchant registration and login
- JWT-based dashboard authentication
- API-key-based payment API authentication
- Order creation
- Payment initiation through multiple payment methods
- Card tokenization and encrypted card storage
- Authorization and capture state transitions
- A scheduled bank-callback simulator
- Domain entities for refunds, settlements, webhooks, and dead-letter events

The application is currently a modular monolith: all modules run in one Spring Boot process and share one relational database.

## 2. High-level architecture

```text
                          +----------------------+
                          |  Merchant Dashboard  |
                          |  JWT authenticated   |
                          +----------+-----------+
                                     |
                                     v
+----------------+       +----------+-----------+       +------------------+
| Payment Client |------>| Spring Controllers   |------>| Service Layer    |
| API-key auth   |       | REST boundary        |       | business rules   |
+----------------+       +----------+-----------+       +--------+---------+
                                     |                          |
                                     |                          v
                                     |                 +--------+---------+
                                     |                 | State Machine    |
                                     |                 | transition logs  |
                                     |                 +--------+---------+
                                     |                          |
                                     v                          v
                          +----------+-----------+       +------+-----------+
                          | Strategy / Adapter  |------>| PostgreSQL       |
                          | payment methods     |       | JPA entities     |
                          +----------+-----------+       +------------------+
                                     |
                     +---------------+----------------+
                     |                                |
                     v                                v
             +-------+--------+              +--------+---------+
             | Card Vault     |              | Simulated Bank   |
             | encrypt/token  |              | scheduled        |
             +----------------+              | callback         |
                                               +------------------+
```

## 3. Application bootstrap

`RazorpaycloneApplication` starts the Spring Boot application and enables:

- `@SpringBootApplication`: component scanning, auto-configuration, and configuration support.
- `@EnableScheduling`: activates `BankCallbackSimulator` polling.
- `@EnableJpaAuditing`: populates audit fields such as `createdAt`, `updatedAt`, `createdBy`, and `updatedBy` on entities extending `BaseAuditableEntity`.

Configuration is loaded from `src/main/resources/application.yaml`. Database connectivity uses `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Vault encryption uses `DEK_ENCRYPTION_KEY`, and JWT signing uses `SECRET_KEY`.

## 4. Common and platform layer

### 4.1 Common entities

`BaseAuditableEntity` is the shared JPA superclass for timestamps and audit users.

`Money` is an embeddable value object containing:

- `amountUnits`: integer amount in the smallest currency unit
- `currency`: currency code

Using integer units avoids floating-point money calculations.

### 4.2 Enums

The common enums model business states and roles, including:

- `MerchantStatus`: merchant onboarding state
- `OrderStatus`: order lifecycle state
- `PaymentMethod`: `CARD`, `UPI`, and `NETBANKING`
- `PaymentStatus`: authorization, capture, settlement, refund, failure, and cancellation states
- `PaymentEvent`: events that cause payment transitions
- `PaymentActor`: actor recorded in transition logs
- `ChaosMode`: bank simulator behavior
- `Environment`: API-key environment
- `SettlementStatus` and `WebhookEventStatus`: operational lifecycle states

### 4.3 API response and exception handling

`GlobalResponseHandler` wraps controller responses in `ApiResponse`, giving the API a consistent envelope.

`GlobalExceptionHandler` maps common exceptions to structured error responses:

- Validation and malformed input: HTTP 400
- Invalid credentials: HTTP 400
- Resource not found: HTTP 404
- Duplicate or conflicting resources: HTTP 409
- JWT failures: HTTP 401
- Unhandled exceptions: HTTP 500

Validation is performed with Jakarta Bean Validation. Card expiry has a custom `@CardExpiryValidation` annotation and validator.

## 5. Merchant module

Package: `merchant`

### 5.1 Responsibilities

The merchant module manages:

- Merchant onboarding
- Dashboard users
- Password authentication
- JWT issuance
- API-key creation, listing, revocation, and rotation
- Merchant context used by downstream controllers

### 5.2 Main entities

| Entity | Purpose |
|---|---|
| `Merchant` | Business account, status, business information, and settlement-bank details |
| `AppUser` | Login user associated with a merchant; stores a BCrypt password hash and role |
| `ApiKey` | Public key ID, hashed secret, environment, enabled status, and rotation metadata |
| `Customer` | Merchant-owned customer profile |
| `MerchantWebhookConfig` | Webhook URL, event type, active flag, and secret configuration |

### 5.3 Signup flow

```text
POST /v1/auth/signup
        |
        v
AuthController
        |
        v
AuthServiceImpl
        |
        +--> Check merchant email uniqueness
        +--> Check app-user email uniqueness
        +--> Create Merchant with PENDING_KYC
        +--> BCrypt-encode password
        +--> Create AppUser with OWNER role
        +--> Save both records in one transaction
        v
MerchantSignUpResponse
```

### 5.4 Login flow

```text
POST /v1/auth/login
        |
        v
Find AppUser by email
        |
        v
AuthenticationManager + DaoAuthenticationProvider
        |
        v
Verify BCrypt password
        |
        v
JwtUtil creates token containing:
email/subject, merchant ID, and role
        |
        v
LoginResponse
```

### 5.5 API-key flow

The merchant first authenticates with a JWT and calls:

- `POST /v1/merchants/api-keys/`: create a key
- `GET /v1/merchants/api-keys/`: list keys
- `DELETE /v1/merchants/api-keys/{id}`: disable a key
- `POST /v1/merchants/api-keys/{id}`: rotate a key

During creation:

1. A key ID is generated with an `rzp_<environment>_` prefix.
2. A secret is generated and returned once.
3. Only the BCrypt hash of the secret is stored.

During rotation, the old hash is retained for a 24-hour grace period. Both the current and valid previous secret can be accepted during that period.

## 6. Authentication and authorization design

There are two stateless security paths.

### 6.1 JWT path

JWT protects:

```text
/v1/auth/**
/v1/merchants/**
```

Signup and login are public. Other routes in this filter chain require authentication.

`JwtAuthFilter`:

1. Reads the `Authorization: Bearer <token>` header.
2. Validates the token with `JwtUtil`.
3. Creates a Spring Security authentication object.
4. Extracts the merchant ID and stores it in `MerchantContext`.

### 6.2 API-key path

API keys protect:

```text
/v1/order/**
/v1/payments/**
```

The expected credential format is HTTP Basic authentication:

```text
Basic base64(keyId:keySecret)
```

`ApiKeyAuthFilter`:

1. Decodes the key ID and secret.
2. Loads the API key by key ID.
3. Checks that it is enabled.
4. Verifies the secret against the current hash or the previous hash during its grace period.
5. Stores the merchant ID and key ID in `MerchantContext`.

## 7. Order module

Package: `payment.controller.OrderController` and `payment.service.impl.OrderServiceImpl`

### 7.1 Order entity

An `Order` contains:

- Merchant ID
- Amount and currency
- Receipt
- Status
- Number of payment attempts
- JSON notes
- Expiration timestamp

Orders are initially created with `OrderStatus.CREATED`. If no expiration is supplied, the service sets expiration to 15 minutes from creation.

The `(merchant_id, receipt)` combination is unique, preventing duplicate receipts for the same merchant.

### 7.2 Create-order flow

```text
POST /v1/order
        |
        v
ApiKeyAuthFilter identifies merchant
        |
        v
OrderController gets merchant ID from MerchantContext
        |
        v
OrderServiceImpl
        |
        +--> Reject duplicate merchant/receipt
        +--> Set default expiry if required
        +--> Create Order with CREATED status
        +--> Save Order
        v
OrderResponse wrapped in ApiResponse
```

The service also contains methods to cancel an order, load an order, and list its payments. The current controller exposes creation; those other service methods are ready for additional endpoints.

## 8. Payment module

The payment module is split into four layers:

```text
PaymentController
      |
      v
PaymentServiceImpl
      |
      v
PaymentStrategyFactory -> PaymentStrategy
      |
      v
PaymentProcessorFactory -> PaymentProcessor
      |
      v
Simulated processor or Vault
```

### 8.1 Payment entity

`Payment` stores:

- Order and merchant association
- Idempotency key
- Amount
- Current payment status
- Payment method
- Method-specific JSON details
- Processor and bank references
- Error code and description

### 8.2 Payment initiation flow

```text
POST /v1/payments/initiate
        |
        v
PaymentController
        |
        v
PaymentServiceImpl.initiatePayment
        |
        +--> Load the order
        +--> Reject payment for PAID or CANCELLED order
        +--> Increment order attempts
        +--> Create Payment with CREATED status
        +--> Select PaymentStrategy by PaymentMethod
        +--> Record AUTHORIZE_ATTEMPT
        +--> Call strategy.initiate(...)
        +--> Store pending/success/failure references
        +--> Save Payment and Order
        v
PaymentResponse
```

The payment strategy result is one of:

- `Pending`: processor accepted the request and will complete asynchronously
- `Success`: processor completed immediately
- `Failed`: processor rejected the request

In the current simulated processors, initiation generally returns `Pending`, which leaves the payment in `AUTHORIZING` for the scheduled bank simulator to resolve.

### 8.3 Strategy pattern

`PaymentStrategy` abstracts payment-method-specific gateway behavior:

```java
PaymentResult initiate(PaymentRequest request);
PaymentResult capture(UUID paymentId);
```

Implementations:

- `CardPaymentStrategy`
- `UpiStrategy`
- `NetBankingStrategy`

`PaymentStrategyFactory` receives Spring’s map of implementations and selects one using `PaymentMethod`.

This keeps `PaymentServiceImpl` independent of method-specific gateway code.

### 8.4 Processor adapter pattern

`PaymentProcessor` represents a lower-level processor or bank integration:

```java
PaymentProcessorResponse processPayment(PaymentProcessorRequest request);
```

Implementations:

- `CardPaymentProcessor`
- `UpiPaymentProcessor`
- `NetBankingProcessor`

`PaymentProcessorFactory` selects the processor by payment method.

The processor adapters are currently test doubles. They generate processor references and return pending responses. They also contain failure test values:

- Card PAN `4000000000000002`: declined
- Card PAN `4000000000000003`: expired
- UPI VPA `fail@hdfc.in`: declined
- Net-banking bank code `BANK_CODE_FAIL`: declined

## 9. Payment state machine

`PaymentStateMachine` is the single place that defines legal payment transitions. `PaymentTransitionService` applies a transition and writes a `PaymentTransitionLog`.

### 9.1 Main lifecycle

```text
CREATED
   |
   | AUTHORIZE_ATTEMPT
   v
AUTHORIZING
   |                         |
   | AUTHORIZE_SUCCESS       | AUTHORIZE_FAIL / CANCEL
   v                         v
AUTHORIZED                FAILED / CANCELLED
   |
   | CAPTURE_REQUEST
   v
CAPTURING
   |                         |
   | CAPTURE_SUCCESS         | CAPTURE_FAIL
   v                         v
CAPTURED                 AUTHORIZED
   |
   | SETTLE
   v
SETTLED
```

Refund-related transitions are also defined:

```text
CAPTURED or SETTLED --REFUND_INIT--> PARTIAL_REFUND
PARTIAL_REFUND     --REFUND_COMPLETE--> REFUNDED
CAPTURED           --REFUND_COMPLETE--> REFUNDED
```

An invalid transition throws `BadRequestException`. Every successful transition is logged with the old status, event, new status, actor, and timestamp.

## 10. Authorization resolution and capture

`PaymentServiceImpl.resolveAuthorization` is used by the bank callback simulator.

For an approval:

```text
AUTHORIZING
   |
   | AUTHORIZE_SUCCESS
   v
AUTHORIZED
   |
   | CAPTURE_REQUEST
   v
CAPTURING
   |
   | Call selected strategy.capture(paymentId)
   v
CAPTURED
```

If capture succeeds, the associated order becomes `PAID`.

If capture fails, the payment returns to `AUTHORIZED` and stores the error information.

For a decline, the service applies `AUTHORIZE_FAIL`, stores the bank error code and description, and leaves the order unpaid.

The merchant can also call:

```text
POST /v1/payments/{paymentId}/capture
```

That endpoint is intended for manual capture from a merchant dashboard. It loads the payment by payment ID and merchant ID, applies `CAPTURE_REQUEST`, calls the selected payment strategy, and updates the result.

## 11. Bank callback simulator and chaos mode

Package: `payment.simulator`

`BankCallbackSimulator` is a scheduled component that stands in for an external bank callback service.

Every configured polling interval it:

1. Finds payments in `AUTHORIZING` state that are at least one second old.
2. Loads method-specific simulator configuration.
3. Calculates the payment’s due time from the configured delay range.
4. Skips payments that are not due yet.
5. Applies the configured `ChaosMode`.
6. Calls `resolveAuthorization` for approval or decline.

`SimulatorConfig.MethodSimulatorConfig` supports:

- Minimum delay
- Maximum delay
- Success rate

The configured modes are:

| Mode | Behavior |
|---|---|
| `NORMAL` | Uses method delay and success rate |
| `SLOW` | Doubles the calculated delay, then uses normal success logic |
| `SUCCESS` | Approves every due payment |
| `FAILURE` | Declines every due payment |
| `TIMEOUT` | Logs a timeout and leaves the payment in `AUTHORIZING` |

The success-rate bucket is derived from the payment ID hash, so the result is repeatable for the same payment rather than truly random.

Current configuration:

```yaml
payment:
  simulator:
    poll-interval-ms: 5000
    chaos-mode: NORMAL
    methods:
      CARD:
        min-delay-seconds: 2
        max-delay-seconds: 6
        success-rate: 90
      UPI:
        min-delay-seconds: 1
        max-delay-seconds: 3
        success-rate: 95
      NET_BANKING:
        min-delay-seconds: 4
        max-delay-seconds: 10
        success-rate: 80
```

## 12. Vault module

Package: `vault`

The vault prevents the rest of the payment flow from storing raw card PAN data in the payment record.

### 12.1 Vault entities

| Entity | Purpose |
|---|---|
| `VaultCard` | Encrypted PAN, encrypted data-encryption key, BIN, last four digits, brand, and expiry |
| `CardToken` | Merchant/customer-scoped token pointing to a vault card |

### 12.2 Tokenization flow

```text
POST /v1/vault/tokenize
        |
        v
VaultController
        |
        v
VaultServiceImpl.tokenize
        |
        +--> Extract BIN and last four digits
        +--> Detect card brand
        +--> Generate a per-card DEK
        +--> Encrypt PAN with DEK using AES-GCM
        +--> Encrypt DEK with the configured master key
        +--> Store VaultCard
        +--> Generate and store CardToken
        v
Return token, expiry, and last four digits
```

The API returns a token rather than the PAN. The token is later supplied in card payment method details.

### 12.3 Card charge flow

For a card payment, `CardPaymentStrategy` sends the token to the vault. The vault:

1. Loads a non-revoked `CardToken`.
2. Decrypts the DEK using the master-key encryptor.
3. Decrypts the PAN using the DEK.
4. Builds a processor request.
5. Calls `CardPaymentProcessor`.
6. Returns a processor response to the strategy.

The payment entity stores references and outcomes, not the raw PAN.

## 13. Operations module

Package: `operations.entity`

The operations package currently contains persistence models for capabilities that are not yet wired to controllers or services.

### 13.1 Settlements

`Settlement` models merchant payout calculations:

- Gross amount
- Refund amount
- Fees
- GST
- Net amount
- Status
- Bank reference
- Processed timestamp

`SettlementPayment` is the join entity connecting settlements to payments.

### 13.2 Webhooks

`WebhookEvent` models an outbound merchant webhook, including:

- Merchant and event type
- JSON payload
- Target URL
- Signature
- Delivery status
- Attempt count
- Retry time
- Last response details
- Delivered timestamp

`MerchantWebhookConfig` stores the merchant’s webhook endpoint and event configuration.

### 13.3 Dead-letter events

`DlqEvent` stores webhook events that could not be delivered after retries. It supports later replay through `replayedAt`.

There is currently no webhook dispatcher, retry scheduler, settlement service, refund controller, or DLQ replay service in the source tree. The entities establish the data model for those future capabilities.

## 14. Persistence and repository layer

Repositories are Spring Data JPA repositories. Important query patterns include:

- Find merchant, user, or API key by identity
- Find orders scoped to a merchant
- Find payments belonging to an order
- Find `AUTHORIZING` payments older than a time window for simulation
- Find non-revoked card tokens
- Find payment transition logs by payment ID

Hibernate is configured with PostgreSQL and `ddl-auto: update`, so the schema is updated from entity mappings during development.

JSON fields such as order notes and payment method details use PostgreSQL `jsonb` mappings.

## 15. End-to-end example: UPI payment

```text
1. Merchant creates an order with API-key authentication.
2. Client calls POST /v1/payments/initiate with the order ID and UPI details.
3. API-key filter identifies the merchant.
4. PaymentService creates a CREATED payment.
5. PaymentTransitionService changes CREATED -> AUTHORIZING.
6. UpiStrategy selects UpiPaymentProcessor.
7. UpiPaymentProcessor returns a processor reference and PENDING.
8. Payment remains AUTHORIZING.
9. BankCallbackSimulator finds the payment after its configured delay.
10. NORMAL mode evaluates the configured UPI success rate.
11. On success, authorization and capture are performed.
12. The payment becomes CAPTURED and the order becomes PAID.
13. Every state change is stored in PaymentTransitionLog.
```

## 16. Configuration reference

| Configuration | Purpose |
|---|---|
| `spring.datasource.*` | PostgreSQL connection |
| `spring.jpa.hibernate.ddl-auto` | Development schema update behavior |
| `vault.DEK_ENCRYPTION_KEY` | Base64 master key used to encrypt card DEKs |
| `jwt.secret-key` | JWT signing secret |
| `payment.simulator.poll-interval-ms` | Bank simulator polling interval |
| `payment.simulator.chaos-mode` | Global simulated bank behavior |
| `payment.simulator.methods.*` | Per-method delay and success settings |

## 17. Important implementation notes

These are current code-level characteristics to be aware of:

1. The enum value is `NETBANKING`, but the YAML key is `NET_BANKING`. Since simulator configuration is looked up using `method.name()`, the custom net-banking simulator settings may not be found.
2. `TIMEOUT` only logs a warning; it does not transition the payment to a timeout or terminal state.
3. Payment initiation uses an internally generated UUID idempotency key. The request does not currently supply or reuse an idempotency key.
4. The callback simulator computes delay from the payment ID hash, so delay is deterministic for a payment.
5. The payment strategy capture implementations currently return immediate simulated success references.
6. The operations entities exist, but their service and controller workflows are not implemented yet.
7. `OrderController` currently exposes order creation. The order service contains cancel, fetch, and payment-list methods that are not all exposed as REST endpoints.
8. Transition actors are currently recorded as `SYSTEM`; the transition service has a TODO to obtain the actual actor from the security context.
9. The application logs database user information at startup. This should be removed or sanitized in production.
10. The vault logs the configured master key in `VaultEncryption`; this is sensitive and should not be logged in production.

## 18. Suggested next extensions

The existing design can be extended with:

- Refund controller and service using the existing `Refund` entity and state transitions
- Settlement batch scheduler and settlement-payment association logic
- Webhook event publisher, HMAC signing, retry policy, and DLQ replay
- Payment-status and order-query endpoints
- Request-level idempotency handling
- API-key environment validation and improved authorization scopes
- Explicit timeout state handling
- Integration tests covering all state transitions and chaos modes
