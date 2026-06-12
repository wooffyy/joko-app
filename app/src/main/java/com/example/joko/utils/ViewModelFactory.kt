package com.example.joko.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.joko.activities.*
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
            modelClass.isAssignableFrom(TeamViewModel::class.java) -> {
                TeamViewModel(Injection.provideTeamRepository(context)) as T
            }
            modelClass.isAssignableFrom(BookmarkViewModel::class.java) -> {
                BookmarkViewModel(
                    Injection.provideRepository(context),
                    Injection.provideTeamRepository(context)
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
