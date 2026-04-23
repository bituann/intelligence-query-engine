# Profile Query API – README

## Overview

This API provides two endpoints for retrieving profile data:

1. **Structured Query Endpoint** – `/api/profiles`
   Uses explicit query parameters for filtering, sorting, and pagination.

2. **Natural Language Search Endpoint** – `/api/profiles/search`
   Accepts free-text queries and converts them into structured filters using pattern matching.

This document focuses on the **natural language search endpoint**, including supported query formats, keywords, behavior, and limitations.

---

## Structured Query

### Endpoint 
```
GET /api/profiles
```

Fetch a paginated list of profiles with optional filtering and sorting.

---

### Query Parameters

| Parameter                 | Type    | Description                                          |
| ------------------------- | ------- | ---------------------------------------------------- |
| `page`                    | Integer | Page number (0-based). Default: `0`                  |
| `limit`                   | Integer | Number of records per page. Default: `10`, Max: `50` |
| `sort_By`                 | String  | Field name to sort by                                |
| `order`                   | String  | Sort direction: `asc` or `desc`. Default: `asc`      |
| `gender`                  | String  | Filter by gender                                     |
| `age_group`               | String  | Filter by age group                                  |
| `country_id`              | String  | Filter by country (ISO code)                         |
| `min_age`                 | Integer | Minimum age (inclusive)                              |
| `max_age`                 | Integer | Maximum age (inclusive)                              |
| `min_country_probability` | Double  | Minimum country probability threshold                |
| `min_gender_probability`  | Double  | Minimum gender probability threshold                 |

---

### Validation Rules

* `limit` must not be greater than `50`
* Invalid requests return `400 Bad Request`

---

### Sorting

* Sorting is optional
* If `sort_By` is not provided or blank, results are unsorted
* `order` defaults to ascending if not specified or invalid

---

### Pagination

* `page` defaults to `0`
* `limit` defaults to `10`
* Uses standard offset-based pagination

---

### Filtering Logic

Filters are combined using logical `AND`. Only records matching **all provided criteria** are returned.

Applied filters:

* Exact match: `gender`, `age_group`, `country_id`
* Range filters:

    * `min_age` → `age >= value`
    * `max_age` → `age <= value`
* Probability thresholds:

    * `country_probability >= min_country_probability`
    * `gender_probability >= min_gender_probability`

---

### Response Format

```json
{
  "status": "success",
  "page": 0,
  "limit": 10,
  "total": 10,
  "data": [
    {
      "id": "...",
      "gender": "...",
      "age": "...",
      "country_id": "...",
      "gender_probability": "...",
      "country_probability": "..."
    }
  ]
}
```

---

### Response Fields

| Field    | Description                           |
| -------- | ------------------------------------- |
| `status` | Response status                       |
| `page`   | Current page number                   |
| `limit`  | Number of records per page            |
| `total`  | Number of records in the current page |
| `data`   | List of profile objects               |

---

### Example Request

```
GET /api/profiles?page=0&limit=5&gender=male&min_age=18&max_age=30&sort_By=age&order=desc
```

---

---


## Natural Language Search

### Endpoint

```
GET /api/profiles/search?q=<query>&page=<page>&limit=<limit>
```

### Parameters

* `q` (required): Natural language query string
* `page` (optional): Page number (default: 0)
* `limit` (optional): Number of results per page (default: 10, max: 50)

---

## Supported Query Patterns

The parser uses regex and keyword matching to extract filters from the query.

---

### 1. Gender Filtering

**Supported keywords:**

* `male`
* `female`
* `men` → interpreted as `male`
* `women` → interpreted as `female`

**Examples:**

* `"male users"`
* `"women in Canada"`
* `"female adults"`

**Behavior:**

* Case-insensitive
* If both male and female are detected in the same query, **gender filtering is ignored**

---

### 2. Age-Based Filtering

#### a. Age Groups

**Supported keywords:**

* `child`
* `teenager`
* `adult`
* `senior`

**Examples:**

* `"adult males"`
* `"female teenagers"`

---

#### b. "Young" Shortcut

**Keyword:**

* `young`

**Behavior:**

* Translates to:

    * `age >= 16`
    * `age <= 24`

**Example:**

* `"young men"`

---

#### c. Minimum Age (Lower Bound)

**Supported patterns:**

* `above <number>` → age strictly greater than
* `over <number>` → age strictly greater than
* `older than <number>` → age strictly greater than
* `from <number>` → age greater than or equal to

**Examples:**

* `"male above 30"`
* `"women over 25"`
* `"female from 18"`

---

#### d. Maximum Age (Upper Bound)

**Supported patterns:**

* `below <number>` → age strictly less than
* `under <number>` → age strictly less than
* `younger than <number>` → age strictly less than
* `to <number>` → age less than or equal to

**Examples:**

* `"male below 40"`
* `"women under 35"`
* `"female to 25"`

---

### 3. Country Filtering

**Supported input:**

* Any valid country name from ISO country list

**Examples:**

* `"users from Nigeria"`
* `"female adults in Canada"`
* `"male teenagers in Germany"`

**Behavior:**

* Case-insensitive substring match

---

## Combining Filters

**Examples:**

* `"female adults in Canada above 25"`
* `"young male in Nigeria"`
* `"teenager female under 20 in France"`

---

## Unsupported / Not Covered Scenarios

### 1. Multiple Conditions for Same Filter Type ❌

The API **does NOT support** queries that define different conditions for the same attribute.

**Examples that will NOT work correctly:**

* `"men over 30 and women under 50"`
* `"male teenagers and female adults"`

**Reason:**

* The parser builds a single specification per filter type and cannot separate logic into independent groups.

---

### 2. Logical Operators ❌

**Unsupported:**

* `AND`, `OR`, `NOT`
* Grouped or nested expressions

**Examples:**

* `"male OR female"`
* `"not male"`
* `"female AND adult"`

**Behavior:**

* The parser ignores logical intent and simply accumulates recognized filters.

---

### 3. Ambiguous or Unrecognized Queries ❌

If no valid patterns are detected, the API returns:

```
422 Unprocessable Entity
```

**Examples:**

* `"people with high probability"`
* `"random users"`
* `"cool profiles"`

---

### 4. Probability Filters ❌

Natural language search does **not support**:

* Gender probability
* Country probability

(These are only available in the structured `/api/profiles` endpoint.)

---

### 5. Sorting ❌

Natural language queries do not support:

* Sorting fields
* Sort direction

---

## Edge Cases & Behavior Notes

* If **no valid filter is detected**, request fails with `UnprocessableEntity`
* Queries are **order-independent** (word order does not matter)
* Pagination defaults:

    * `page = 0`
    * `limit = 10`
    * Maximum `limit = 50`

---

## Example Queries

| Query                     | Interpretation                                        |
| ------------------------- | ----------------------------------------------------- |
| `female adults in Canada` | gender = female AND ageGroup = adult AND country = CA |
| `young male`              | gender = male AND age between 17–23                   |
| `male above 30`           | gender = male AND age > 30                            |
| `female under 25`         | gender = female AND age < 25                          |
| `teenager in Nigeria`     | ageGroup = teenager AND country = NG                  |

---

## Summary

The natural language endpoint is a **regex-driven parser** designed for simple, keyword-based filtering. It works well for straightforward queries combining gender, age, and country, but does not support complex logic, multi-condition grouping, or advanced expressions.
