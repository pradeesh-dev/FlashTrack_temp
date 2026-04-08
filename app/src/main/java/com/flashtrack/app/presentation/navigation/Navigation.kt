package com.flashtrack.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.flashtrack.app.presentation.features.accounts.AccountDetailScreen
import com.flashtrack.app.presentation.features.accounts.AccountsScreen
import com.flashtrack.app.presentation.features.accounts.BillPaymentScreen
import com.flashtrack.app.presentation.features.analysis.AnalysisScreen
import com.flashtrack.app.presentation.features.budgets.BudgetsScreen
import com.flashtrack.app.presentation.features.categories.CategoriesScreen
import com.flashtrack.app.presentation.features.debts.*
import com.flashtrack.app.presentation.features.home.HomeScreen
import com.flashtrack.app.presentation.features.more.MoreScreen
import com.flashtrack.app.presentation.features.scheduled.ScheduledScreen
import com.flashtrack.app.presentation.features.settings.SettingsScreen
import com.flashtrack.app.presentation.features.tags.TagsScreen
import com.flashtrack.app.presentation.features.transactions.AddTransactionScreen
import com.flashtrack.app.presentation.features.transactions.TransactionsScreen

sealed class Screen(val route: String) {
    object Home         : Screen("home")
    object Analysis     : Screen("analysis")
    object Accounts     : Screen("accounts")
    object More         : Screen("more")
    object Transactions : Screen("transactions")
    object Budgets      : Screen("budgets")
    object Categories   : Screen("categories")
    object Tags         : Screen("tags")
    object Scheduled    : Screen("scheduled")
    object Debts        : Screen("debts")
    object Settings     : Screen("settings")

    // FIX 3: added editId parameter for edit mode
    object AddTransaction : Screen("add_transaction?type={type}&accountId={accountId}&editId={editId}") {
        fun route(type: String = "EXPENSE", accountId: Long = -1L, editId: Long = -1L) =
            "add_transaction?type=$type&accountId=$accountId&editId=$editId"
    }

    object AccountDetail : Screen("account_detail/{accountId}") {
        fun route(id: Long) = "account_detail/$id"
    }
    object BillPayment : Screen("bill_payment/{accountId}") {
        fun route(id: Long) = "bill_payment/$id"
    }
    object DebtDetail : Screen("debt_detail/{personId}") {
        fun route(id: Long) = "debt_detail/$id"
    }
    object AddDebt : Screen("add_debt?debtType={debtType}") {
        fun route(type: String = "LENDING") = "add_debt?debtType=$type"
    }
}

@Composable
fun FlashTrackNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition  = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left,  tween(260)) + fadeIn(tween(260))  },
        exitTransition   = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left,  tween(260)) + fadeOut(tween(260)) },
        popEnterTransition  = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(260)) + fadeIn(tween(260))  },
        popExitTransition   = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(260)) + fadeOut(tween(260)) }
    ) {
        composable(Screen.Home.route)         { HomeScreen(navController) }
        composable(Screen.Analysis.route)     { AnalysisScreen(navController) }
        composable(Screen.Accounts.route)     { AccountsScreen(navController) }
        composable(Screen.More.route)         { MoreScreen(navController) }
        composable(Screen.Transactions.route) { TransactionsScreen(navController) }
        composable(Screen.Budgets.route)      { BudgetsScreen(navController) }
        composable(Screen.Categories.route)   { CategoriesScreen(navController) }
        composable(Screen.Tags.route)         { TagsScreen(navController) }
        composable(Screen.Scheduled.route)    { ScheduledScreen(navController) }
        composable(Screen.Debts.route)        { DebtsScreen(navController) }
        composable(Screen.Settings.route)     { SettingsScreen(navController) }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type")      { type = NavType.StringType; defaultValue = "EXPENSE" },
                navArgument("accountId") { type = NavType.LongType;   defaultValue = -1L },
                navArgument("editId")    { type = NavType.LongType;   defaultValue = -1L }  // FIX 3
            )
        ) { ent ->
            AddTransactionScreen(
                navController        = navController,
                initialType          = ent.arguments?.getString("type") ?: "EXPENSE",
                initialAccountId     = ent.arguments?.getLong("accountId") ?: -1L,
                editTransactionId    = ent.arguments?.getLong("editId") ?: -1L
            )
        }

        composable(Screen.AccountDetail.route,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { ent -> AccountDetailScreen(navController, ent.arguments?.getLong("accountId") ?: 0L) }

        composable(Screen.BillPayment.route,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { ent -> BillPaymentScreen(navController, ent.arguments?.getLong("accountId") ?: 0L) }

        composable(Screen.DebtDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { ent -> DebtDetailScreen(navController, ent.arguments?.getLong("personId") ?: 0L) }

        composable(Screen.AddDebt.route,
            arguments = listOf(navArgument("debtType") { type = NavType.StringType; defaultValue = "LENDING" })
        ) { ent -> AddDebtScreen(navController, ent.arguments?.getString("debtType") ?: "LENDING") }
    }
}
