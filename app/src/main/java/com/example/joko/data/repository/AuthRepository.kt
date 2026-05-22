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

    suspend fun signUp(
        email: String,
        password: String,
        name: String?,
        university: String?,
        interests: List<String>?,
        portfolioLink: String?
    ): AuthResponse {
        try {
            // Kita bungkus data profil ke dalam metadata agar tersimpan di auth.users
            // Metadata ini akan dibaca oleh Database Trigger di Supabase
            val metadata = mapOf(
                "full_name" to name,
                "university" to university,
                "interests" to interests,
                "portfolio_links" to portfolioLink
            )

            val authRequest = AuthRequest(
                email = email,
                password = password,
                data = metadata
            )
            
            val authResponse = apiService.signUp(authRequest)
            
            Log.d(TAG, "Registration request sent successfully. Check email for confirmation.")
            return authResponse

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} Error: $errorBody")
            val errorMessage = try {
                val json = JSONObject(errorBody ?: "")
                json.optString("error_description", json.optString("msg", "Registration failed"))
            } catch (jsonEx: Exception) {
                "Error ${e.code()}: Bad Request"
            }
            throw Exception(errorMessage)
        } catch (e: Exception){
            Log.e(TAG, "Unexpected Error: ${e.message}")
            throw e
        }
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        try {
            val request = AuthRequest(email, password)
            Log.d(TAG, "Attempting login for: $email")
            
            val response = apiService.signIn(request)
            
            val accessToken = response.accessToken
            val refreshToken = response.refreshToken

            if (accessToken != null && refreshToken != null) {
                sessionManager.saveAuthToken(accessToken)
                sessionManager.saveRefreshToken(refreshToken)
                Log.d(TAG, "Login successful for: $email")
            } else {
                throw Exception("Login failed: Session data missing")
            }
            
            return response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP ${e.code()} Error: $errorBody")
            
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
