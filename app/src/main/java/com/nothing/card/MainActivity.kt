package com.nothing.card

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.util.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = getSharedPreferences("nothing_card_prefs", Context.MODE_PRIVATE)
        val isBiometricEnabled = prefs.getBoolean("biometric_enabled", false)

        setContent {
            val context = LocalContext.current
            val activity = remember(context) { 
                var c = context
                while (c is android.content.ContextWrapper) {
                    if (c is FragmentActivity) break
                    c = c.baseContext
                }
                c as? FragmentActivity
            }

            var canAuthenticate by remember { mutableStateOf(false) }
            var isAuthChecked by remember { mutableStateOf(false) }
            var isUnlocked by remember { mutableStateOf(false) }
            val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
            
            LaunchedEffect(Unit) {
                try {
                    canAuthenticate = biometricHelper.canAuthenticate()
                } catch (e: Exception) {
                    canAuthenticate = false
                }
                isAuthChecked = true
                
                if (!isBiometricEnabled || !canAuthenticate) {
                    isUnlocked = true
                } else {
                    activity?.let {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        authenticate(it) { isUnlocked = true }
                    }
                }
            }

            val shouldShowLock = isBiometricEnabled && canAuthenticate
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            if (shouldShowLock) isUnlocked = false
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            if (isAuthChecked && shouldShowLock && !isUnlocked) {
                                activity?.let { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    authenticate(it) { isUnlocked = true } 
                                }
                            }
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            NothingCardTheme {
                when {
                    !isAuthChecked -> Box(modifier = Modifier.fillMaxSize().background(NothingBlack))
                    isUnlocked -> NothingCardApp()
                    else -> {
                        LockScreen { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            activity?.let { authenticate(it) { isUnlocked = true } } 
                        }
                    }
                }
            }
        }
    }

    private fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit) {
        try {
            biometricHelper.authenticate(
                activity = activity,
                title = "Nothing Card",
                subtitle = "Sblocca per accedere alle tue carte",
                onSuccess = onSuccess,
                onError = { }
            )
        } catch (e: Exception) {
            onSuccess()
        }
    }
}

@Composable
fun LockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .clickable(onClick = onUnlockClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(240.dp)
        )
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
            enterTransition = { fadeIn(tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)) },
            exitTransition = { fadeOut(tween(250)) + scaleOut(targetScale = 1.05f, animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(tween(300)) + scaleIn(initialScale = 1.05f, animationSpec = tween(300)) },
            popExitTransition = { fadeOut(tween(250)) + scaleOut(targetScale = 0.95f, animationSpec = tween(250)) }
        ) {
            composable("home") {
                HomeScreen(
                    onCardClick = { cardId -> navController.navigate("detail/$cardId") },
                    onAddClick = { navController.navigate("add_vendor") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            
            composable("add_vendor") {
                com.nothing.card.ui.screens.vendor.VendorSelectionScreen(
                    onVendorSelected = { vendor, barcode, format ->
                        if (barcode == "") {
                            navController.navigate("scan/${vendor.name}/${vendor.colorHex}")
                        } else {
                            val encodedBarcode = android.net.Uri.encode(barcode ?: "")
                            val encodedFormat = android.net.Uri.encode(format ?: "QR_CODE")
                            val cleanColor = vendor.colorHex.replace("#", "")
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
                        val encodedBarcode = android.net.Uri.encode(barcode)
                        val encodedFormat = android.net.Uri.encode(format)
                        val cleanColor = colorHex.replace("#", "")
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
                AddCardScreen(
                    cardNumber = backStackEntry.arguments?.getString("barcode") ?: "",
                    barcodeFormat = backStackEntry.arguments?.getString("format") ?: "QR_CODE",
                    initialName = backStackEntry.arguments?.getString("vendorName") ?: "",
                    initialColor = backStackEntry.arguments?.getString("colorHex") ?: "#333333",
                    onCardAdded = {
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "detail/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) { backStackEntry ->
                DetailScreen(
                    cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPermissions = { navController.navigate("permissions") },
                    onNavigateToCloudSync = { navController.navigate("cloud_sync") },
                    onNavigateToAbout = { navController.navigate("about") },
                    onNavigateToPkPass = { navController.navigate("pkpass") },
                    onNavigateToAccount = { navController.navigate("account") }
                )
            }

            composable("account") {
                com.nothing.card.ui.screens.account.AccountScreen(
                    onBack = { navController.popBackStack() },
                    onSignOutSuccess = { navController.popBackStack("settings", inclusive = false) }
                )
            }
            
            composable("permissions") { PermissionsScreen(onBack = { navController.popBackStack() }) }
            composable("cloud_sync") { CloudSyncScreen(onBack = { navController.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { navController.popBackStack() }) }
            composable("pkpass") { PkPassScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
