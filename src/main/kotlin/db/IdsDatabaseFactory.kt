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

        val jdbcUrl = "jdbc:sqlite:$dbPath"

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection(jdbcUrl)
    }

    fun getCount(): Int {
        val stmt = connection.createStatement()
        val rs = stmt.executeQuery("SELECT COUNT(*) FROM id")
        val count = rs.getInt(1)
        rs.close()
        stmt.close()
        return count
    }

    fun getDayId(id: Int): Int {
        val stmt = connection.prepareStatement("SELECT wordId FROM id WHERE id = ?")
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()
        val wordId = rs.getInt("wordId")
        rs.close()
        stmt.close()
        return wordId
    }
}