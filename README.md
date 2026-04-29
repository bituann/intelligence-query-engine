# Intelligence Query Engine

A Spring Boot REST API for Insighta Labs — a demographic intelligence platform used to segment users, identify patterns, and query large datasets using advanced filtering, sorting, pagination, and natural language search.

---

## Tech Stack

- Java 17
- Spring Boot
- PostgreSQL (via Neon)
- JWT authentication (RSA key pair)
- GitHub OAuth 2.0 with PKCE

---

## Prerequisites

- Java 17+
- Maven
- A PostgreSQL database
- A GitHub OAuth App

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/bituann/intelligence-query-engine.git
cd intelligence-query-engine
```

### 2. Set up environment variables

Create a `.env` file at the root or set the following variables in your environment:

```env
DB_URL=jdbc:postgresql://<host>/<database>
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
GITHUB_REDIRECT_URL=http://localhost:8080/auth/callback

RSA_PRIVATE_KEY=your_rsa_private_key
RSA_PUBLIC_KEY=your_rsa_public_key

SEED_DATA_URL=url_to_seed_data
FRONTEND_AUTH_CALLBACK=http://localhost:3000/api/auth/callback
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Authentication

Authentication uses GitHub OAuth 2.0. Two flows are supported:

**Web portal flow:**
1. Frontend requests the GitHub OAuth URL from `GET /auth/github/url`
2. User is redirected to GitHub
3. GitHub redirects to `GET /auth/callback`
4. Backend sets HTTP-only cookies and redirects to the frontend callback

**CLI flow (PKCE):**
1. CLI generates a `code_verifier` and `code_challenge`
2. CLI requests the GitHub OAuth URL with the challenge
3. User completes OAuth in the browser
4. GitHub redirects to the CLI's local callback server with the authorization code
5. CLI exchanges the code + verifier directly with the backend
6. Backend returns `{ access_token, refresh_token }` as JSON

---

## Roles

| Role | Permissions |
|---|---|
| `admin` | Full access — create, delete, query profiles |
| `analyst` | Read-only — list, search, view profiles |

Default role on signup: `analyst`

---

## API Overview

All `/api/*` endpoints require authentication via `Authorization: Bearer <token>` and the header `X-API-Version: 1`.

### Auth Endpoints (`/auth/*`)
Rate limit: 10 requests/minute

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/auth/github/url` | Get GitHub OAuth URL |
| `GET` | `/auth/callback` | GitHub OAuth callback |
| `POST` | `/auth/refresh` | Refresh access token |
| `POST` | `/auth/logout` | Logout |
| `GET` | `/auth/me` | Get current user |

### Profile Endpoints (`/api/*`)
Rate limit: 60 requests/minute per user

| Method | Endpoint | Description | Role |
|---|---|---|---|
| `GET` | `/api/profiles` | List profiles with filters | analyst+ |
| `GET` | `/api/profiles/:id` | Get profile by ID | analyst+ |
| `GET` | `/api/profiles/search?q=` | Natural language search | analyst+ |
| `POST` | `/api/profiles` | Create profile | admin |
| `GET` | `/api/profiles/export` | Export profiles as CSV | analyst+ |

[//]: # (| `DELETE` | `/api/profiles/:id` | Delete profile | admin |)

---

## Structured Query

### Endpoint

```
GET /api/profiles
```

### Query Parameters

| Parameter | Type | Description |
|---|---|---|
| `page` | Integer | Page number (0-based, default: `0`) |
| `limit` | Integer | Records per page (default: `10`, max: `50`) |
| `sort_by` | String | Field name to sort by |
| `order` | String | Sort direction: `asc` or `desc` (default: `asc`) |
| `gender` | String | Filter by gender |
| `age_group` | String | Filter by age group |
| `country_id` | String | Filter by ISO country code |
| `min_age` | Integer | Minimum age (inclusive) |
| `max_age` | Integer | Maximum age (inclusive) |
| `min_gender_probability` | Double | Minimum gender probability threshold |
| `min_country_probability` | Double | Minimum country probability threshold |

### Validation Rules

- `limit` must not exceed `50`
- Invalid requests return `400 Bad Request`

### Sorting

- Sorting is optional
- If `sort_by` is not provided or blank, results are unsorted
- `order` defaults to `asc` if not specified or invalid

### Filtering Logic

Filters are combined using logical `AND`. Only records matching all provided criteria are returned.

- Exact match: `gender`, `age_group`, `country_id`
- Range filters: `min_age` → `age >= value`, `max_age` → `age <= value`
- Probability thresholds: `country_probability >= min_country_probability`, `gender_probability >= min_gender_probability`

### Response Format

```json
{
  "status": "success",
  "page": 0,
  "limit": 10,
  "total": 10,
  "total_pages": 1,
  "links": {
    "next": "/api/profiles?page=1&limit=10",
    "prev": null,
    "self": "/api/profiles?page=0&limit=10"
  },
  "data": [
    {
      "id": "...",
      "name": "...",
      "gender": "...",
      "genderProbability": 0.98,
      "age": 25,
      "ageGroup": "adult",
      "countryId": "NG",
      "countryName": "Nigeria",
      "countryProbability": 0.91,
      "createdAt": "..."
    }
  ]
}
```

### Example Request

```
GET /api/profiles?page=0&limit=5&gender=male&min_age=18&max_age=30&sort_by=age&order=desc
```

---

## Natural Language Search

### Endpoint

```
GET /api/profiles/search?q=<query>&page=<page>&limit=<limit>
```

### Parameters

| Parameter | Required | Description |
|---|---|---|
| `q` | Yes | Natural language query string |
| `page` | No | Page number (default: `0`) |
| `limit` | No | Records per page (default: `10`, max: `50`) |

### Supported Query Patterns

**Gender**

| Keyword | Interpreted as |
|---|---|
| `male`, `men` | gender = male |
| `female`, `women` | gender = female |

- Case-insensitive
- If both male and female are detected in the same query, gender filtering is ignored

**Age Groups**

Supported keywords: `child`, `teenager`, `adult`, `senior`

**"Young" Shortcut**

The keyword `young` translates to `age >= 16` and `age <= 24`.

**Minimum Age (Lower Bound)**

| Pattern | Behaviour |
|---|---|
| `above <n>` | age > n |
| `over <n>` | age > n |
| `older than <n>` | age > n |
| `from <n>` | age >= n |

**Maximum Age (Upper Bound)**

| Pattern | Behaviour |
|---|---|
| `below <n>` | age < n |
| `under <n>` | age < n |
| `younger than <n>` | age < n |
| `to <n>` | age <= n |

**Country Filtering**

Accepts any valid country name from the ISO country list. Case-insensitive substring match.

### Combining Filters

Filters can be freely combined in a single query:

```
female adults in Canada above 25
young male in Nigeria
teenager female under 20 in France
```

### Example Queries

| Query | Interpretation |
|---|---|
| `female adults in Canada` | gender=female, ageGroup=adult, country=CA |
| `young male` | gender=male, age between 16–24 |
| `male above 30` | gender=male, age > 30 |
| `female under 25` | gender=female, age < 25 |
| `teenager in Nigeria` | ageGroup=teenager, country=NG |

### Unsupported Scenarios

| Scenario | Example |
|---|---|
| Multiple conditions for the same filter | `men over 30 and women under 50` |
| Logical operators | `male OR female`, `not male` |
| Probability filters | not supported in NL search |
| Sorting | not supported in NL search |

Returns `422 Unprocessable Entity` if no valid filter is detected.

---

## CSV Export

```
GET /api/profiles/export?format=csv
```

Supports the same filters as the profiles list endpoint. Returns a CSV attachment with columns:

```
id, name, gender, gender_probability, age, age_group, country_id, country_name, country_probability, created_at
```

---

## CI/CD

GitHub Actions runs on every pull request to `main`:

- **Test** — runs the full test suite
- **Build** — Maven build (runs only if tests pass)

---

## Related Repositories

- [insighta-web-portal](https://github.com/bituann/insighta-web-portal) — Next.js web portal
- [insighta-cli](https://github.com/bituann/insighta-cli) — CLI tool