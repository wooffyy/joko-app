package com.example.joko.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.joko.data.remote.request.UpdateProfileRequest
import com.example.joko.data.remote.response.ProfileResponse
import com.example.joko.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    private val _userProfile = MutableLiveData<ProfileResponse?>()
    val userProfile: LiveData<ProfileResponse?> = _userProfile

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _imageByteArray = MutableLiveData<ByteArray?>()
    val imageByteArray: LiveData<ByteArray?> = _imageByteArray

    fun processImage(context: Context, uri: Uri) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    validateFileMetadata(context, uri)
                    compressImage(context, uri)
                }
                _imageByteArray.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memproses gambar"
                _imageByteArray.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateFileMetadata(context: Context, uri: Uri) {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        if (type != "image/jpeg" && type != "image/png") {
            throw Exception("Format file tidak didukung. Gunakan JPG atau PNG.")
        }

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val size = cursor.getLong(sizeIndex)
            if (size > 1 * 1024 * 1024) {
                throw Exception("Ukuran file terlalu besar. Maksimal 1MB.")
            }
        }
    }

    private suspend fun compressImage(context: Context, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) 
                ?: throw Exception("Tidak bisa membaca file")
            
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2 
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                ?: throw Exception("Gagal mendekode gambar")

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            val result = outputStream.toByteArray()
            bitmap.recycle()
            
            result
        }
    }

    fun resetImageByteArray() {
        _imageByteArray.value = null
    }

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

    fun register(
        email: String,
        password: String,
        name: String?,
        university: String?,
        interests: List<String>?,
        skills: List<String>?,
        portfolioLink: String?
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                repository.signUp(email, password, name, university, interests, skills, portfolioLink)
                _authSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Registrasi gagal, silakan coba lagi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun getUserProfile(){
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile()
                _userProfile.value = profile
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat profil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        name: String,
        university: String,
        bio: String,
        portfolioLink: String,
        email: String,
        linkedin: String,
        skills: List<String>
    ) {
        val userId = repository.getUserId()
        if (userId == null) {
            _errorMessage.value = "Sesi telah berakhir, silakan login kembali"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                var finalPfpUrl = _userProfile.value?.pfpUrl
                
                val imageBytes = _imageByteArray.value
                if (imageBytes != null) {
                    try {
                        finalPfpUrl = repository.uploadProfileImage(userId, imageBytes)
                    } catch (e: Exception) {
                        _errorMessage.value = "Gagal mengunggah foto profil: ${e.message}"
                        _isLoading.value = false
                        return@launch
                    }
                }

                val request = UpdateProfileRequest(
                    name = name,
                    university = university,
                    bio = bio,
                    portfolioLink = portfolioLink,
                    email = email,
                    linkedin = linkedin,
                    skills = skills,
                    pfpUrl = finalPfpUrl
                )

                val success = repository.updateProfile(request)
                _updateSuccess.value = success
                if (!success) {
                    _errorMessage.value = "Gagal memperbarui profil"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _isSessionValid = MutableLiveData<Boolean>()
    val isSessionValid: LiveData<Boolean> = _isSessionValid

    fun checkSession() {
        if (!repository.isLoggedIn()) {
            _isSessionValid.value = false
            return
        }

        viewModelScope.launch {
            val isValid = repository.validateSession()
            _isSessionValid.value = isValid
        }
    }
}
