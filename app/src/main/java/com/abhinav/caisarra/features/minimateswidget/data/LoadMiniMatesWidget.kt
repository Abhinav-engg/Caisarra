package com.abhinav.caisarra.features.minimateswidget.data

import android.content.Context
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.features.minimates.data.MiniMatesOverviewDto
import com.abhinav.caisarra.features.minimates.data.MiniMatesRepository
import com.abhinav.caisarra.features.minimateswidget.domain.MiniMatesWidgetState

suspend fun loadMiniMatesWidget(context: Context): MiniMatesWidgetState {
    val repository = MiniMatesRepository(RetrofitInstance.createMiniMatesService(TokenManager.get(context)))
    val overview = repository.overview().getOrDefault(MiniMatesOverviewDto())
    return MiniMatesWidgetState(overview, repository.level(overview.currentLevel).getOrNull())
}
