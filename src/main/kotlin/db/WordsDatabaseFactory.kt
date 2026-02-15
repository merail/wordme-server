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

        val jdbcUrl = "jdbc:sqlite:$dbPath"

        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection(jdbcUrl)
    }

    fun getDayWord(id: Int): WordDto {
        val stmt = connection.prepareStatement("SELECT * FROM word WHERE id = ?")
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()
        val word = rs.toWordDto()
        rs.close()
        stmt.close()
        return word
    }

    fun isWordExist(word: String): WordDto? {
        val stmt = connection.prepareStatement("SELECT * FROM word WHERE word = ?")
        stmt.setString(1, word)
        val rs = stmt.executeQuery()
        val result = if (rs.next()) rs.toWordDto() else null
        rs.close()
        stmt.close()
        return result
    }

    private fun ResultSet.toWordDto() = WordDto(
        id = getInt("id"),
        word = getString("word"),
        isUndesirable = getBoolean("isUndesirable"),
    )
}