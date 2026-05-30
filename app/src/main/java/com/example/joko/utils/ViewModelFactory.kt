package com.example.joko.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.joko.activities.AuthViewModel
import com.example.joko.activities.EventViewModel
import com.example.joko.activities.HomeViewModel
import com.example.joko.di.Injection

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(Injection.provideAuthRepository(context)) as T
            }
            modelClass.isAssignableFrom(EventViewModel::class.java) -> {
                EventViewModel(Injection.provideRepository(context), Injection.provideAuthRepository(context)) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(Injection.provideAuthRepository(context), Injection.provideRepository(context)) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
