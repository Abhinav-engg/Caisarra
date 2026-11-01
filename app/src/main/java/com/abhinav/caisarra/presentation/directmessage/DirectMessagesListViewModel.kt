package com.abhinav.caisarra.presentation.directmessage

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.DirectConversationDto
import com.abhinav.caisarra.data.remote.dto.DirectPlayerDto
import com.abhinav.caisarra.data.repository.DirectMessageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DirectMessagesListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DirectMessageRepository(application)

    var conversations by mutableStateOf<List<DirectConversationDto>>(emptyList())
        private set
    var searchResults by mutableStateOf<List<DirectPlayerDto>>(emptyList())
        private set
    var query by mutableStateOf("")
        private set
    var loadingConversations by mutableStateOf(true)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var searching by mutableStateOf(false)
        private set
    var searchPerformed by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadConversations()
    }

    fun onQueryChange(value: String) {
        query = value
        error = null
    }

    fun loadConversations() {
        viewModelScope.launch {
            loadingConversations = true
            error = null
            try {
                conversations = repository.getConversations()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = friendlyError(e, "Unable to load conversations.")
            } finally {
                loadingConversations = false
            }
        }
    }

    fun refreshConversations() {
        viewModelScope.launch {
            isRefreshing = true
            error = null
            try {
                if (searchPerformed && query.isNotBlank()) {
                    searchResults = repository.searchPlayers(query.trim())
                }
                conversations = repository.getConversations()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = friendlyError(e, "Unable to refresh conversations.")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun searchPlayers() {
        val searchQuery = query.trim()

        if (searchQuery.isBlank() || (searchQuery.length < 2 && !searchQuery.all { it.isDigit() })) {
            error = "Enter at least two characters or a numeric user ID."
            return
        }

        viewModelScope.launch {
            searching = true
            searchPerformed = true
            error = null
            try {
                searchResults = repository.searchPlayers(searchQuery)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                searchResults = emptyList()
                error = friendlyError(e, "Unable to search players.")
            } finally {
                searching = false
            }
        }
    }

    private fun friendlyError(e: Exception, fallback: String): String {
        if (e is IOException) {
            return "No internet connection. Please try again."
        }
        if (e is HttpException) {
            if (e.code() == 401) return "Your session has expired. Please sign in again."
            if (e.code() == 403) return "You don't have access to this."
            if (e.code() == 404) return "Nothing found."
            if (e.code() >= 500) return "Server error. Please try again in a moment."
        }
        return fallback
    }
}
