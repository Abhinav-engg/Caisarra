package com.abhinav.caisarra.data.repository

sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}