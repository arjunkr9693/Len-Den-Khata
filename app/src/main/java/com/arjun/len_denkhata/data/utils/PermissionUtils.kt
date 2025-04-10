package com.arjun.len_denkhata.data.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

//object PermissionUtils {
//
//    @Composable
//    fun requestContactPermission(
//        onPermissionGranted: () -> Unit,
//        onPermissionDenied: () -> Unit
//    ): ManagedActivityResultLauncher<String, Boolean> {
//        val context = LocalContext.current
//        return rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                onPermissionGranted()
//            } else {
//                onPermissionDenied()
//            }
//        }
//    }
//
//    fun launchContactPicker(contactPickerLauncher: ActivityResultLauncher<Intent>) {
//        val intent = Intent(Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
//        contactPickerLauncher.launch(intent)
//    }
//
//    fun shouldShowRationale(context: Context): Boolean {
//        return androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
//            context as androidx.activity.ComponentActivity,
//            Manifest.permission.READ_CONTACTS
//        )
//    }
//
//    fun openAppSettings(context: Context) {
//        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//        val uri = Uri.fromParts("package", context.packageName, null)
//        intent.data = uri
//        context.startActivity(intent)
//    }
//
//}