package com.example.joko.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.*
import com.example.joko.data.local.entity.BookmarkEventEntity
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
        private const val MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024 // 2MB limit
    }

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified: LiveData<Boolean> = _isVerified

    private val _imageByteArray = MutableLiveData<ByteArray?>()
    val imageByteArray: LiveData<ByteArray?> = _imageByteArray

    private val _filterCategory = MutableLiveData<String>("Semua")
    val currentFilter: LiveData<String> = _filterCategory

    private val _searchQuery = MutableLiveData<String>("")
    
    private val _allEventsFromDb = eventRepository.allEvents.asLiveData()

    val allEvents = MediatorLiveData<List<EventEntity>>().apply {
        addSource(_allEventsFromDb) { list ->
            value = filterList(list, _filterCategory.value ?: "Semua", _searchQuery.value ?: "")
        }
        addSource(_filterCategory) { category ->
            value = filterList(_allEventsFromDb.value ?: emptyList(), category, _searchQuery.value ?: "")
        }
        addSource(_searchQuery) { query ->
            value = filterList(_allEventsFromDb.value ?: emptyList(), _filterCategory.value ?: "Semua", query)
        }
    }

    val categories: LiveData<List<String>> = _allEventsFromDb.map { list ->
        list.map { it.category.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sorted()
    }

    private fun filterList(list: List<EventEntity>, category: String, query: String): List<EventEntity> {
        val trimmedQuery = query.trim()
        
        // 1. Filter berdasarkan kategori (AND logic)
        val categoryFiltered = if (category == "Semua") {
            list
        } else {
            list.filter { it.category.trim().equals(category.trim(), ignoreCase = true) }
        }

        // 2. Filter berdasarkan search query (OR logic multi-field)
        return if (trimmedQuery.isEmpty()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { event ->
                event.title.contains(trimmedQuery, ignoreCase = true) || 
                event.organizer.contains(trimmedQuery, ignoreCase = true) ||
                event.category.contains(trimmedQuery, ignoreCase = true) ||
                event.location.contains(trimmedQuery, ignoreCase = true) ||
                (event.description.contains(trimmedQuery, ignoreCase = true)) ||
                (event.tags?.contains(trimmedQuery, ignoreCase = true) == true)
            }
        }
    }

    fun setFilter(category: String) {
        _filterCategory.value = category
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    fun getEventById(id: String): LiveData<EventEntity?> {
        return eventRepository.getEventById(id).asLiveData()
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
                _isVerified.postValue(profile?.isVerified ?: false)
                _pfpUrl.postValue(profile?.pfpUrl)
            } catch (e: Exception) {
                _isVerified.postValue(false)
                _pfpUrl.postValue(null)
            }
        }
    }

    fun isBookmarked(id: String): LiveData<Boolean> {
        return eventRepository.isBookmarked(id).asLiveData()
    }

    fun toggleBookmark(event: EventEntity, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                val bookmark = BookmarkEventEntity(
                    id = event.id,
                    title = event.title,
                    organizer = event.organizer,
                    category = event.category,
                    location = event.location,
                    startDate = event.startDate,
                    endDate = event.endDate,
                    description = event.description,
                    imageUrl = event.imageUrl,
                    registrationUrl = event.registrationUrl,
                    tags = event.tags,
                    requirements = event.requirements,
                    ownerId = event.ownerId,
                    isVerified = event.isVerified,
                    trustScore = event.trustScore
                )
                eventRepository.removeBookmark(bookmark)
            } else {
                eventRepository.addBookmark(event)
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

    fun resetImageByteArray() {
        _imageByteArray.value = null
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
            if (size > MAX_IMAGE_SIZE_BYTES) {
                throw Exception("Ukuran file terlalu besar. Maksimal 2MB.")
            }
        }
    }

    private suspend fun compressImage(context: Context, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) 
                ?: throw Exception("Tidak bisa membaca file")
            
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: throw Exception("Gagal mendekode gambar")

            val width = originalBitmap.width
            val height = originalBitmap.height
            
            // Calculate 16:9 Center Crop
            val targetRatio = 16.0 / 9.0
            val currentRatio = width.toDouble() / height.toDouble()
            
            val cropWidth: Int
            val cropHeight: Int
            if (currentRatio > targetRatio) {
                // Image is wider than 16:9, crop width
                cropHeight = height
                cropWidth = (height * targetRatio).toInt()
            } else {
                // Image is taller than 16:9, crop height
                cropWidth = width
                cropHeight = (width / targetRatio).toInt()
            }
            
            val xOffset = (width - cropWidth) / 2
            val yOffset = (height - cropHeight) / 2
            
            val croppedBitmap = Bitmap.createBitmap(originalBitmap, xOffset, yOffset, cropWidth, cropHeight)
            
            // Resize to standard HD (1280x720) for efficiency and memory safety
            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 1280, 720, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            val result = outputStream.toByteArray()
            
            // Cleanup
            originalBitmap.recycle()
            if (croppedBitmap != originalBitmap) croppedBitmap.recycle()
            scaledBitmap.recycle()
            
            if (result.size > MAX_IMAGE_SIZE_BYTES) {
                throw Exception("Gagal mengompres gambar di bawah 2MB")
            }
            result
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
        registrationUrl: String?,
        requirements: List<String>?,
        tags: List<String>?
    ) {
        val ownerId = authRepository.getUserId()
        if (ownerId == null) {
            _errorMessage.value = "Sesi berakhir. Silakan login kembali."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _publishSuccess.value = false
        
        viewModelScope.launch {
            try {
                val profile = authRepository.getUserProfile()
                val isVerifiedStatus = profile?.isVerified ?: false

                var finalImageUrl = DEFAULT_BANNER_URL
                val imageBytes = _imageByteArray.value
                
                if (imageBytes != null) {
                    finalImageUrl = eventRepository.uploadEventBanner(ownerId, imageBytes)
                }

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
                    ownerId = ownerId,
                    isVerified = isVerifiedStatus
                )

                val response = eventRepository.publishEvent(request)
                if (response.isSuccessful) {
                    _publishSuccess.value = true
                    eventRepository.refreshEvents()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Gagal mempublikasikan event: $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan saat memproses event"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
