package com.abhinav.caisarra.presentation.puzzles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PuzzlesHubUiState(
    val isLoading: Boolean = true,
    val streak: Int = 0,
    val puzzleRating: Int = 0,
    val rushHighScore: Int = 24,
    val isSolved: Boolean = false,
    val attemptsLeft: Int = 3,
    val error: String? = null
)

class PuzzlesHubViewModel(
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PuzzlesHubUiState())
    val state = _state.asStateFlow()

    init {
        loadHubData()
    }

    fun loadHubData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            puzzleRepository.getDailyPuzzle()
                .onSuccess { daily ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        streak = daily.currentStreak,
                        puzzleRating = daily.rating,
                        isSolved = daily.solved,
                        attemptsLeft = daily.attemptsLeft
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = err.message
                    )
                }

            puzzleRepository.getRushStats()
                .onSuccess { stats ->
                    if (stats.bestScore3min > 0) {
                        _state.value = _state.value.copy(
                            rushHighScore = stats.bestScore3min
                        )
                    }
                }
        }
    }
}
