package com.arjun.len_denkhata.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.ui.components.TopAppBar
import com.arjun.len_denkhata.ui.viewmodel.SupplierViewModel

@Composable
fun SupplierScreen(viewModel: SupplierViewModel = hiltViewModel(), navController: NavHostController) {
    TopAppBar("Supplier", navController)
}