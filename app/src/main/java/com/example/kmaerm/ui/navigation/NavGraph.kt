package com.example.kmaerm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kmaerm.ui.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object OfficerMain : Screen("officer_main")
    object HoSoDetail : Screen("ho_so_detail/{hoSoId}") {
        fun createRoute(hoSoId: String) = "ho_so_detail/$hoSoId"
    }
    object ProcessProfile : Screen("process_profile/{hoSoId}") {
        fun createRoute(hoSoId: String) = "process_profile/$hoSoId"
    }
    object CompanyInfo : Screen("company_info")
    object ChangePassword : Screen("change_password")
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = { role ->
                    val destination = if (role == "CanBo") Screen.OfficerMain.route else Screen.Main.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToHoSoDetail = { hoSoId ->
                    navController.navigate(Screen.HoSoDetail.createRoute(hoSoId))
                },
                onNavigateToCompanyInfo = {
                    navController.navigate(Screen.CompanyInfo.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                }
            )
        }

        composable(Screen.OfficerMain.route) {
            OfficerMainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.OfficerMain.route) { inclusive = true }
                    }
                },
                onNavigateToProcessProfile = { hoSoId ->
                    navController.navigate(Screen.ProcessProfile.createRoute(hoSoId))
                }
            )
        }

        composable(
            route = Screen.HoSoDetail.route,
            arguments = listOf(
                navArgument("hoSoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val hoSoId = backStackEntry.arguments?.getString("hoSoId") ?: ""
            HoSoDetailScreen(
                hoSoId = hoSoId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ProcessProfile.route,
            arguments = listOf(
                navArgument("hoSoId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val hoSoId = backStackEntry.arguments?.getString("hoSoId") ?: ""
            ProcessProfileScreen(
                hoSoId = hoSoId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CompanyInfo.route) {
            CompanyInfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
