package com.abhinav.caisarra.features.gameplay.domain

enum class PingQuality(val bars: Int) {
    Excellent(3),
    Fair(2),
    Poor(1),
    Offline(0);

    companion object {
        fun of(pingMs: Long?, connected: Boolean): PingQuality = when {
            !connected -> Offline
            pingMs == null || pingMs < 120 -> Excellent
            pingMs < 300 -> Fair
            else -> Poor
        }
    }
}
