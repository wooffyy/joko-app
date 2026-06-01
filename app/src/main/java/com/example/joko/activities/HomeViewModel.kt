package com.example.joko.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.repository.AuthRepository
import com.example.joko.data.repository.EventRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified: LiveData<Boolean> = _isVerified

    // Mengambil nama user dari session
    val userName: String? get() = authRepository.getUserName()

    // Mengambil 3 event terbaru saja untuk ditampilkan di Home
    val latestEvents: LiveData<List<EventEntity>> = eventRepository.allEvents
        .map { list -> list.take(3) }
        .asLiveData()

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val profile = authRepository.getUserProfile()
                _isVerified.postValue(profile?.isVerified ?: false)
            } catch (e: Exception) {
                _isVerified.postValue(false)
            }
        }
    }

    fun fetchLatestEvents() {
        // Memicu sinkronisasi data dari repository
        // Karena allEvents di repository bersifat reactive, 
        // latestEvents akan otomatis terupdate jika data di DB berubah.
    }
}
