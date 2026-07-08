package com.davidbrazuna.pokemonapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Response for the ability detail endpoint (AbilityItem.url). effect_entries
// repeats the same text per language; the UI picks the English one.
@Serializable
data class AbilityDetailResponse(
    @SerialName("effect_entries")
    val effectEntries: List<EffectEntry> = emptyList()
)

@Serializable
data class EffectEntry(
    @SerialName("short_effect")
    val shortEffect: String,
    val language: Language
)

@Serializable
data class Language(
    val name: String
)
