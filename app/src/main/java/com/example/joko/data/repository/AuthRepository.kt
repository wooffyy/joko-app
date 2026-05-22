package com.example.joko.data.repository

import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.AuthRequest
import com.example.joko.data.remote.response.AuthResponse
import com.example.joko.utils.SessionManager

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun signUp(email: String, password: String): AuthResponse {
        val request = AuthRequest(email, password)
        return apiService.signUp(request)
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        val request = AuthRequest(email, password)
        val response = apiService.signIn(request)
        sessionManager.saveAuthToken(response.accessToken)
        sessionManager.saveRefreshToken(response.refreshToken)
        return response
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
