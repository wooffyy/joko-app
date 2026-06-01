package com.example.joko.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.*
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.request.CreateEventRequest
import com.example.joko.data.repository.AuthRepository
import com.example.joko.data.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.abs

class EventViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val DEFAULT_BANNER_URL = "https://via.placeholder.com/180"
    }

    private val _imageByteArray = MutableLiveData<ByteArray?>()
    val imageByteArray: LiveData<ByteArray?> = _imageByteArray

    private val incrementedEventIds = mutableSetOf<String>()

    private val _filterCategory = MutableLiveData<String>("Semua")
    val currentFilter: LiveData<String> = _filterCategory

    private val _allEventsFromDb = eventRepository.allEvents.asLiveData()

    val allEvents = MediatorLiveData<List<EventEntity>>().apply {
        addSource(_allEventsFromDb) { list ->
            value = filterList(list, _filterCategory.value ?: "Semua")
        }
        addSource(_filterCategory) { category ->
            value = filterList(_allEventsFromDb.value ?: emptyList(), category)
        }
    }

    val categories: LiveData<List<String>> = _allEventsFromDb.map { list ->
        list.map { it.category.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sorted()
    }

    private fun filterList(list: List<EventEntity>, category: String): List<EventEntity> {
        return if (category == "Semua") {
            list
        } else {
            list.filter { it.category.trim().equals(category.trim(), ignoreCase = true) }
        }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _publishSuccess = MutableLiveData<Boolean>()
    val publishSuccess: LiveData<Boolean> = _publishSuccess

    private val _pfpUrl = MutableLiveData<String?>()
    val pfpUrl: LiveData<String?> = _pfpUrl

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val profile = authRepository.getUserProfile()
                _pfpUrl.postValue(profile?.pfpUrl)
            } catch (e: Exception) {
                _pfpUrl.postValue(null)
            }
        }
    }

    fun processImage(context: Context, uri: Uri) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    validateFileMetadata(context, uri)
                    validateAspectRatio(context, uri)
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
            if (size > 5 * 1024 * 1024) {
                throw Exception("Ukuran file terlalu besar. Maksimal 5MB.")
            }
        }
    }

    private fun validateAspectRatio(context: Context, uri: Uri) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true 
        }
        context.contentResolver.openInputStream(uri)?.use { 
            BitmapFactory.decodeStream(it, null, options) 
        }

        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) throw Exception("Gambar rusak atau tidak valid")

        val actualRatio = width.toDouble() / height.toDouble()
        val targetRatio = 16.0 / 9.0
        if (abs(actualRatio - targetRatio) > 0.1) {
            throw Exception("Rasio gambar harus 16:9")
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
            
            if (result.size > 5 * 1024 * 1024) {
                throw Exception("Gagal mengompres gambar di bawah 5MB")
            }
            result
        }
    }

    fun setFilter(category: String) {
        _filterCategory.value = category
    }

    fun getEventById(id: String): LiveData<EventEntity?> {
        return eventRepository.getEventById(id).asLiveData()
    }

    fun incrementClickCount(id: String) {
        if (incrementedEventIds.contains(id)) return
        incrementedEventIds.add(id)
        viewModelScope.launch {
            eventRepository.incrementEventClick(id)
        }
    }

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

    // Langkah 4.2.A — ViewModel Signature Fix
    // Parameter imageUrl dihapus agar ViewModel menjadi Single Source of Truth
    fun publishEvent(
        title: String,
        category: String,
        location: String,
        startDate: String,
        endDate: String,
        description: String,
        organizer: String,
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
        
        viewModelScope.launch {
            try {
                // Penentuan finalImageUrl dilakukan secara eksklusif di sini
                var finalImageUrl = DEFAULT_BANNER_URL
                
                val imageBytes = _imageByteArray.value
                if (imageBytes != null) {
                    try {
                        finalImageUrl = eventRepository.uploadEventBanner(ownerId, imageBytes)
                    } catch (e: Exception) {
                        _errorMessage.value = "Gagal mengunggah banner: ${e.message}"
                        _isLoading.value = false
                        return@launch 
                    }
                }

                // Menggunakan parameter camelCase sesuai dengan data class CreateEventRequest
                val request = CreateEventRequest(
                    title = title,
                    category = category,
                    location = location,
                    startDate = startDate,
                    endDate = endDate,
                    description = description,
                    organizer = organizer,
                    imageUrl = finalImageUrl,
                    registrationUrl = registrationUrl,
                    requirements = requirements,
                    tags = tags,
                    ownerId = ownerId
                )

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
