# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WordMe is the backend server for the WordMe Android daily word game (Russian, 5-letter words). It is a small Kotlin/JVM service built on Ktor (Netty engine) with two SQLite databases — one for the words, one for the day-to-word mapping. The companion Android client lives in a separate repository.

- **Stack**: Kotlin 2.3.0, Ktor 3.4.0 (Netty), kotlinx-serialization, SQLite (sqlite-jdbc 3.46.0.0), Logback
- **Build**: Gradle 9.3.0, JVM toolchain 17
- **Public GitHub**: https://github.com/merail/wordme-server (branch `main`)
- **Production**: Ubuntu 24.04 VPS (`wordme.duckdns.org`) behind nginx; Ktor listens on `127.0.0.1:8081`

## Build Commands

```bash
./gradlew test                          # Run the tests
./gradlew build                         # Build everything
./gradlew buildFatJar                   # Executable fat jar (build/libs/wordme-server-all.jar)
./gradlew buildImage                    # Build the Docker image for the fat jar
./gradlew publishImageToLocalRegistry   # Publish the Docker image locally
./gradlew run                           # Run the server
./gradlew runDocker                     # Run using the local Docker image
```

## Architecture

Single-module app (`group = "app"`, `version = "0.0.1"`). The entry point is Ktor's `EngineMain`; the module `app.ApplicationKt.module` is declared in `application.yaml`.

| File | Role |
|------|------|
| `Application.kt` | `main()` → `EngineMain`; `module()` initializes both DB factories, then `configureRouting()` and `configureSerialization()` |
| `Routing.kt` | All HTTP routes (`/health`, `/dayWord`, `/isWordExist`, `/gameCountdownStartDate`) |
| `DayWordAccess.kt` | Day-availability gating (see below) |
| `Serialization.kt` | ContentNegotiation + kotlinx-serialization JSON (pretty-printed, UTF-8) |
| `db/IdsDatabaseFactory.kt` | SQLite `ids.db`: table `id(id, wordId)` — maps a day number to a word id |
| `db/WordsDatabaseFactory.kt` | SQLite `words.db`: table `word(id, word, isUndesirable)` |
| `repository/WordsRepository.kt` | Day-word selection and word-existence lookup |
| `dto/Dtos.kt` | `WordDto(id, word, isUndesirable)` |

Both DB paths come from config (`wordme.words_db.path`, `wordme.ids_db.path`) and are opened as plain JDBC SQLite connections at startup (`DriverManager`).

### Day availability (`DayWordAccess`)

`/dayWord` returns a word only for days that have already started. `DayWordAccess.isAvailable(dayWordId)` compares the day number against the countdown start date, using the earliest time zone (UTC+14) plus a 5-minute clock-skew allowance, so no honest player anywhere is rejected early:

```
dayWordId <= DAYS.between(gameCountdownStartDate, now + 5min in UTC+14) + 1
```

The `X-WordMe-Debug: 1` request header (set by nginx only for a valid debug token) bypasses this check and allows future day words.

### Day-word selection (`WordsRepository.getDayWord`)

The day number is not a word id directly. `ids.db` holds a shuffled day → word mapping, and `words.db` the words:

- `totalCount` = number of rows in `ids.db`;
- `totalBias` = 1 when `randomWordId % totalCount == 0`, else 0;
- `dayWordId` = `ids.getDayId((randomWordId + totalBias) % totalCount)`;
- the response is `words.getDayWord(dayWordId).word`.

## Server API

Auth is enforced by nginx, not Ktor: without a valid `X-WordMe-Token`, `/dayWord`, `/gameCountdownStartDate` and `/isWordExist` return 403 (see `deploy/README.md`).

| Route | Response |
|-------|----------|
| `GET /health` (and `HEAD`) | `200` `OK` |
| `GET /dayWord?randomWordId=<n>` | JSON string — the day's word; `403` if the day hasn't started and there is no debug header |
| `GET /isWordExist?enteredWord=<word>` | JSON boolean |
| `GET /gameCountdownStartDate` | Plain text `14.07.2025` |

## Configuration

`src/main/resources/application.yaml` (production) and `application-dev.yaml` (local):

- `ktor.deployment.host` / `ktor.deployment.port` — `127.0.0.1:8081` in production
- `wordme.words_db.path`, `wordme.ids_db.path` — SQLite file paths (`/opt/wordme/data/*.db` in production, `data/*.db` in dev)
- `wordme.game_countdown_start_date` — `dd.MM.yyyy` (`14.07.2025`)

`data/` is gitignored; local SQLite files live there in dev.

## Deployment

See `deploy/README.md` for the nginx/systemd/backup files (mirrored from the production VPS) and the apply steps. Key points:

- Build: `./gradlew buildFatJar` → `scp build/libs/wordme-server-all.jar wordme:/tmp/` → on the server `mv` it over `/opt/wordme/app/wordme.jar` (not `cp` over a running jar) → `systemctl restart wordme` → check `/health`.
- Ktor listens only on `127.0.0.1:8081`; nginx is the sole entry point and checks the tokens.
- Real token values are never committed — only `*.map.example` files in `deploy/nginx/secrets/`.

## Code Style

- Kotlin official code style (`kotlin.code.style=official`)
- Code comments: minimal, English only
- Commit messages: single line, English, no body / no Co-Authored-By
