package com.arjun.len_denkhata.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object InProgress : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(mobileNumber: String, navController: NavHostController) {
        if (!isValidPhoneNumber(mobileNumber)) {
            return
        }
        _uiState.update { LoginUiState.InProgress }
        viewModelScope.launch {
            val loggedIn = loginRepository.login(mobileNumber)
            if (loggedIn != null) {
                _uiState.update { LoginUiState.Success }
                navController.navigate("initial_data_loader") {
                    popUpTo("login_screen") { inclusive = true }
                }
            } else {
                _uiState.update { LoginUiState.Error("Login failed. Please check your internet.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { LoginUiState.Idle }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val countryCode = phoneNumber.removeSuffix(phoneNumber.takeLast(10))
        return if (countryCode.length < 2) {
            _uiState.update { LoginUiState.Error("Phone number must be at least 10 digits with country code.") }
            false
        } else {
            // If length is more than 10, we consider it potentially valid for login
            // The logic to separate country code and local number happens later in addCustomer
            true
        }
    }
}