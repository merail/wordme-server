package app.db

import io.ktor.server.application.ApplicationEnvironment
import java.sql.Connection
import java.sql.DriverManager

internal object IdsDatabaseFactory {

    lateinit var connection: Connection
        private set

    fun init(environment: ApplicationEnvironment) {
        val dbPath = environment.config
            .property("wordme.ids_db.path")
            .getString()

        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun getCount(): Int {
        return connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) FROM id").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
    }

    fun getDayId(id: Int): Int {
        return connection.prepareStatement("SELECT wordId FROM id WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getInt("wordId")
            }
        }
    }
}
