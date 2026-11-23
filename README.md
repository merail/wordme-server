# wordme-server

Backend server for **[WordMe](https://github.com/merail/wordme)** — a native Android daily word game (Russian, 5-letter words, 6 attempts). It stores the word list, maps each calendar day to a word, and serves the endpoints the app uses to fetch the day's word and validate entered words.

## Overview

- Kotlin/JVM on [Ktor](https://ktor.io) (Netty engine) with `kotlinx.serialization`.
- Two SQLite databases: `words.db` (the words) and `ids.db` (a shuffled day → word mapping).
- A word is served only once its day has started, using the earliest time zone (UTC+14) plus a small clock-skew allowance.

## Endpoints

| Route | Response |
|-------|----------|
| `GET /health` | `200 OK` |
| `GET /dayWord?randomWordId=<n>` | JSON string — the day's word; `403` if the day hasn't started |
| `GET /isWordExist?enteredWord=<word>` | JSON boolean |
| `GET /gameCountdownStartDate` | Plain text `14.07.2025` |

All endpoints except `/health` require authentication, enforced by nginx in production.

## Requirements

- JDK 17+ (the Gradle wrapper downloads Gradle 9.3.0).

## Build & run

```bash
./gradlew test                          # Run the tests
./gradlew build                         # Build everything
./gradlew buildFatJar                   # Executable fat jar (build/libs/wordme-server-all.jar)
./gradlew run                           # Run the server
```

## Configuration

Configuration lives in `src/main/resources/application.yaml` (production) and `application-dev.yaml` (local development).

## Deployment

Production runs behind nginx; see [`deploy/`](deploy/README.md) for the server configuration.
