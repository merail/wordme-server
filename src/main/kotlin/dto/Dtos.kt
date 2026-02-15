package app.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class WordDto(
    val id: Int,
    val word: String,
    val isUndesirable: Boolean,
)