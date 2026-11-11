package com.abhinav.caisarra.presentation.tournament

import com.abhinav.caisarra.data.remote.dto.TournamentDto
import java.time.OffsetDateTime

fun List<TournamentDto>.applyFiltersAndSort(
    filters: TournamentFilters,
    sort: TournamentSort
): List<TournamentDto> {
    val statuses = filters.statuses.map { it.status }.toSet()
    val formats = filters.formats.map { it.format }.toSet()
    val filtered = filter { t ->
        val joined = t.players ?: 0
        val ratingRequired = (t.minRating ?: 0) > 0
        (statuses.isEmpty() || t.status.lowercase() in statuses) &&
                (formats.isEmpty() || t.format.lowercase().replace(' ', '_') in formats) &&
                (filters.speeds.isEmpty() || tournamentSpeedOf(t.timeControl) in filters.speeds) &&
                when (filters.rating) {
                    TournamentRatingFilter.Any -> true
                    TournamentRatingFilter.OpenToAll -> !ratingRequired
                    TournamentRatingFilter.RatingRequired -> ratingRequired
                } &&
                (filters.joinedRange?.contains(joined) ?: true) &&
                (filters.capacityRange?.contains(t.maxPlayers) ?: true) &&
                (!filters.openSpotsOnly || joined < t.maxPlayers)
    }
    return when (sort) {
        TournamentSort.Newest -> filtered.sortedByDescending { createdEpoch(it) }
        TournamentSort.Oldest -> filtered.sortedBy { createdEpoch(it) }
        TournamentSort.MostPlayers -> filtered.sortedByDescending { it.players ?: 0 }
        TournamentSort.FewestPlayers -> filtered.sortedBy { it.players ?: 0 }
        TournamentSort.MostOpenSpots -> filtered.sortedByDescending { it.maxPlayers - (it.players ?: 0) }
        TournamentSort.LargestCapacity -> filtered.sortedByDescending { it.maxPlayers }
        TournamentSort.NameAscending -> filtered.sortedBy { it.name.lowercase() }
    }
}

private fun createdEpoch(t: TournamentDto): Long =
    runCatching { OffsetDateTime.parse(t.createdAt).toInstant().toEpochMilli() }.getOrDefault(0L)
