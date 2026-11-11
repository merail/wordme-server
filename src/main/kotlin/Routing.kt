package app

import app.repository.WordsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DEBUG_HEADER = "X-WordMe-Debug"

fun Application.configureRouting() {
    val gameCountdownStartDate = environment.config
        .property("wordme.game_countdown_start_date")
        .getString()

    val dayWordAccess = DayWordAccess(
        gameCountdownStartDate = LocalDate.parse(
            gameCountdownStartDate,
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        ),
    )

    routing {
        route("/health") {

            get {
                call.respondText("OK")
            }

            head {
                call.respond(HttpStatusCode.OK)
            }
        }

        get("/dayWord") {
            val dayWordId = call.request.queryParameters["randomWordId"]?.toInt() ?: 0

            val isDebug = call.request.headers[DEBUG_HEADER] == "1"
            if (isDebug.not() && dayWordAccess.isAvailable(dayWordId).not()) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respond(WordsRepository.getDayWord(dayWordId))
        }

        get("/isWordExist") {
            val enteredWord = call.request.queryParameters["enteredWord"].orEmpty()

            call.respond(WordsRepository.isWordExist(enteredWord))
        }

        get("/gameCountdownStartDate") {
            call.respondText(gameCountdownStartDate)
        }
    }
}
