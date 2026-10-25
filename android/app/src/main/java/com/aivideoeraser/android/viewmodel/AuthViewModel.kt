package com.aivideoeraser.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivideoeraser.android.data.model.AuthResponse
import com.aivideoeraser.android.data.model.LoginRequest
import com.aivideoeraser.android.data.model.RegisterRequest
import com.aivideoeraser.android.data.model.UserProfileResponse
import com.aivideoeraser.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfileResponse?>(null)
    val profile: StateFlow<UserProfileResponse?> = _profile.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(LoginRequest(email, pass))
            result.onSuccess {
                _authState.value = AuthState.Authenticated(it)
                fetchProfile()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Authentication failed")
            }
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(RegisterRequest(email, pass, name))
            result.onSuccess {
                _authState.value = AuthState.Authenticated(it)
                fetchProfile()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            val res = authRepository.getProfile()
            res.onSuccess { _profile.value = it }
        }
    }
}
