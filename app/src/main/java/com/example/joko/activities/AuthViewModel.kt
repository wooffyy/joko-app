package com.example.joko.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.joko.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    fun login(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                repository.signIn(email, password)
                _authSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login gagal, silakan coba lagi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                repository.signUp(email, password)
                _authSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registrasi gagal, silakan coba lagi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()
}
