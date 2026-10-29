package com.abhinav.caisarra.presentation.invite

object InviteCodeParser {

    /**
     * Extracts a match/invite code from raw user input or scanned QR code payload.
     * Handles full deep links (https://caisaara.duckdns.org/play/{code}),
     * custom schemes (caisarra://play/{code}), query parameters, or raw codes.
     */
    fun extractCode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        return when {
            trimmed.contains("/play/") -> {
                trimmed.substringAfterLast("/play/")
                    .substringBefore("?")
                    .substringBefore("/")
                    .trim()
            }
            trimmed.contains("/join/") -> {
                trimmed.substringAfterLast("/join/")
                    .substringBefore("?")
                    .substringBefore("/")
                    .trim()
            }
            trimmed.contains("code=") -> {
                trimmed.substringAfter("code=")
                    .substringBefore("&")
                    .substringBefore("/")
                    .trim()
            }
            trimmed.startsWith("caisarra://", ignoreCase = true) -> {
                trimmed.substringAfterLast("/")
                    .substringBefore("?")
                    .trim()
            }
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> {
                trimmed.substringAfterLast("/")
                    .substringBefore("?")
                    .trim()
            }
            else -> trimmed
        }
    }
}
