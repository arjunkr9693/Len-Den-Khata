package com.arjun.len_denkhata

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arjun.len_denkhata.ui.screens.customer.CustomerDetailScreen
import com.arjun.len_denkhata.ui.screens.customer.CustomerScreen
import com.arjun.len_denkhata.ui.screens.customer.CustomerTransactionEntryScreen
import com.arjun.len_denkhata.ui.screens.customer.CustomerTransactionScreen
import com.arjun.len_denkhata.ui.screens.login.LoginScreen
import com.arjun.len_denkhata.ui.screens.MoreScreen
import com.arjun.len_denkhata.ui.screens.SupplierScreen
import com.arjun.len_denkhata.ui.screens.SupplierTransactionScreen
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.arjun.len_denkhata.data.database.transactions.monthbook.MonthBookTransactionType
import com.arjun.len_denkhata.monthbook.ui.viewmodel.MonthBookViewModel
import com.arjun.len_denkhata.ui.screens.monthbook.AddMonthBookTransactionScreen
import com.arjun.len_denkhata.ui.screens.monthbook.MonthBookScreen
import com.arjun.len_denkhata.ui.screens.InitialDataLoaderScreen
import com.arjun.len_denkhata.ui.screens.LanguageSelectionScreen
import com.arjun.len_denkhata.ui.screens.monthbook.MonthBookCalculatedDataScreen
import com.arjun.len_denkhata.ui.viewmodel.CustomerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Customer : Screen("customer")
    object Supplier : Screen("supplier")
    object More : Screen("more")
    object CustomerDetail : Screen("customerDetail")
    object CustomerTransaction : Screen("customerTransaction/{customerId}") {
        fun createRoute(customerId: String) = "customerTransaction/$customerId"
    }

    object SupplierTransaction : Screen("supplierTransaction/{supplierId}") {
        fun createRoute(supplierId: String) = "supplierTransaction/$supplierId"
    }

    object TransactionEntry : Screen(
        "transactionEntry&customerId={customerId}&transactionType={transactionType}&isEditing={isEditing}&transactionId={transactionId}"
    ) {
        private const val CUSTOMER_ID_ARG = "customerId"
        private const val TRANSACTION_TYPE_ARG = "transactionType"
        private const val IS_EDITING_ARG = "isEditing"
        private const val TRANSACTION_ID_ARG = "transactionId"

        fun createRoute(
            customerId: String,
            transactionType: String,
            isEditing: Boolean = false,
            transactionId: Long = -1
        ): String {
            return "transactionEntry&$CUSTOMER_ID_ARG=$customerId&$TRANSACTION_TYPE_ARG=$transactionType&$IS_EDITING_ARG=$isEditing${"&$TRANSACTION_ID_ARG=$transactionId"}"
        }
    }

    object MonthBook : Screen("month_book")
    object AddIncome : Screen("add_income")
    object AddExpense : Screen("add_expense")
    object MonthBookCalculatedData : Screen("month_book_calculated_data")

    object EditMonthBookTransaction : Screen(
        "edit_month_book_transaction&transactionType={transactionType}&transactionId={transactionId}"
    ) {
        fun createRoute(
            transactionType: String, // "income" or "expense"
            transactionId: Long
        ) = "edit_month_book_transaction&transactionType=$transactionType&transactionId=$transactionId"
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, startDestination: String) {

    var customerViewModel: CustomerViewModel = hiltViewModel()

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
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L // Or some other indicator that it's not present
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")
            val transactionType = backStackEntry.arguments?.getString("transactionType")
            val isEditing = backStackEntry.arguments?.getBoolean("isEditing") ?: false
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1

            Log.d("testTag", "Transaction ID: $transactionId")
            if (customerId != null && transactionType != null) {
                CustomerTransactionEntryScreen(
                    navController = navController,
                    customerId = customerId,
                    transactionType = transactionType,
                    isEditing = isEditing,
                    transactionId = transactionId
                )
            }
        }

        composable(Screen.CustomerDetail.route) {
            CustomerDetailScreen(viewModel = customerViewModel, navController = navController)
        }

        composable(Screen.MonthBook.route) {
            val viewModel: MonthBookViewModel = hiltViewModel()
            MonthBookScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.AddIncome.route) {
            val viewModel: MonthBookViewModel = hiltViewModel()
            AddMonthBookTransactionScreen(
                navController = navController,
                transactionType = MonthBookTransactionType.INCOME,
                viewModel = viewModel,
                isEditing = false,
            )
        }
        composable(Screen.AddExpense.route) {
            val viewModel: MonthBookViewModel = hiltViewModel()
            AddMonthBookTransactionScreen(
                navController = navController,
                transactionType = MonthBookTransactionType.EXPENSE,
                viewModel = viewModel,
                isEditing = false,
            )
        }
        composable(
            route = Screen.EditMonthBookTransaction.route,
            arguments = listOf(
                navArgument("transactionType") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionTypeString = backStackEntry.arguments?.getString("transactionType") ?: ""
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L

            val transactionType = when (transactionTypeString.lowercase()) {
                "income" -> MonthBookTransactionType.INCOME
                "expense" -> MonthBookTransactionType.EXPENSE
                else -> MonthBookTransactionType.INCOME // Default
            }

            val viewModel: MonthBookViewModel = hiltViewModel()

            AddMonthBookTransactionScreen(
                navController = navController,
                transactionType = transactionType,
                viewModel = viewModel,
                isEditing = true,
                transactionId = transactionId // Pass transactionId
            )
        }
        composable(Screen.MonthBookCalculatedData.route) {
            MonthBookCalculatedDataScreen(navController = navController)
        }

        composable("initial_data_loader") {
            InitialDataLoaderScreen(
                navController = navController,
//                navigateToCustomerScreen = {
//                    navController.navigate(Screen.Customer.route) {
//                        popUpTo("initial_data_loader") { inclusive = true }
//                    }
//                }
            )
        }

        composable("language_selection") {
            LanguageSelectionScreen(navController)
        }

    }
}