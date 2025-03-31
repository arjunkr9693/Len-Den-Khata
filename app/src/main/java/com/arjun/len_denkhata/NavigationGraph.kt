package com.arjun.len_denkhata

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arjun.len_denkhata.data.database.transactions.customer.CustomerTransactionEntity
import com.arjun.len_denkhata.ui.screens.CustomerScreen
import com.arjun.len_denkhata.ui.screens.CustomerTransactionEntryScreen
import com.arjun.len_denkhata.ui.screens.CustomerTransactionScreen
import com.arjun.len_denkhata.ui.screens.LoginScreen
import com.arjun.len_denkhata.ui.screens.MoreScreen
import com.arjun.len_denkhata.ui.screens.SupplierScreen
import com.arjun.len_denkhata.ui.screens.SupplierTransactionScreen
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.arjun.len_denkhata.data.utils.UserSession
import com.arjun.len_denkhata.data.utils.toCustomerTransactionEntity
import com.arjun.len_denkhata.data.utils.toJson
import com.arjun.len_denkhata.ui.screens.CustomerDetailScreen
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Customer : Screen("customer")
    object Supplier : Screen("supplier")
    object More : Screen("more")
    object CustomerDetail: Screen("customerDetail")
    object CustomerTransaction : Screen("customerTransaction/{customerId}") {
        fun createRoute(customerId: String) = "customerTransaction/$customerId"
    }

    object SupplierTransaction : Screen("supplierTransaction/{supplierId}") {
        fun createRoute(supplierId: String) = "supplierTransaction/$supplierId"
    }

    object TransactionEntry : Screen(
        "transactionEntry&customerId={customerId}&transactionType={transactionType}&isEditing={isEditing}&customerTransaction={customerTransaction}"
    ) {
        fun createRoute(
            customerId: String,
            transactionType: String,
            isEditing: Boolean = false,
            customerTransaction: CustomerTransactionEntity? = null
        ) = "transactionEntry&customerId=${customerId}&transactionType=$transactionType&isEditing=$isEditing&customerTransaction=${customerTransaction?.toJson()}"
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, startDestination: String) {

    var customerViewModel: CustomerViewModel = hiltViewModel()
    var isLoggedIn = false

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Supplier.route) {
            SupplierScreen(navController = navController)
        }

        composable(Screen.More.route) {
            MoreScreen(navController = navController)
        }
        composable(Screen.Customer.route) {
            CustomerScreen(navController = navController, viewModel = customerViewModel)
        }
        composable(Screen.CustomerTransaction.route) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            if (customerId != null) {
                CustomerTransactionScreen(navController = navController, customerId = customerId, viewModel = customerViewModel)
            }
        }
        composable(Screen.SupplierTransaction.route) { backStackEntry ->
            val supplierId = backStackEntry.arguments?.getString("supplierId")
            if (supplierId != null) {
                SupplierTransactionScreen(navController, supplierId)
            }
        }
        composable(
            route = Screen.TransactionEntry.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.StringType },
                navArgument("transactionType") { type = NavType.StringType },
                navArgument("isEditing") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("customerTransaction") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            val transactionType = backStackEntry.arguments?.getString("transactionType")
            val isEditing = backStackEntry.arguments?.getBoolean("isEditing") ?: false
            val customerTransactionJson = backStackEntry.arguments?.getString("customerTransaction")
            val customerTransaction = customerTransactionJson?.toCustomerTransactionEntity()

            Log.d("testTag", customerId.toString())
            if (customerId != null && transactionType != null) {
                CustomerTransactionEntryScreen(
                    navController = navController,
                    customerId = customerId,
                    transactionType = transactionType,
                    isEditing = isEditing,
                    customerTransactionEntity = customerTransaction
                )
            }
        }

        composable("customerDetail") {
            CustomerDetailScreen(viewModel = customerViewModel, navController = navController)
        }
    }
}
