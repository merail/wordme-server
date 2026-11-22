# wordme-server

Backend server for **[WordMe](https://github.com/merail/wordme)** — a native Android daily word game (Russian, 5-letter words, 6 attempts). The server stores the word list, maps each calendar day to a word, and exposes the endpoints the app uses to fetch the day's word and validate entered words.

## Overview

- Kotlin/JVM, built on [Ktor](https://ktor.io) (Netty engine) with `kotlinx.serialization`.
- Two SQLite databases:
  - `words.db` — the words (`word(id, word, isUndesirable)`);
  - `ids.db` — a shuffled day → word mapping (`id(id, wordId)`).
- Day gating: a word is served only after its day has started, using the earliest time zone (UTC+14) plus a 5-minute clock-skew allowance, so no honest player is ever rejected early.
- Runs behind nginx in production; Ktor itself listens only on `127.0.0.1:8081`. Token checks are done by nginx, not the app.

## Endpoints

| Route | Response |
|-------|----------|
| `GET /health` (and `HEAD`) | `200 OK` |
| `GET /dayWord?randomWordId=<n>` | JSON string — the day's word; `403 Forbidden` if the day hasn't started and no debug header is set |
| `GET /isWordExist?enteredWord=<word>` | JSON boolean |
| `GET /gameCountdownStartDate` | Plain text `14.07.2025` |

Without a valid `X-WordMe-Token` header, nginx returns `403` for `/dayWord`, `/gameCountdownStartDate` and `/isWordExist`. The `X-WordMe-Debug: 1` header (set by nginx only for a valid debug token) lets `/dayWord` return future day words.

## Requirements

- JDK 17+ (the Gradle wrapper downloads Gradle 9.3.0).

## Build & run

```bash
./gradlew test                          # Run the tests
./gradlew build                         # Build everything
./gradlew buildFatJar                   # Executable fat jar (build/libs/wordme-server-all.jar)
./gradlew buildImage                    # Build the Docker image for the fat jar
./gradlew publishImageToLocalRegistry   # Publish the Docker image locally
./gradlew run                           # Run the server
./gradlew runDocker                     # Run using the local Docker image
```

On a successful start the server logs:

```
Application - Application started in ... seconds.
Application - Responding at http://127.0.0.1:8081
```

## Configuration

Configuration lives in `src/main/resources/application.yaml` (production) and `application-dev.yaml` (local development):

| Key | Production | Dev |
|-----|------------|-----|
| `ktor.deployment.host` | `127.0.0.1` | not set (`0.0.0.0` default) |
| `ktor.deployment.port` | `8081` | `8081` |
| `wordme.words_db.path` | `/opt/wordme/data/words.db` | `data/words.db` |
| `wordme.ids_db.path` | `/opt/wordme/data/ids.db` | `data/ids.db` |
| `wordme.game_countdown_start_date` | `14.07.2025` | `14.07.2025` |

`data/` is gitignored; local SQLite files live there in dev.

## Project structure

```
src/main/kotlin/
├── Application.kt              # Entry point (EngineMain) and module wiring
├── Routing.kt                  # HTTP routes
├── DayWordAccess.kt            # Day-availability gating
├── Serialization.kt            # ContentNegotiation + JSON
├── db/
│   ├── IdsDatabaseFactory.kt   # ids.db — day → word mapping
│   └── WordsDatabaseFactory.kt # words.db — the words
├── dto/
│   └── Dtos.kt                 # WordDto(id, word, isUndesirable)
└── repository/
    └── WordsRepository.kt      # Day-word selection and word lookup
```

## Deployment

Production runs on an Ubuntu 24.04 VPS (`wordme.duckdns.org`) behind nginx. The nginx, systemd, and backup files are kept in [`deploy/`](deploy/README.md), which also documents the deploy steps and the server↔repo file mapping.
