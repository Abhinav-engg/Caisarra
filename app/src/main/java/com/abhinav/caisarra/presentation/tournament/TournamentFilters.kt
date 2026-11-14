package com.abhinav.caisarra.presentation.tournament

enum class TournamentStatusFilter(val label: String, val status: String) {
    Registration("Registration open", "registration"),
    Ongoing("In progress", "ongoing"),
    Completed("Completed", "completed"),
    Cancelled("Cancelled", "cancelled")
}

enum class TournamentFormatFilter(val label: String, val format: String) {
    Swiss("Swiss", "swiss"),
    RoundRobin("Round Robin", "round_robin"),
    Knockout("Knockout", "knockout")
}

enum class TournamentSpeed(val label: String) {
    Bullet("Bullet"),
    Blitz("Blitz"),
    Rapid("Rapid"),
    Classical("Classical")
}

enum class TournamentRatingFilter(val label: String) {
    Any("Any"),
    OpenToAll("Open to all"),
    RatingRequired("Rating required")
}

data class TournamentFilters(
    val statuses: Set<TournamentStatusFilter> = emptySet(),
    val formats: Set<TournamentFormatFilter> = emptySet(),
    val speeds: Set<TournamentSpeed> = emptySet(),
    val rating: TournamentRatingFilter = TournamentRatingFilter.Any,
    val joinedRange: IntRange? = null,
    val capacityRange: IntRange? = null,
    val openSpotsOnly: Boolean = false
) {
    val activeCount: Int
        get() = listOf(
            statuses.isNotEmpty(),
            formats.isNotEmpty(),
            speeds.isNotEmpty(),
            rating != TournamentRatingFilter.Any,
            joinedRange != null,
            capacityRange != null,
            openSpotsOnly
        ).count { it }
}
