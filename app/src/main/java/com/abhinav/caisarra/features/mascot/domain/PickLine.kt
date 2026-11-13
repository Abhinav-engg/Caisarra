package com.abhinav.caisarra.features.mascot.domain

fun List<String>.pick(seed: Long): String {
    val index = ((seed % size) + size) % size
    return this[index.toInt()]
}

fun List<String>.pick(seed: Int): String = pick(seed.toLong())
