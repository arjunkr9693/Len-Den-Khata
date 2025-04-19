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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _illegalPhoneNumberError = MutableStateFlow("")
    val illegalPhoneNumberError: StateFlow<String> = _illegalPhoneNumberError

    fun login(mobileNumber: String, navController: NavHostController) {
        if (!isValidPhoneNumber(mobileNumber)) {
            return
        }
        viewModelScope.launch {
            val loggedIn = loginRepository.login(mobileNumber)
            if (loggedIn != null) {
                navController.navigate("initial_data_loader")
            } else {
                _illegalPhoneNumberError.value = "Login failed. Please check your number and try again."
            }
        }
    }

    fun clearIllegalPhoneNumberError() {
        _illegalPhoneNumberError.value = ""
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val cleanedNumber = phoneNumber.replace(Regex("[^\\d+]"), "")

        return if (cleanedNumber.length <= 10) {
            _illegalPhoneNumberError.value = "Phone number must be at least 10 digits."
            false
        } else {
            // If length is more than 10, we consider it potentially valid for login
            // The logic to separate country code and local number happens later in addCustomer
            true
        }
    }
}