package app.db

import app.dto.WordDto
import io.ktor.server.application.ApplicationEnvironment
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

internal object WordsDatabaseFactory {

    lateinit var connection: Connection
        private set

    fun init(environment: ApplicationEnvironment) {
        val dbPath = environment.config
            .property("wordme.words_db.path")
            .getString()

        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun getDayWord(id: Int): WordDto {
        return connection.prepareStatement("SELECT * FROM word WHERE id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.toWordDto()
            }
        }
    }

    fun isWordExist(word: String): WordDto? {
        return connection.prepareStatement("SELECT * FROM word WHERE word = ?").use { stmt ->
            stmt.setString(1, word)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toWordDto() else null
            }
        }
    }

    private fun ResultSet.toWordDto() = WordDto(
        id = getInt("id"),
        word = getString("word"),
        isUndesirable = getBoolean("isUndesirable"),
    )
}
