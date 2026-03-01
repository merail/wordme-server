package app.repository

import app.db.IdsDatabaseFactory
import app.db.WordsDatabaseFactory

internal object WordsRepository {
    fun getDayWord(
        randomWordId: Int,
    ): String {
        val totalCount = IdsDatabaseFactory.getCount()
        val totalBias = if (randomWordId % totalCount == 0) {
            1
        } else {
            0
        }

        val dayWordId = IdsDatabaseFactory.getDayId(
            id = (randomWordId + totalBias) % totalCount,
        )

        return WordsDatabaseFactory.getDayWord(dayWordId).word
    }

    fun isWordExist(
        word: String,
    ): Boolean {
        return WordsDatabaseFactory.isWordExist(word) != null
    }
}