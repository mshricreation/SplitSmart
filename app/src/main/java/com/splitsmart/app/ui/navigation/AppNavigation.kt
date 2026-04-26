package com.splitsmart.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.splitsmart.app.ui.screens.*

// ── Route constants ──────────────────────────────────────────────────────────
object Routes {
    const val HOME           = "home"
    const val CREATE_GROUP   = "create_group"
    const val GROUP_DETAIL   = "group/{groupId}"
    const val ADD_EXPENSE    = "group/{groupId}/add_expense"
    const val BALANCE_SUMMARY = "group/{groupId}/balances"
    // Settle-up can be navigated to with pre-filled values from smart settlement
    const val SETTLE_UP      = "group/{groupId}/settle?fromUserId={fromUserId}&toUserId={toUserId}&amount={amount}"

    fun groupDetail(groupId: String)    = "group/$groupId"
    fun addExpense(groupId: String)     = "group/$groupId/add_expense"
    fun balanceSummary(groupId: String) = "group/$groupId/balances"
    fun settleUp(
        groupId: String,
        fromUserId: String = "",
        toUserId: String = "",
        amount: String = ""
    ) = "group/$groupId/settle?fromUserId=$fromUserId&toUserId=$toUserId&amount=$amount"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onGroupClick  = { groupId -> navController.navigate(Routes.groupDetail(groupId)) },
                onCreateGroup = { navController.navigate(Routes.CREATE_GROUP) }
            )
        }

        // ── Create Group ──────────────────────────────────────────────────────
        composable(Routes.CREATE_GROUP) {
            CreateGroupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Group Detail ──────────────────────────────────────────────────────
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId      = groupId,
                onNavigateBack = { navController.popBackStack() },
                onAddExpense   = { navController.navigate(Routes.addExpense(groupId)) },
                onViewBalances = { navController.navigate(Routes.balanceSummary(groupId)) },
                onSettleUp     = { navController.navigate(Routes.settleUp(groupId)) }
            )
        }

        // ── Add Expense ───────────────────────────────────────────────────────
        composable(
            route = Routes.ADD_EXPENSE,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Balance Summary ───────────────────────────────────────────────────
        composable(
            route = Routes.BALANCE_SUMMARY,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString("groupId") ?: return@composable
            BalanceSummaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSettleUp = { from, to, amount ->
                    navController.navigate(Routes.settleUp(groupId, from, to, amount))
                }
            )
        }

        // ── Settle Up ─────────────────────────────────────────────────────────
        composable(
            route = Routes.SETTLE_UP,
            arguments = listOf(
                navArgument("groupId")    { type = NavType.StringType },
                navArgument("fromUserId") { type = NavType.StringType; defaultValue = "" },
                navArgument("toUserId")   { type = NavType.StringType; defaultValue = "" },
                navArgument("amount")     { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            SettleUpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
