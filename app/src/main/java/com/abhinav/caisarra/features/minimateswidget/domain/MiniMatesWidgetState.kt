package com.abhinav.caisarra.features.minimateswidget.domain

import com.abhinav.caisarra.features.minimates.data.MiniMatesLevelDto
import com.abhinav.caisarra.features.minimates.data.MiniMatesOverviewDto

data class MiniMatesWidgetState(val overview: MiniMatesOverviewDto, val level: MiniMatesLevelDto?)
