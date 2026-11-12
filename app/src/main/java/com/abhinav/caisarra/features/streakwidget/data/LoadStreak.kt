package com.abhinav.caisarra.features.streakwidget.data

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.DailyStreakResponse

suspend fun loadStreak(context: Context): DailyStreakResponse =
    runCatching { RetrofitInstance.createGamesService(TokenManager.get(context)).getDailyStreak() }.getOrDefault(DailyStreakResponse())
