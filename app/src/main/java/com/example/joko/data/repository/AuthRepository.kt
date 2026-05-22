package com.example.joko.data.repository

import android.util.Log
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.AuthRequest
import com.example.joko.data.remote.response.AuthResponse
import com.example.joko.utils.SessionManager
import org.json.JSONObject
import retrofit2.HttpException

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private val TAG = "AuthRepository"

    suspend fun signUp(email: String, password: String): AuthResponse {
        val request = AuthRequest(email, password)
        return apiService.signUp(request)
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        try {
            val request = AuthRequest(email, password)
            Log.d(TAG, "Attempting login for: $email")
            
            val response = apiService.signIn(request)
            
            sessionManager.saveAuthToken(response.accessToken)
            sessionManager.saveRefreshToken(response.refreshToken)
            
            Log.d(TAG, "Login successful for: $email")
            return response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} Error: $errorBody")
            
            // Ekstrak pesan error dari JSON Supabase
            val errorMessage = try {
                val json = JSONObject(errorBody ?: "")
                json.optString("error_description", json.optString("msg", "Login gagal"))
            } catch (jsonEx: Exception) {
                "Error ${e.code()}: Bad Request"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Error: ${e.message}")
            throw e
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
