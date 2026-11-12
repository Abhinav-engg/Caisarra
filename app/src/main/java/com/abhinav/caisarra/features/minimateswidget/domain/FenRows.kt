package com.abhinav.caisarra.features.minimateswidget.domain

fun fenRows(fen: String): List<List<Char?>> = fen.substringBefore(" ").split("/").map { rank ->
    rank.flatMap { if (it.isDigit()) List(it.digitToInt()) { null } else listOf(it) }
}
