package com.example.joko.data.repository

import android.R
import android.util.Log
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.AuthRequest
import com.example.joko.data.remote.response.AuthResponse
import com.example.joko.data.remote.response.ProfileResponse
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
        skills: List<String>?,
        portfolioLink: String?
    ): AuthResponse {
        try {
            val metadata = mapOf(
                "full_name" to name,
                "university" to university,
                "interests" to interests,
                "portfolio_link" to portfolioLink,
                "skills" to skills
            )

            val authRequest = AuthRequest(
                email = email,
                password = password,
                data = metadata
            )
            
            return apiService.signUp(authRequest)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val json = JSONObject(errorBody ?: "")
                json.optString("error_description", json.optString("msg", "Registration failed"))
            } catch (jsonEx: Exception) {
                "Error ${e.code()}: Bad Request"
            }
            throw Exception(errorMessage)
        } catch (e: Exception){
            throw e
        }
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        try {
            val request = AuthRequest(email, password)
            val response = apiService.signIn(request)
            
            val accessToken = response.accessToken
            val refreshToken = response.refreshToken
            val userId = response.userId
            val fullName = response.fullName

            if (accessToken != null && refreshToken != null && userId != null) {
                sessionManager.saveAuthToken(accessToken)
                sessionManager.saveRefreshToken(refreshToken)
                sessionManager.saveUserId(userId)
                fullName?.let { sessionManager.saveUserName(it) }
                Log.d(TAG, "Login successful: UserID $userId saved")
            } else {
                throw Exception("Login failed: Session data missing")
            }
            
            return response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                val json = JSONObject(errorBody ?: "")
                json.optString("error_description", json.optString("msg", "Login gagal"))
            } catch (jsonEx: Exception) {
                "Error ${e.code()}: Bad Request"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserProfile(): ProfileResponse? {
        val userId = getUserId() ?: return null
        val response = apiService.getUserProfile("eq.$userId")
        return response.firstOrNull()
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    
    fun getUserId(): String? = sessionManager.getUserId()

    fun getUserName(): String? = sessionManager.getUserName()

    suspend fun validateSession(): Boolean {
        return try {
            apiService.getCurrentUser()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Session validation failed: ${e.message}")
            false
        }
    }
}
