package com.example.joko.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.joko.data.remote.response.TeamMemberResponse
import com.example.joko.data.remote.response.TeamResponse
import com.example.joko.data.repository.TeamRepository
import com.google.gson.JsonParser
import kotlinx.coroutines.launch

enum class UserRoleStatus {
    NONE,
    OWNER,
    PENDING,
    APPROVED,
    REJECTED
}

class TeamViewModel(private val teamRepository: TeamRepository) : ViewModel() {

    private val _teams = MutableLiveData<List<TeamResponse>>()
    val teams: LiveData<List<TeamResponse>> = _teams

    private val _filteredTeams = MutableLiveData<List<TeamResponse>>()
    val filteredTeams: LiveData<List<TeamResponse>> = _filteredTeams

    private val _teamDetail = MutableLiveData<TeamResponse?>()
    val teamDetail: LiveData<TeamResponse?> = _teamDetail

    private val _myTeams = MutableLiveData<List<TeamResponse>>()
    val myTeams: LiveData<List<TeamResponse>> = _myTeams

    private val _myApplications = MutableLiveData<List<TeamMemberResponse>>()
    val myApplications: LiveData<List<TeamMemberResponse>> = _myApplications

    private val _joinedTeams = MutableLiveData<List<TeamMemberResponse>>()
    val joinedTeams: LiveData<List<TeamMemberResponse>> = _joinedTeams

    private val _teamMembers = MutableLiveData<List<TeamMemberResponse>>()
    val teamMembers: LiveData<List<TeamMemberResponse>> = _teamMembers

    private val _userRoleStatus = MediatorLiveData<UserRoleStatus>().apply {
        addSource(_teamDetail) { calculateUserRoleStatus() }
        addSource(_teamMembers) { calculateUserRoleStatus() }
    }
    val userRoleStatus: LiveData<UserRoleStatus> = _userRoleStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _actionSuccess = MutableLiveData<Boolean>()
    val actionSuccess: LiveData<Boolean> = _actionSuccess

    private fun calculateUserRoleStatus() {
        val currentUserId = teamRepository.getCurrentUserId()
        val team = _teamDetail.value
        val members = _teamMembers.value ?: emptyList()

        if (currentUserId == null || team == null) {
            _userRoleStatus.value = UserRoleStatus.NONE
            return
        }

        // 1. Prioritas OWNER
        if (team.ownerId == currentUserId) {
            _userRoleStatus.value = UserRoleStatus.OWNER
            return
        }

        // Cari user di dalam list member
        val userMemberEntry = members.find { it.userId == currentUserId }
        
        Log.d("TeamViewModel", "Calculating role status for user $currentUserId. Entry status: ${userMemberEntry?.status}")

        // 2. Prioritas APPROVED (Sync with DB Contract)
        if (userMemberEntry?.status == "APPROVED") {
            _userRoleStatus.value = UserRoleStatus.APPROVED
            return
        }

        // 3. Prioritas PENDING (Sync with DB Contract)
        if (userMemberEntry?.status == "PENDING") {
            _userRoleStatus.value = UserRoleStatus.PENDING
            return
        }

        // 4. Prioritas REJECTED (Sync with DB Contract)
        if (userMemberEntry?.status == "REJECTED") {
            _userRoleStatus.value = UserRoleStatus.REJECTED
            return
        }

        // 5. NONE
        _userRoleStatus.value = UserRoleStatus.NONE
    }

    fun loadTeams() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getTeams()
                _teams.postValue(result)
                _filteredTeams.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchTeams(query: String) {
        val originalList = _teams.value ?: return
        if (query.isBlank()) {
            _filteredTeams.value = originalList
            return
        }

        val filtered = originalList.filter { team ->
            val matchName = team.teamName.contains(query, ignoreCase = true)
            val matchEvent = team.eventName.contains(query, ignoreCase = true)
            val matchRole = team.roleNeed?.any { it.contains(query, ignoreCase = true) } ?: false
            val matchDesc = team.description?.contains(query, ignoreCase = true) ?: false

            matchName || matchEvent || matchRole || matchDesc
        }
        _filteredTeams.value = filtered
    }

    fun loadTeamById(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getTeamById(id)
                _teamDetail.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadMyTeams() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getMyTeams()
                _myTeams.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadMyApplications() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getUserApplications()
                _myApplications.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadJoinedTeams() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getJoinedTeams()
                _joinedTeams.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createTeam(
        name: String,
        eventName: String,
        maxCapacity: Int,
        description: String?,
        roleNeed: List<String>?,
        ownerContact: String?
    ) {
        _isLoading.value = true
        _actionSuccess.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = teamRepository.createTeam(
                    name, eventName, maxCapacity, description, roleNeed, ownerContact
                )
                if (response.isSuccessful) {
                    _actionSuccess.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue(parseError(errorBody))
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun applyToTeam(teamId: String) {
        _isLoading.value = true
        _actionSuccess.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = teamRepository.applyToTeam(teamId)
                if (response.isSuccessful) {
                    _actionSuccess.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue(parseError(errorBody))
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadTeamMembers(teamId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = teamRepository.getTeamMembers(teamId)
                _teamMembers.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateMemberStatus(memberId: String, status: String) {
        _isLoading.value = true
        _actionSuccess.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = teamRepository.updateMemberStatus(memberId, status)
                if (response.isSuccessful) {
                    _actionSuccess.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue(parseError(errorBody))
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun cancelApplication(memberId: String) {
        _isLoading.value = true
        _actionSuccess.value = false
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = teamRepository.cancelApplication(memberId)
                if (response.isSuccessful) {
                    _actionSuccess.postValue(true)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue(parseError(errorBody))
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun isTeamFull(team: TeamResponse?): Boolean {
        if (team == null) return false
        return team.currentMembersCount >= team.maxCapacity
    }

    private fun parseError(errorBody: String?): String {
        if (errorBody == null) return "Terjadi kesalahan"
        return try {
            @Suppress("DEPRECATION")
            val jsonElement = JsonParser().parse(errorBody)
            if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                val messageElement = jsonObject.get("message")
                if (messageElement != null && !messageElement.isJsonNull) {
                    messageElement.asString
                } else {
                    errorBody
                }
            } else {
                errorBody
            }
        } catch (e: Exception) {
            errorBody
        }
    }

    fun resetActionSuccess() {
        _actionSuccess.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
