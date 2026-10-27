package com.abhinav.caisarra.presentation.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.ClubDto
import com.abhinav.caisarra.data.repository.ClubRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ClubTab {
    DISCOVER,
    MINE
}

data class ClubUiState(
    val clubs: List<ClubDto> = emptyList(),
    val myClubs: List<ClubDto> = emptyList(),
    val tab: ClubTab = ClubTab.MINE,
    val search: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class ClubViewModel(
    private val repository: ClubRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClubUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val search = _state.value.search
                val clubs = repository.getClubs(search = search)
                val mine = repository.getMyClubs()

                _state.update {
                    it.copy(
                        clubs = clubs,
                        myClubs = mine,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unable to load clubs."
                    )
                }
            }
        }
    }

    fun selectTab(tab: ClubTab) {
        _state.update {
            it.copy(tab = tab)
        }
    }

    fun search(query: String) {
        _state.update {
            it.copy(search = query)
        }
    }

    fun submitSearch() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val query = _state.value.search
                val clubs = repository.getClubs(search = query)

                _state.update {
                    it.copy(
                        clubs = clubs,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Search failed."
                    )
                }
            }
        }
    }

    fun createClub(name: String, description: String) {
        submit {
            val created = repository.createClub(name, description)
            val id = created.effectiveId

            _state.update { old ->
                old.copy(
                    myClubs = listOf(created) +
                            old.myClubs.filterNot {
                                it.effectiveId == id
                            },
                    clubs = listOf(created) +
                            old.clubs.filterNot {
                                it.effectiveId == id
                            },
                    tab = ClubTab.MINE,
                    info = "Club created successfully."
                )
            }

            try {
                val mine = repository.getMyClubs()
                val discovered = repository.getClubs(
                    search = _state.value.search
                )

                _state.update { old ->
                    val updatedMine =
                        if (mine.any { it.effectiveId == id }) {
                            mine
                        } else {
                            listOf(created) + mine.filterNot {
                                it.effectiveId == id
                            }
                        }

                    val updatedDiscover =
                        if (discovered.any { it.effectiveId == id }) {
                            discovered
                        } else {
                            listOf(created) + discovered.filterNot {
                                it.effectiveId == id
                            }
                        }

                    old.copy(
                        myClubs = updatedMine,
                        clubs = updatedDiscover
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {

            }
        }
    }

    fun joinById(id: String) {
        submit {
            repository.joinClub(id)

            reloadAfterMembershipChange(
                message = "Successfully joined club."
            )
        }
    }

    fun joinByCode(code: String) {
        submit {
            repository.joinByCode(code)

            reloadAfterMembershipChange(
                message = "Successfully joined club."
            )
        }
    }

    fun leaveClub(id: String) {
        submit {
            repository.leaveClub(id)

            _state.update { old ->
                old.copy(
                    myClubs = old.myClubs.filterNot {
                        it.effectiveId == id
                    },
                    info = "You left the club."
                )
            }

            reloadAfterMembershipChange(
                message = "You left the club.",
                removedClubId = id
            )
        }
    }

    fun deleteClub(id: String) {
        submit {
            repository.deleteClub(id)

            _state.update { old ->
                old.copy(
                    clubs = old.clubs.filterNot {
                        it.effectiveId == id
                    },
                    myClubs = old.myClubs.filterNot {
                        it.effectiveId == id
                    },
                    info = "Club deleted successfully."
                )
            }

            try {
                val mine = repository.getMyClubs()
                val clubs = repository.getClubs(
                    search = _state.value.search
                )

                _state.update { old ->
                    old.copy(
                        myClubs = mine.filterNot {
                            it.effectiveId == id
                        },
                        clubs = clubs.filterNot {
                            it.effectiveId == id
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {

            }
        }
    }

    private suspend fun reloadAfterMembershipChange(
        message: String,
        removedClubId: String? = null
    ) {
        val mine = repository.getMyClubs()
        val clubs = repository.getClubs(
            search = _state.value.search
        )

        _state.update { old ->
            old.copy(
                myClubs = if (removedClubId == null) {
                    mine
                } else {
                    mine.filterNot {
                        it.effectiveId == removedClubId
                    }
                },
                clubs = clubs,
                info = message
            )
        }
    }

    private fun submit(action: suspend () -> Unit) {
        if (_state.value.isSubmitting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                    error = null,
                    info = null
                )
            }

            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message
                            ?: "Club operation failed."
                    )
                }
            } finally {
                _state.update {
                    it.copy(isSubmitting = false)
                }
            }
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun clearInfo() {
        _state.update {
            it.copy(info = null)
        }
    }

    fun clearMessage() {
        _state.update {
            it.copy(
                error = null,
                info = null
            )
        }
    }
}