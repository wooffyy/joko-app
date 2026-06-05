package com.example.joko.data.repository

import android.util.Log
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.MemberActionRequest
import com.example.joko.data.remote.request.TeamRequest
import com.example.joko.data.remote.response.TeamMemberResponse
import com.example.joko.data.remote.response.TeamResponse
import com.example.joko.utils.SessionManager
import retrofit2.Response

class TeamRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private val TAG = "TeamRepository"

    fun getCurrentUserId(): String? = sessionManager.getUserId()

    suspend fun getTeams(): List<TeamResponse> {
        return try {
            // Endpoint ini sekarang menggunakan view view_teams_with_member_count
            apiService.getTeams()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching teams: ${e.message}")
            throw e
        }
    }

    suspend fun getTeamById(id: String): TeamResponse? {
        return try {
            // Menggunakan view yang sama untuk mendapatkan current_members_count yang akurat
            apiService.getTeams().find { it.id == id }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching team by id: ${e.message}")
            throw e
        }
    }

    suspend fun getMyTeams(): List<TeamResponse> {
        val userId = sessionManager.getUserId() ?: throw Exception("User not logged in")
        return try {
            apiService.getTeams(ownerId = "eq.$userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching my teams: ${e.message}")
            throw e
        }
    }

    suspend fun createTeam(
        name: String,
        eventName: String,
        maxCapacity: Int,
        description: String?,
        roleNeed: List<String>?,
        ownerContact: String?
    ): Response<Unit> {
        val userId = sessionManager.getUserId() ?: throw Exception("User not logged in")
        val request = TeamRequest(
            teamName = name,
            eventName = eventName,
            ownerId = userId,
            maxCapacity = maxCapacity,
            description = description,
            roleNeed = roleNeed,
            ownerContact = ownerContact
        )

        return try {
            val response = apiService.createTeam(request = request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Create team failed: $errorBody")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating team: ${e.message}")
            throw e
        }
    }

    /**
     * Menggunakan RPC apply_to_team.
     * Validasi (duplicate, owner, capacity) dilakukan sepenuhnya di sisi database.
     */
    suspend fun applyToTeam(teamId: String): Response<Unit> {
        val request = mapOf("p_team_id" to teamId)

        return try {
            val response = apiService.applyToTeam(request = request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Apply to team failed: $errorBody")
                // Melempar error message dari backend agar bisa ditangkap ViewModel
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception applying to team: ${e.message}")
            throw e
        }
    }

    suspend fun getTeamMembers(teamId: String): List<TeamMemberResponse> {
        return try {
            apiService.getTeamMembers(teamId = "eq.$teamId")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching team members: ${e.message}")
            throw e
        }
    }

    suspend fun getUserApplications(): List<TeamMemberResponse> {
        val userId = sessionManager.getUserId() ?: throw Exception("User not logged in")
        return try {
            apiService.getUserApplications(userId = userId, status = "pending")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user applications: ${e.message}")
            throw e
        }
    }

    /**
     * Refactored untuk mendukung accept flow melalui RPC accept_applicant.
     */
    suspend fun updateMemberStatus(memberId: String, status: String): Response<Unit> {
        return try {
            val response = if (status == "accepted") {
                acceptApplicant(memberId)
            } else {
                val request = MemberActionRequest(status = status)
                apiService.updateMemberStatus(memberId = "eq.$memberId", request = request)
            }
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Update member status failed: $errorBody")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating member status: ${e.message}")
            throw e
        }
    }

    /**
     * Menggunakan RPC accept_applicant.
     */
    suspend fun acceptApplicant(applicationId: String): Response<Unit> {
        val request = mapOf("p_application_id" to applicationId)
        return try {
            val response = apiService.acceptApplicant(request = request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Accept applicant failed: $errorBody")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception accepting applicant: ${e.message}")
            throw e
        }
    }

    suspend fun cancelApplication(memberId: String): Response<Unit> {
        return try {
            val response = apiService.cancelApplication(memberId = "eq.$memberId")
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Cancel application failed: $errorBody")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Exception cancelling application: ${e.message}")
            throw e
        }
    }
}
