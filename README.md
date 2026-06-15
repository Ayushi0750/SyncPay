# SyncPay – Offline-First Secure Payment Infrastructure

SyncPay is an offline-first digital payment system inspired by UPI. It enables users to send money without internet connectivity by queuing transactions locally and synchronizing them automatically when the device comes back online.

Built with React, Spring Boot, PostgreSQL, and end-to-end encryption (AES-GCM + RSA). Designed for exactly-once settlement, replay protection, tamper resistance, double-spending prevention, and crash recovery.



## Table of Contents

- [Key Highlights](#key-highlights)
- [Load Test Summary](#load-test-summary)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Architecture](#architecture)
- [Transaction Flow](#transaction-flow)
- [Security Design](#security-design)
- [Database Schema](#database-schema)
- [Key Engineering Decisions](#key-engineering-decisions)
- [Future Improvements](#future-improvements)
- [Resume Summary](#resume-summary)
- [License](#license)


## Key Highlights

- Offline-first payment system with automatic synchronization
- Hybrid encryption (AES-GCM + RSA) for secure packet transmission
- Idempotent transaction processing for exactly-once settlement
- Replay attack protection using nonce and timestamp validation
- Reserved balance system to prevent double-spending
- Retry engine with exponential backoff strategy
- Dead Letter Queue (DLQ) for failed transaction handling
- Crash recovery for incomplete transaction reprocessing
- Comprehensive audit logging for all critical events

## Load Test Summary

Tests were conducted using Apache JMeter on a 4 vCPU, 16GB RAM system with local PostgreSQL.

| API Endpoint | Concurrent Users | Throughput | Average Latency | Error Rate |
|--------------|------------------|------------|-----------------|------------|
| POST /api/sync | 1,000 | 18.4 req/sec | 11.25 sec | 0.50% |
| GET /api/wallet/balance | 1,000 | 50.2 req/sec | 12 ms | 0.00% |
| POST /api/wallet/add | 1,000 | 49.9 req/sec | 24 ms | 0.00% |

No system crashes or data corruption were observed under network partition simulations.



## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React, Axios, React Router, Tailwind CSS |
| Backend | Spring Boot, Spring Security, JPA, Hibernate |
| Database | PostgreSQL |
| Security | JWT, AES-GCM, RSA,  |
| Testing | Apache JMeter |


## Quick Start

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher

### Backend Setup


# Clone the repository
git clone https://github.com/Ayushi0750/SyncPay.git
cd SyncPay/backend

# Create the database
createdb syncpay_db

# Configure environment variables
# Edit src/main/resources/application.properties
# Add the following:
# jwt.secret=your_jwt_secret_key
# aes.secret=your_aes_256_bit_key
# hmac.secret=your_hmac_secret_key

# Build and run
./mvnw clean install
./mvnw spring-boot:run


The backend will start on http://localhost:8081

###Frontend Setup

cd ../frontend
npm install
npm run dev

The frontend will start on http://localhost:5173


Environment Variables Reference
Variable	Description	Example
JWT_SECRET	Secret key for JWT signing	64+ character random string
AES_SECRET	256-bit key for AES encryption	32 byte base64 encoded string
HMAC_SECRET	Secret key for HMAC signing	32+ character random string
text



## Part 7: API Endpoints


## API Endpoints

All protected endpoints require a JWT token in the Authorization header: `Bearer <token>`

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| POST | /api/auth/register | Register a new user | None |
| POST | /api/auth/login | Authenticate and receive JWT token | None |
| GET | /api/wallet/balance | Get current wallet balance | Required |
| POST | /api/wallet/add | Add money from mock bank account | Required |
| POST | /api/payments | Send money to another user (online) | Required |
| POST | /api/sync | Synchronize offline transactions | Required |
| GET | /api/transactions | Get transaction history | Required |

### Example Request: Send Money

POST /api/payments
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "receiverId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 500.00,
  "transactionId": "txn_1704067200000_abc123"
}



## Architecture

Plain text diagram below. (Backticks removed to prevent formatting issues)

User
  │
  ▼
React Frontend
  │
  ├── Online Mode ──────────────────┐
  │                                 │
  └── Offline Queue (IndexedDB)     │
              │                     │
              ▼                     │
         Sync Engine                 │
              │                     │
              └──────────┬──────────┘
                         ▼
              Spring Boot Backend
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
   JWT Auth         Validation       Wallet Service
   Filter             Layer         (Debit/Credit)
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
   Idempotency       Replay          Audit
     Check         Protection        Logger
                         │
                         ▼
                   Retry Engine
                         │
                         ▼
                   Dead Letter
                      Queue
                         │
                         ▼
                    PostgreSQL
                      Database

### Core Components

| Component | Responsibility |
|-----------|---------------|
| QueueSyncService | Processes pending transactions, handles retries, crash recovery, idempotency validation, replay protection |
| Retry Engine | Automatically retries failed transactions with exponential backoff (1s, 2s, 4s, 8s) |
| Dead Letter Queue | Stores transactions that exceed retry limits for manual investigation |
| Sync Engine | Triggers synchronization when connectivity is restored |
| Recovery Service | Scans for incomplete transactions on startup and reprocesses safely |


## Transaction Flow

### Online Flow

| Step | Action |
|------|--------|
| 1 | User submits send money request |
| 2 | System validates wallet balance |
| 3 | Sender balance is debited |
| 4 | Receiver balance is credited |
| 5 | Transaction is recorded with status SUCCESS |

### Offline Flow

| Step | Action |
|------|--------|
| 1 | User submits send money request without internet |
| 2 | Transaction is stored in local queue with status OFFLINE_PENDING |
| 3 | Reserved balance is deducted locally to prevent double-spending |
| 4 | System monitors for internet connectivity |
| 5 | Sync engine processes queued transactions when online |
| 6 | Idempotency, replay, and HMAC validation are performed |
| 7 | Wallet balances are updated (reserved converted to actual) |
| 8 | Transaction status is updated to COMPLETED |
| 9 | Failed transactions enter retry queue or Dead Letter Queue |

### Transaction States

| State | Description |
|-------|-------------|
| PENDING | Transaction created, awaiting processing |
| PROCESSING | Transaction being processed by sync engine |
| SUCCESS | Transaction completed successfully |
| FAILED | Transaction failed after retries exhausted |
| OFFLINE_PENDING | Transaction stored locally, awaiting sync |
| SYNCING | Transaction being synchronized with server |
| DEAD_LETTER | Transaction moved to DLQ for manual review |




## Security Design

### Encryption Strategy

SyncPay uses hybrid encryption combining RSA and AES-GCM:

1. Transaction payload is encrypted using AES-GCM with a random key
2. The AES key is encrypted using the server's RSA public key
3. Both encrypted payload and encrypted key are transmitted together

### Packet Structure for Synchronization

{
  "encryptedData": "base64_encoded_aes_gcm_encrypted_payload",
  "encryptedKey": "base64_encoded_rsa_encrypted_aes_key",
  "hash": "hmac_sha256_signature",
  
}

### Security Mechanisms

| Threat | Solution | Implementation |
|--------|----------|----------------|
| Data exposure | AES-GCM encryption | 256-bit key, authenticated encryption |
| Key compromise | RSA key exchange | 2048-bit RSA, AES key encrypted per request |
| Tampering | HMAC validation | SHA-256 HMAC with server-side secret |
| Replay attacks | Nonce + timestamp | Unique nonce per request, 5-minute window |
| Duplicate processing | Idempotency | Unique transaction ID with database constraint |

### Idempotency Implementation

Each transaction contains a unique transaction ID. Before processing, the system executes:

IF transaction_id EXISTS IN transactions_table:
    RETURN duplicate_rejected
ELSE:
    PROCESS transaction
    INSERT INTO transactions_table
    RETURN success

This guarantees exactly-once processing and prevents duplicate payments.






## Database Schema

### users

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| name | VARCHAR(100) | User's full name |
| email | VARCHAR(255) | Unique, used for login |
| password | VARCHAR(255) | BCrypt hashed |
| created_at | TIMESTAMP | Default now() |
| updated_at | TIMESTAMP | Auto-update |

### wallets

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| available_balance | DECIMAL(15,2) | Spendable balance |
| reserved_balance | DECIMAL(15,2) | Locked for offline transactions |

### transactions

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| txn_id | VARCHAR(100) | Unique, idempotency key |
| sender_id | UUID | Foreign key to users |
| receiver_id | UUID | Foreign key to users |
| amount | DECIMAL(15,2) | Transaction amount |
| status | VARCHAR(50) | PENDING, SUCCESS, FAILED, etc. |
| cipher_hash | VARCHAR(255) | For integrity verification |
| created_at | TIMESTAMP | Default now() |

### pending_transactions

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| transaction_id | VARCHAR(100) | Original transaction ID |
| sender | VARCHAR(100) | Sender email or ID |
| receiver | VARCHAR(100) | Receiver email or ID |
| amount | DECIMAL(15,2) | Transaction amount |
| status | VARCHAR(50) | Offline queue status |
| retry_count | INTEGER | Number of retry attempts |
| created_at | TIMESTAMP | Default now() |

### audit_logs

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| event_type | VARCHAR(50) | LOGIN, TRANSFER, RETRY, FAILURE, SYNC |
| user_id | UUID | Foreign key to users |
| details | TEXT | JSON or plain text details |
| timestamp | TIMESTAMP | Default now() |

### nonce_store

| Column | Type | Description |
|--------|------|-------------|
| nonce | VARCHAR(255) | Unique, primary key |
| timestamp | TIMESTAMP | When nonce was created |
| processed_at | TIMESTAMP | When nonce was consumed |

### mock_bank_accounts

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| account_number | VARCHAR(50) | Fake account number |
| balance | DECIMAL(15,2) | Simulated bank balance |

### mock_bank_transactions

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| account_id | UUID | Foreign key to mock_bank_accounts |
| type | VARCHAR(20) | DEBIT or CREDIT |
| amount | DECIMAL(15,2) | Transaction amount |
| reference_id | VARCHAR(100) | For idempotency |
| created_at | TIMESTAMP | Default now() |


## Key Engineering Decisions

| Challenge | Solution | Rationale |
|-----------|----------|-----------|
| Offline payments | Local transaction queue with persistent storage | Ensures zero transaction loss during network outages |
| Duplicate charges | Idempotency keys with unique database constraint | Prevents financial inconsistencies from retries |
| Replay attacks | Nonce + timestamp validation with server-side storage | Attacker cannot reuse captured packets |
| Packet tampering | HMAC signatures with AES-GCM authenticated encryption | Tampering detected before processing |
| Crash during sync | Transaction state machine with recovery service | No orphaned or partially processed transactions |
| Double-spending offline | Reserved balance system with spend locks | User cannot spend same money twice offline |
| Temporary failures | Retry engine with exponential backoff (1s, 2s, 4s, 8s) | Improves success rate without overwhelming system |
| Persistent failures | Dead Letter Queue for manual investigation | Prevents infinite retry loops, enables debugging |
| Audit compliance | Comprehensive audit logging for all events | Full traceability for financial transactions |



## Future Improvements

| Priority | Improvement | Description |
|----------|-------------|-------------|
| High | Deployment | Deploy to Railway, Render, or AWS for live demo |
| High | Demo video | Add screen recording demonstrating offline flow |
| High | Redis caching | Cache nonce and idempotency keys for faster validation |
| High | WebSocket notifications | Live sync status updates to frontend |
| Medium | Kafka event streaming | Asynchronous transaction processing and retries |
| Medium | Prometheus + Grafana | Production monitoring and alerting |
| Low | Microservices decomposition | Separate auth, wallet, sync, and audit services |
| Low | Kubernetes deployment | Horizontal scaling and self-healing |


## Resume Summary

Built SyncPay, an offline-first UPI-like payment system using React, Spring Boot, and PostgreSQL. Implemented hybrid RSA and AES-GCM encrypted synchronization with exactly-once idempotent settlement, replay and tamper protection, and reserved-balance double-spending prevention. Load-tested for 1,000 concurrent users achieving 18.4 requests per second throughput with a 0.5% error rate. Wallet APIs achieved 50+ requests per second at sub-25ms latency. Designed crash recovery mechanisms, Dead Letter Queue handling, retry engine with exponential backoff, and comprehensive audit logging.


## License

MIT License

---

## Author

Built by Ayushi Tiwari to demonstrate distributed systems, security engineering, and offline-first architecture.

[LinkedIn](https://www.linkedin.com/in/ayushi-tiwari-977602347)

---

## Note

This project is inspired by modern payment systems like UPI. It uses a mock bank for simulation and is intended for learning and portfolio purposes. Live demo and demo video coming soon.


## Acknowledgements

This project was inspired by the [UPI Without Internet](https://github.com/perryvegehan/UPI_Without_Internet) repository. SyncPay is a complete from-scratch implementation built with React, Spring Boot, and PostgreSQL, adding offline queue management, reserved balance double-spending prevention, crash recovery, dead letter queue handling, and comprehensive load testing.








