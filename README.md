# EquiBalance — Portfolio Management & Rebalancing Engine

EquiBalance is a full-stack web application that lets a user build and manage an investment
portfolio of stocks and ETFs, set target allocations, and automatically rebalance the
portfolio when drift exceeds a configurable threshold. Live price data is pulled from a
free market-data API. An analytics dashboard tracks portfolio performance, asset-class
breakdown, and rebalancing history — mirroring the core function of Parametric's custom
portfolio solutions.


## Features

### User & Auth
- Email/password registration and login secured with JWT (access + refresh tokens).
- Each user's portfolios and data are fully isolated from other users.

### Portfolio Management
- Full CRUD for portfolios (name, description, base currency, rebalancing threshold %).
- Full CRUD for holdings within a portfolio (ticker symbol, units, target allocation %).
- Validation that target allocations across a portfolio's holdings sum to 100%.
- Support for multiple portfolios per user.

### Live Price Integration
- Live prices fetched from a free market-data API (Alpha Vantage / Yahoo Finance).
- Prices cached in Redis with a 60-second TTL to stay within free-tier rate limits.
- Automatic fallback to the last known price with a "stale" badge if the API is unavailable.

### Drift Calculation & Rebalancing Engine
- Real-time calculation of each holding's current weight and drift from its target.
- Portfolio automatically flagged "Needs Rebalancing" once any holding's drift exceeds
  the configured threshold.
- A rebalancing algorithm generates the minimum set of buy/sell trades needed to restore
  every holding to its target allocation.
- Every rebalancing event is stored with a timestamp, before/after allocations, and the
  trades suggested.

### Transaction Log
- Every holding change (buy/sell/add/remove) is recorded with a timestamp and the price
  at the time of the action.
- A full chronological transaction log is available per portfolio.

### Analytics Dashboard
- Portfolio summary: total value, day's gain/loss vs. previous close, and overall return %.
- Allocation donut chart comparing current vs. target allocation.
- Drift bar chart showing per-holding drift from target.
- Performance line chart tracking portfolio value over time.
- Rebalancing history table (date, total drift before, trades executed).

### Watchlist
- Add tickers to a watchlist and track live prices and basic stats without creating a
  full portfolio.

### Non-Functional
- Dashboard loads in under 2 seconds; cached prices return in under 100ms.
- Redis caching guarantees free-tier API rate limits are never breached.
- Passwords hashed with BCrypt; JWT access/refresh token expiry; input validation on
  every endpoint.
- Stateless REST API with an externalized Redis cache, so the backend can scale
  horizontally across multiple instances.
- Clean service/repository/controller layering with DTOs for every API contract — no
  business logic lives in controllers.
- Spring Actuator health and metrics endpoints for observability.
- Responsive dashboard, usable on tablet and desktop browsers.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Backend | Java 17 + Spring Boot 3 (Web, Data JPA, Security, Actuator, Validation) |
| Database | PostgreSQL |
| Cache | Redis (price caching, 60s TTL) |
| Price API | Alpha Vantage free tier / Yahoo Finance |
| Auth | Spring Security + JWT (jjwt) |
| Frontend | React 18 + Vite |
| Charts | Recharts |
| Styling | Tailwind CSS |
| Containerization | Docker + docker-compose |
| CI/CD | GitHub Actions |
| Hosting | Render (backend) + Vercel (frontend) |
| Scripting | Python 3 + yfinance (demo data seeding) |



---

## Architecture

```
  User (Browser)
       |
       | REST (HTTP/JSON)
       v
+-------------------+        +---------------+
|  Spring Boot API  | <--->  |  Redis Cache  |   <-- Price TTL: 60s
|  auth | portfolio |        +---------------+
|  holdings | drift |
|  rebalance | txn  |        +---------------------+
+------+------------+        | Alpha Vantage / Yahoo|
       |                     | Finance Free API     |
       v                     +---------------------+
+-------------------+
|   PostgreSQL DB   |
| users | portfolios|
| holdings | txns   |
+-------------------+

  Frontend (React + Vite) --> served on Vercel
  talks to Spring Boot API on Render
```

---

## Project Structure

```
EquiBalance/
├── backend/                          Spring Boot API
│   ├── src/main/java/com/equibalance/
│   │   ├── config/                   Security, CORS, Redis config
│   │   ├── security/                 JWT service, auth filter, current-user helper
│   │   ├── entity/                   JPA entities (User, Portfolio, Holding, Transaction, RebalanceEvent)
│   │   ├── repository/               Spring Data JPA repositories
│   │   ├── dto/                      Request/response DTOs (auth, portfolio, rebalance, analytics)
│   │   ├── controller/               REST controllers
│   │   ├── service/                  Business logic (auth, portfolio, drift, rebalance, analytics, price)
│   │   ├── client/                   Market-data HTTP client
│   │   └── exception/                Global exception handling
│   ├── src/main/resources/
│   │   ├── application.yml           Local defaults
│   │   └── application-docker.yml    Docker-profile overrides (reads env vars)
│   ├── src/test/java/...             JUnit 5 unit tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                         React + Vite SPA
│   └── src/
│       ├── api/                      Axios client + endpoint wrappers
│       ├── context/                  AuthContext (JWT session state)
│       ├── pages/                    Login, Register, PortfolioList, PortfolioDetail, Analytics, Watchlist
│       ├── components/               Navbar, HoldingsTable, charts, RebalanceModal, TransactionLog
│       └── App.jsx / main.jsx
├── python-scripts/
│   └── seed_prices.py                Seeds demo transaction history via yfinance
├── .github/workflows/ci.yml          GitHub Actions: build + test backend & frontend
├── docker-compose.yml                One-command local stack (Postgres, Redis, backend, frontend)
├── .env.example                      Template for secrets (copy to .env)
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose (recommended path)
- A free market-data API key (Alpha Vantage: https://www.alphavantage.co/support/#api-key —
  no credit card required)

### Environment Variables / API Keys

Copy the example env file and fill in your own values:

```bash
cp .env.example .env
```

```
JWT_SECRET=<a long random string, 32+ characters>
ALPHAVANTAGE_API_KEY=<your key from alphavantage.co>
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

`.env` is git-ignored — never commit real secrets. Docker Compose reads this file automatically.

For frontend-only local dev (outside Docker), also copy `frontend/.env.example` to `frontend/.env`.

---

## Running with Docker Compose

```bash
docker compose up --build
```

This starts:

| Service | Port |
|---|---|
| PostgreSQL | 5432 |
| Redis | 6379 |
| Backend (Spring Boot) | 8080 |
| Frontend (Nginx + built React app) | 5173 |

Visit http://localhost:5173.


## API Reference

All endpoints except `/api/auth/**` require `Authorization: Bearer <accessToken>`.

```
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/refresh              (X-Refresh-Token header)

POST   /api/portfolios
GET    /api/portfolios
GET    /api/portfolios/{id}
PUT    /api/portfolios/{id}
DELETE /api/portfolios/{id}

POST   /api/portfolios/{id}/holdings
PATCH  /api/portfolios/{id}/holdings/{holdingId}
DELETE /api/portfolios/{id}/holdings/{holdingId}

GET    /api/portfolios/{id}/drift
POST   /api/portfolios/{id}/rebalance

GET    /api/portfolios/{id}/transactions
GET    /api/portfolios/{id}/analytics

GET    /api/prices/{ticker}
```

Spring Actuator health/metrics are exposed at `/actuator/health`, `/actuator/info`, `/actuator/metrics`.

---

## Data Model

```
User          (id, name, email, passwordHash, createdAt)
Portfolio     (id, userId, name, description, rebalanceThresholdPct, baseCurrency, createdAt)
Holding       (id, portfolioId, ticker, units, targetAllocationPct, addedAt)
PriceCache    (ticker, price, currency, fetchedAt)  -- stored in Redis as JSON
Transaction   (id, portfolioId, holdingId, type[BUY/SELL/ADD/REMOVE], units, priceAtTime, executedAt)
RebalanceEvent(id, portfolioId, triggeredAt, totalDriftBefore, tradesJson, status)
```

---

## Rebalancing Algorithm

Implemented in `RebalanceService.buildTrades()` — a clean O(n log n) algorithm:

```
Input: holdings {ticker, units, currentPrice, targetPct}, total portfolio value V

For each holding h:
  currentValue(h) = h.units * h.currentPrice
  currentPct(h)   = currentValue(h) / V * 100
  drift(h)        = currentPct(h) - h.targetPct
  targetValue(h)  = h.targetPct / 100 * V
  delta(h)        = targetValue(h) - currentValue(h)   // +ve = BUY, -ve = SELL

Sort holdings: SELLs first (frees up cash), then BUYs (spends that cash)
Output: trades {ticker, action, units, estimatedCost}
```

Covered by `backend/src/test/java/com/equibalance/service/RebalanceServiceTest.java`.

---

## Testing

```bash
# Backend unit + integration tests
cd backend && mvn test

# Frontend build check
cd frontend && npm run build
```

---
