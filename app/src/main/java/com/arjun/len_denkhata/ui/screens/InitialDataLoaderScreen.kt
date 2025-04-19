package com.arjun.len_denkhata.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.ui.viewmodel.InitialDataLoaderViewModel
import com.arjun.len_denkhata.ui.viewmodel.InitialLoadingState

@Composable
fun InitialDataLoaderScreen(
    navController: NavHostController,
    viewModel: InitialDataLoaderViewModel = hiltViewModel(),
) {
    val loadingState by viewModel.loadingState
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.downloadInitialData()
    }

    when (loadingState) {
        InitialLoadingState.LOADING -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(R.string.loading_initial_data))
            }
        }
        InitialLoadingState.LOADED -> {
            Log.d("InitialDataLoader", "Data loaded, restarting app")
            RestartApp(context)
        }
        InitialLoadingState.ERROR -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.error_loading_data))
                Text(text = errorMessage ?: stringResource(R.string.unknown_error))
                // Optionally add a button to retry loading
            }
        }
    }
}

private fun RestartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent).also {
        Log.d("InitialDataLoader", "Restarting app")
    }
}