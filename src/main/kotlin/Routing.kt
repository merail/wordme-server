package app

import app.repository.WordsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
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

            call.respond(WordsRepository.getDayWord(dayWordId))
        }

        get("/isWordExist") {
            val enteredWord = call.request.queryParameters["enteredWord"].orEmpty()

            call.respond(WordsRepository.isWordExist(enteredWord))
        }

        get("/gameCountdownStartDate") {
            val date = application.environment.config
                .property("wordme.game_countdown_start_date")
                .getString()
            call.respondText(date)
        }
    }
}
