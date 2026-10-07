package com.abhinav.caisarra.domain.chess.model

data class Square(val file: Int, val rank: Int) {
    val name: String get() = "${'a' + file}${rank + 1}"
}