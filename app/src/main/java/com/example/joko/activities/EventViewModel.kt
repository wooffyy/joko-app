package com.example.joko.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.request.CreateEventRequest
import com.example.joko.data.repository.AuthRepository
import com.example.joko.data.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val allEvents: LiveData<List<EventEntity>> = eventRepository.allEvents.asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _publishSuccess = MutableLiveData<Boolean>()
    val publishSuccess: LiveData<Boolean> = _publishSuccess

    fun fetchEvents() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                eventRepository.refreshEvents()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memperbarui data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun publishEvent(
        title: String,
        category: String,
        location: String,
        startDate: String,
        endDate: String,
        description: String,
        organizer: String,
        imageUrl: String,
        registrationUrl: String?,
        requirements: List<String>?,
        tags: List<String>?
    ) {
        val ownerId = authRepository.getUserId()
        if (ownerId == null) {
            _errorMessage.value = "User session expired. Please login again."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        val request = CreateEventRequest(
            title = title,
            category = category,
            location = location,
            startDate = startDate,
            endDate = endDate,
            description = description,
            organizer = organizer,
            imageUrl = imageUrl,
            registrationUrl = registrationUrl,
            requirements = requirements,
            tags = tags,
            ownerId = ownerId
        )

        viewModelScope.launch {
            try {
                val response = eventRepository.publishEvent(request)
                if (response.isSuccessful) {
                    _publishSuccess.value = true
                    eventRepository.refreshEvents()
                } else {
                    _errorMessage.value = "Failed to publish event"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
