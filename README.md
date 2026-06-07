# FollowThrough

Turn meeting transcripts into decisions, action items, accountability, and follow-through.

## Why I Built This

During my first year in industry, I noticed a recurring pattern.

Meetings ended with decisions.

Action items were assigned.

Everyone aligned on next steps.

Yet a few days later, people were asking:

* Why did we make this decision?
* Who owns this task?
* Wasn't there a deadline for this?

Most meeting tools do a great job capturing conversations and generating summaries.

I wanted to explore a different problem:

**What happens after the meeting ends?**

How do decisions remain discoverable?

How do action items stay accountable?

How do teams avoid repeating the same discussions months later?

FollowThrough is my attempt to solve that problem.

## What It Does

Upload a meeting transcript or Minutes of Meeting (MoM).

FollowThrough automatically extracts:

* Decisions
* Action Items
* Owners
* Due Dates

And then helps teams follow through by providing:

* Searchable decision history
* Reminder workflows
* Overdue detection
* Real-time updates
* Decision context preservation

## Example Flow

```text
Meeting Transcript
        │
        ▼
   AI Extraction
        │
        ▼
┌─────────────────────┐
│ Decisions           │
│ Action Items        │
│ Owners              │
│ Due Dates           │
└─────────────────────┘
        │
        ▼
    PostgreSQL
        │
        ▼
      Kafka
        │
        ▼
 Notification Service
        │
        ▼
 Reminders + Alerts +
 Real-Time Updates
```

## Architecture

```text
┌─────────────────────────────────────────────┐
│                 FollowThrough               │
└─────────────────────────────────────────────┘

          ┌──────────────┐
          │   Main App   │
          └──────┬───────┘
                 │
                 ▼
          ┌──────────────┐
          │ PostgreSQL   │
          └──────────────┘
                 │
                 ▼
          ┌──────────────┐
          │    Redis     │
          └──────────────┘

                 │
                 ▼

          ┌──────────────┐
          │    Kafka     │
          └──────┬───────┘
                 │
                 ▼

     ┌─────────────────────────┐
     │ Notification Service    │
     └──────────┬──────────────┘
                │
                ▼
      WebSocket Notifications

                │
                ▼

        End Users / Dashboard

  -------------------------------
      Gemini / Claude
     (LLM Extraction Layer)
  -------------------------------

      Prometheus
         +
       Grafana

  -------------------------------
```


## Tech Stack

| Component               | Technology              |
| ----------------------- | ----------------------- |
| Backend                 | Java 21 + Spring Boot 3 |
| Database                | PostgreSQL 16           |
| Messaging               | Apache Kafka            |
| Cache                   | Redis 7                 |
| AI Extraction           | Gemini / Claude         |
| Real-Time Communication | WebSocket + STOMP       |
| Resilience              | Resilience4j            |
| Observability           | Prometheus + Grafana    |
| Containerization        | Docker Compose          |

---

## Key Technical Decisions

### Kafka over REST

Notification delivery should not depend on another service being online.

Kafka provides:

* Temporal decoupling
* Replay capability
* Retry handling
* Fan-out support

### PostgreSQL Full-Text Search over Elasticsearch

For the current scale, PostgreSQL provides everything needed while avoiding another operational dependency.

### Redis Cache-Aside Strategy

Frequently accessed decisions and overdue items are cached while PostgreSQL remains the source of truth.

### LLM Behind an Interface

LLM providers can be swapped without changing business logic.

Circuit breakers prevent cascading failures during provider outages.

---

## Running Locally

### Prerequisites

* Docker
* Docker Compose
* Gemini API Key

### Start Everything

```bash
export GEMINI_API_KEY=your-api-key

docker compose up --build
```

### Available Services
-----------------------------------------------
| Service              | URL                   |
| -------------------- | --------------------- |
| Main API             | http://localhost:8080 |
| Notification Service | http://localhost:8081 |
| Kafka UI             | http://localhost:8090 |
| Prometheus           | http://localhost:9090 |
| Grafana              | http://localhost:3000 |
-----------------------------------------------
---

## Core API Flow

### Create Meeting

```http
POST /api/meetings
```

### Extract Decisions & Action Items

```http
POST /api/meetings/{id}/extract
```

### Search Historical Decisions

```http
GET /api/decisions/search?q=kafka
```

### View Overdue Tasks

```http
GET /api/action-items/overdue
```

### Update Task Status

```http
PATCH /api/action-items/{id}/status
```

---

## Roadmap

### Completed

* Transcript ingestion
* AI-powered extraction
* Decision management
* Action item tracking
* Kafka-based notifications
* Overdue detection
* Redis caching
* Observability with Prometheus and Grafana

### Next

* Web dashboard
* User authentication
* Email notifications
* Slack integration
* Chrome extension
* Multi-user collaboration

---

## Lessons Learned

Building the happy path is easy.

Building systems that continue to work when dependencies fail is where engineering becomes interesting.

This project helped me explore:

* Event-driven architecture
* Reliability patterns
* Caching strategies
* AI integration
* Distributed system design trade-offs
* Observability and monitoring

---

## Current Status

🚧 Active Development

The backend foundation is complete.

The next goal is to evolve FollowThrough into a complete end-to-end product with a user-facing experience and collaborative workflows.