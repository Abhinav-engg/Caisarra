package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryResponse
import com.abhinav.caisarra.data.remote.dto.MoveAnalysisDto
import com.abhinav.caisarra.data.repository.GameAnalysisRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AnalysisPhase { ANALYSING, READY, FAILED }

data class GameAnalysisUiState(
    val phase: AnalysisPhase = AnalysisPhase.ANALYSING,
    val summary: AnalysisSummaryResponse? = null,
    val moves: List<MoveAnalysisDto> = emptyList(),
    val selectedMove: Int? = null,
    val errorMessage: String? = null
)

class GameAnalysisViewModel(
    private val repository: GameAnalysisRepository,
    private val gameId: String
) : ViewModel() {

    private val _state = MutableStateFlow(GameAnalysisUiState())
    val state: StateFlow<GameAnalysisUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.summary(gameId).getOrNull() == null) {
                repository.requestAnalysis(gameId).onFailure {
                    fail(it.message)
                    return@launch
                }
            }
            poll()
        }
    }

    fun retry() {
        _state.value = GameAnalysisUiState()
        viewModelScope.launch {
            repository.retry(gameId).onFailure {
                fail(it.message)
                return@launch
            }
            poll()
        }
    }

    fun selectMove(moveNumber: Int) {
        _state.update {
            it.copy(selectedMove = if (it.selectedMove == moveNumber) null else moveNumber)
        }
    }

    private suspend fun poll() {
        repeat(MAX_POLLS) {
            val summary = repository.summary(gameId).getOrNull()
            when (summary?.analysis?.status) {
                "completed" -> {
                    val moves = repository.moves(gameId).getOrNull().orEmpty()
                    _state.value = GameAnalysisUiState(
                        phase = AnalysisPhase.READY,
                        summary = summary,
                        moves = moves
                    )
                    return
                }
                "failed" -> {
                    fail("Analysis failed. Please try again.")
                    return
                }
            }
            delay(POLL_INTERVAL_MS)
        }
        fail("Analysis is taking too long. Please try again.")
    }

    private fun fail(message: String?) {
        _state.value = GameAnalysisUiState(
            phase = AnalysisPhase.FAILED,
            errorMessage = message ?: "Something went wrong. Please try again."
        )
    }

    private companion object {
        const val MAX_POLLS = 60
        const val POLL_INTERVAL_MS = 2000L
    }
}