package com.nothing.card

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nothing.card.ui.screens.add.AddCardScreen
import com.nothing.card.ui.screens.detail.DetailScreen
import com.nothing.card.ui.screens.home.HomeScreen
import com.nothing.card.ui.screens.scanner.ScannerScreen
import com.nothing.card.ui.screens.settings.*
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingCardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NothingCardTheme {
                NothingCardApp()
            }
        }
    }
}

@Composable
fun NothingCardApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NothingBlack
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 1.05f, animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.95f, animationSpec = tween(250))
            }
        ) {
            composable("home") {
                HomeScreen(
                    onCardClick = { cardId ->
                        navController.navigate("detail/$cardId")
                    },
                    onAddClick = {
                        navController.navigate("add_vendor")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
            
            composable("add_vendor") {
                com.nothing.card.ui.screens.vendor.VendorSelectionScreen(
                    onVendorSelected = { vendor, barcode, format ->
                        if (barcode == "") {
                            // Empty string means user chose Camera
                            navController.navigate("scan/${vendor.name}/${vendor.colorHex}")
                        } else {
                            // Non-empty (barcode found) or null (no barcode found in gallery image)
                            // Navigate to add screen
                            val b = barcode ?: ""
                            val f = format ?: "QR_CODE"
                            val cleanColor = vendor.colorHex.replace("#", "")
                            val encodedBarcode = android.net.Uri.encode(b)
                            val encodedFormat = android.net.Uri.encode(f)
                            navController.navigate("add/${vendor.name}/$cleanColor?barcode=$encodedBarcode&format=$encodedFormat")
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "scan/{vendorName}/{colorHex}",
                arguments = listOf(
                    navArgument("vendorName") { type = NavType.StringType },
                    navArgument("colorHex") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val vendorName = backStackEntry.arguments?.getString("vendorName") ?: ""
                val colorHex = backStackEntry.arguments?.getString("colorHex") ?: ""
                ScannerScreen(
                    onBarcodeScanned = { barcode, format ->
                        val cleanColor = colorHex.replace("#", "")
                        val encodedBarcode = android.net.Uri.encode(barcode)
                        val encodedFormat = android.net.Uri.encode(format)
                        navController.navigate("add/$vendorName/$cleanColor?barcode=$encodedBarcode&format=$encodedFormat") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onClose = { navController.popBackStack() }
                )
            }

            composable(
                route = "add/{vendorName}/{colorHex}?barcode={barcode}&format={format}",
                arguments = listOf(
                    navArgument("vendorName") { type = NavType.StringType },
                    navArgument("colorHex") { type = NavType.StringType },
                    navArgument("barcode") { type = NavType.StringType; nullable = true },
                    navArgument("format") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val vendorName = backStackEntry.arguments?.getString("vendorName") ?: ""
                val colorHex = backStackEntry.arguments?.getString("colorHex") ?: "#333333"
                val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                val format = backStackEntry.arguments?.getString("format") ?: "QR_CODE"
                
                AddCardScreen(
                    cardNumber = barcode,
                    barcodeFormat = format,
                    initialName = vendorName,
                    initialColor = colorHex,
                    onCardAdded = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "detail/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
                DetailScreen(
                    cardId = cardId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPermissions = { navController.navigate("permissions") },
                    onNavigateToCloudSync = { navController.navigate("cloud_sync") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToPkPass = { navController.navigate("pkpass") }
                )
            }
            
            composable("permissions") {
                PermissionsScreen(onBack = { navController.popBackStack() })
            }
            
            composable("cloud_sync") {
                CloudSyncScreen(onBack = { navController.popBackStack() })
            }
            
            composable("about") {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable("pkpass") {
                PkPassScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
