package app

import app.db.IdsDatabaseFactory
import app.db.WordsDatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    IdsDatabaseFactory.init(environment)
    WordsDatabaseFactory.init(environment)
    configureRouting()
    configureSerialization()
}
