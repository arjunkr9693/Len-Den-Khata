package com.arjun.len_denkhata.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.ui.viewmodel.MoreViewModel

@Composable
fun MoreScreen(viewModel: MoreViewModel = hiltViewModel(), navController: NavHostController) {
    TopAppBar("More", navController)
}