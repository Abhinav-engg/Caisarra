package com.abhinav.caisarra.features.notificationsettings.presentation

enum class NotificationCategory(val section: String, val title: String, val subtitle: String) {
    CHALLENGES("GAMES", "Game Challenges", "Invites and challenge responses"),
    TOURNAMENTS("GAMES", "Tournaments", "Round starts, results and cancellations"),
    FRIENDS("SOCIAL", "Friend Requests", "New requests and accepted requests"),
    DIRECT_MESSAGES("SOCIAL", "Direct Messages", "New private messages"),
    COMMUNITY("SOCIAL", "Community", "Comments and reactions on your posts"),
    CLUBS("SOCIAL", "Clubs", "Role changes and membership updates")
}
