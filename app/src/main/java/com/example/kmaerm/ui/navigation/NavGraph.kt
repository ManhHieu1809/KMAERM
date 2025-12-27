package com.example.kmaerm.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kmaerm.ui.screens.*
import com.example.kmaerm.ui.viewmodel.GiayPhepViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object OfficerMain : Screen("officer_main")
    object ParallelDemo : Screen("parallel_demo")
    object HoSoDetail : Screen("ho_so_detail/{hoSoId}") {
        fun createRoute(hoSoId: String) = "ho_so_detail/$hoSoId"
    }
    object ProcessProfile : Screen("process_profile/{hoSoId}") {
        fun createRoute(hoSoId: String) = "process_profile/$hoSoId"
    }
    object LicenseDetail : Screen("license_detail/{giayPhepId}/{doanhNghiepId}") {
        fun createRoute(giayPhepId: String, doanhNghiepId: String) = "license_detail/$giayPhepId/$doanhNghiepId"
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
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) {
                            inclusive = true
                        }
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
                onNavigateToLicenseDetail = { giayPhepId, doanhNghiepId ->
                    navController.navigate(Screen.LicenseDetail.createRoute(giayPhepId, doanhNghiepId))
                },
                onNavigateToCompanyInfo = {
                    navController.navigate(Screen.CompanyInfo.route)
                },
                onNavigateToChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                },
                onNavigateToParallelDemo = {
                    navController.navigate(Screen.ParallelDemo.route)
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

        composable(
            route = Screen.LicenseDetail.route,
            arguments = listOf(
                navArgument("giayPhepId") {
                    type = NavType.StringType
                },
                navArgument("doanhNghiepId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val giayPhepId = backStackEntry.arguments?.getString("giayPhepId") ?: ""
            val doanhNghiepId = backStackEntry.arguments?.getString("doanhNghiepId") ?: ""
            val viewModel: GiayPhepViewModel = viewModel()
            val giayPhep by viewModel.selectedGiayPhep.collectAsState()

            LaunchedEffect(giayPhepId) {
                viewModel.loadGiayPhepDetail(giayPhepId)
            }

            giayPhep?.let {
                LicenseDetailScreen(
                    giayPhep = it,
                    doanhNghiepId = doanhNghiepId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = {
                        // TODO: Implement edit navigation if needed
                    }
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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

        composable(Screen.ParallelDemo.route) {
            DemoComparisonScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
