package com.example.taller3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller3.ui.screens.ProfileScreen
import com.example.taller3.ui.theme.screens.HomeScreen
import com.example.taller3.ui.theme.screens.LoginScreen
import com.example.taller3.ui.theme.screens.MapScreen

import com.example.taller3.ui.theme.screens.RegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("map") { MapScreen(navController) }
    }
}
