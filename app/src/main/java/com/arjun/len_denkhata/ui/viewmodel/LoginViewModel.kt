package com.arjun.len_denkhata.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.MainActivity
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun login(mobileNumber: String, navController: NavHostController) {
        viewModelScope.launch {
            val loggedIn = loginRepository.login(mobileNumber)
            if (loggedIn != null) {
                navController.navigate("initial_data_loader")
            } else {
                // Handle error (e.g., show a toast)
            }
        }
    }
}