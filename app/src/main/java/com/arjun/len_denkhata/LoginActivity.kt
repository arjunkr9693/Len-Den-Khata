package com.arjun.len_denkhata

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.arjun.len_denkhata.data.repository.LoginRepository
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.ui.screens.login.LoginScreen
import com.arjun.len_denkhata.ui.theme.Len_DenKhataTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    @Inject
    lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = loginRepository.getUser()
        if (user != null) {
            UserSession.login(user)
            // Already logged in, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setContent {
            Len_DenKhataTheme {
                LoginScreen(navController = rememberNavController())
            }
        }
    }
}
