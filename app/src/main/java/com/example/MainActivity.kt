package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.preferences.ThemeMode
import com.example.ui.category.CategoryManagerScreen
import com.example.ui.category.EditCategoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val settingsRepo = (application as DownLandsApp).container.settingsRepository

    setContent {
      val themeMode by settingsRepo.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
      val dynamicColor by settingsRepo.dynamicColorFlow.collectAsState(initial = true)
      val darkTheme = when (themeMode) {
          ThemeMode.LIGHT -> false
          ThemeMode.DARK -> true
          ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      MyApplicationTheme(
          darkTheme = darkTheme,
          dynamicColor = dynamicColor
      ) {
        DownLandsNavHost()
      }
    }
  }
}

@Composable
fun DownLandsNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToCategoryManager = { navController.navigate("category_manager") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategoryManager = { navController.navigate("category_manager") }
            )
        }
        composable("category_manager") {
            CategoryManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCategory = { categoryId ->
                    if (categoryId == null) {
                        navController.navigate("edit_category")
                    } else {
                        navController.navigate("edit_category?categoryId=$categoryId")
                    }
                }
            )
        }
        composable(
            route = "edit_category?categoryId={categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            EditCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
