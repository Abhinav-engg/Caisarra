package com.abhinav.caisarra.features.minimates.domain

val miniMatesLevels = listOf(
    1 to "b.k/K../.Q.",
    1 to "RR./..k/K.n",
    2 to ".../k.K/..R",
    2 to ".K./PpR/k..",
    1 to "...k/..../.RK./...n",
    1 to "k.../..../..KN/.Q.n",
    2 to ".RR./k..K/..../....",
    2 to "..../K.R./.P../.k..",
    3 to ".n../..k./Q.N./...K",
    3 to ".R.K/k.../..../..N.",
    1 to "..k../p..../..K../...BR/.....",
    1 to "..K../...pk/...../...../Q....",
    2 to "...../nKR../...../k..../..R..",
    2 to "...../...../..K../.RB.k/n....",
    3 to "...../....k/...../..K../R....",
    3 to "...../....K/..k../...../.Q.B."
).mapIndexed { index, (mateIn, layout) -> MiniMatesLevel(index + 1, mateIn, layout) }
