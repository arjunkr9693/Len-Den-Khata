package com.arjun.len_denkhata


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arjun.len_denkhata.data.repository.LoginRepository
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.ui.components.BottomNavigationBar
import com.arjun.len_denkhata.ui.theme.Len_DenKhataTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Len_DenKhataTheme {
                var startDestination: String = Screen.Customer.route
                val user = loginRepository.getUser()
                if (user != null) {
                    UserSession.initialize(user)
                    LenDenKhataApp(startDestination)
                } else {
                    startDestination = Screen.Login.route
                    LenDenKhataApp(startDestination)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("testTag", "Main onPauseActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("testTag", "Main onDestroyActivity")
    }
}


@Composable
fun LenDenKhataApp(startDestination: String) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
                BottomNavigationBar(navController = navController)
            }
        }

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController, startDestination)
        }
    }
}