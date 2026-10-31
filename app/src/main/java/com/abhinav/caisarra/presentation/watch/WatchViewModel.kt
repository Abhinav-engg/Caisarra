package com.abhinav.caisarra.presentation.watch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WatchViewModel : ViewModel() {

    private val _codeQuery = MutableStateFlow("")
    val codeQuery: StateFlow<String> = _codeQuery.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigateToWatchCode = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToWatchCode: SharedFlow<String> = _navigateToWatchCode

    fun onCodeQueryChange(code: String) {
        _codeQuery.value = code
        _errorMessage.value = null
    }

    fun submitCodeOrLink() {
        val input = _codeQuery.value.trim()
        if (input.isEmpty()) return

        val extractedCode = extractWatchCode(input)
        if (extractedCode.isNotEmpty()) {
            _errorMessage.value = null
            _navigateToWatchCode.tryEmit(extractedCode)
        } else {
            _errorMessage.value = "Invalid watch code or link format."
        }
    }

    private fun extractWatchCode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.contains("/")) {
            return trimmed.substringAfterLast("/").trim()
        }
        return trimmed
    }
}
