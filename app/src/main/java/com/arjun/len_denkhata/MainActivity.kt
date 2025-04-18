package com.arjun.len_denkhata


import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arjun.len_denkhata.data.repository.LoginRepository
import com.arjun.len_denkhata.data.utils.DailyTransactionReminderReceiver
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.ui.components.BottomNavigationBar
import com.arjun.len_denkhata.ui.theme.Len_DenKhataTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    private val requestNotificationPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//            if (isGranted) {
//                Log.d("Notifications", "Notification permission granted")
//                scheduleDebugReminder() // Schedule debug reminder
//            } else {
//                Log.d("Notifications", "Notification permission denied")
//                // Optionally explain to the user why the permission is needed
//            }
//        }

    private val requestContactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("ContactsPermission", "READ_CONTACTS permission granted in MainActivity")
                // You can optionally do something here if needed immediately after granting
            } else {
                Log.d("ContactsPermission", "READ_CONTACTS permission denied in MainActivity")
                // Optionally handle the denial in MainActivity
            }
        }

    @Inject
    lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    android.Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            } else {
//                scheduleDebugReminder() // Schedule debug reminder
//            }
//        } else {
//            scheduleDebugReminder() // Schedule debug reminder
//        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            Log.d("ContactsPermission", "READ_CONTACTS permission already granted in MainActivity")
        }


        enableEdgeToEdge()
        setContent {
            Len_DenKhataTheme {

                var startDestination: String = Screen.Customer.route
                val user = loginRepository.getUser()

                val isInitialDataDownloaded = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getBoolean("initialDataDownloaded", false)

                if (user != null) {
                    UserSession.initialize(user)
                    if(!isInitialDataDownloaded) {
                        startDestination = "initial_data_loader"
                    }
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
//    private fun scheduleDebugReminder() {
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val reminderIntent = Intent(this, DailyTransactionReminderReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(this, 100, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 9)   // 9 AM
//            set(Calendar.MINUTE, 46)  // 45 minutes
//            set(Calendar.SECOND, 0)
//            if (timeInMillis <= System.currentTimeMillis()) {
//                add(Calendar.DAY_OF_YEAR, 1) // Set for tomorrow if it's past 9:45 AM today
//            }
//        }
//
//        alarmManager.setExactAndAllowWhileIdle( // Use setExactAndAllowWhileIdle for more reliable triggering
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            pendingIntent
//        )
//
//        Log.d("Notifications", "Debug reminder scheduled for 9:45 AM")
//    }
}


@Composable
fun LenDenKhataApp(startDestination: String) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            if (currentRoute == Screen.Customer.route || currentRoute == Screen.Supplier.route || currentRoute == Screen.More.route) {
                BottomNavigationBar(navController = navController)
            }
        }

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavigationGraph(navController = navController, startDestination)
        }
    }

}