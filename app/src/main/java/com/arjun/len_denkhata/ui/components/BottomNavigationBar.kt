package com.arjun.len_denkhata.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.arjun.len_denkhata.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Customer,
        Screen.Supplier,
        Screen.More
    )
    NavigationBar { // Use NavigationBar instead of BottomNavigation
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { screen ->
            NavigationBarItem( // Use NavigationBarItem instead of BottomNavigationItem
                icon = {
                    when (screen) {
                        Screen.Customer -> Icon(Icons.Filled.Person, contentDescription = "Customers")
                        Screen.Supplier -> Icon(Icons.Filled.ShoppingCart, contentDescription = "Suppliers")
                        Screen.More -> Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        Screen.CustomerTransaction -> TODO()
                        Screen.SupplierTransaction -> TODO()
                        Screen.TransactionEntry -> TODO()
                        Screen.Login -> TODO()
                        Screen.CustomerDetail -> TODO()
                    }
                },
                label = { Text(screen.route) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}