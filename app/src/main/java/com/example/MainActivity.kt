package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.VendorRepository
import com.example.ui.components.AboutDialog
import com.example.ui.components.AiAssistantSheet
import com.example.ui.screens.MainScreen
import com.example.ui.screens.VendorDetailScreen
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.LocalBitesTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = VendorRepository(database.vendorDao(), database.reviewDao())

        setContent {
            LocalBitesTheme {
                var currentVendorId by remember { mutableStateOf<Long?>(null) }
                var showAboutDialog by remember { mutableStateOf(false) }
                var showAiSheet by remember { mutableStateOf(false) }

                val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val vendors by repository.allVendors.collectAsStateWithLifecycle(initialValue = emptyList())
                val scope = rememberCoroutineScope()

                BackHandler(enabled = currentVendorId != null) {
                    currentVendorId = null
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CharcoalBackground)
                ) {
                    AnimatedContent(
                        targetState = currentVendorId,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "screen_transition"
                    ) { targetId ->
                        if (targetId == null) {
                            MainScreen(
                                repository = repository,
                                onNavigateToVendor = { vendorId ->
                                    currentVendorId = vendorId
                                }
                            )
                        } else {
                            VendorDetailScreen(
                                vendorId = targetId,
                                repository = repository,
                                onNavigateBack = { currentVendorId = null },
                                onOpenAiAssistant = { showAiSheet = true },
                                onOpenAbout = { showAboutDialog = true }
                            )
                        }
                    }

                    if (showAboutDialog) {
                        AboutDialog(
                            onDismiss = { showAboutDialog = false }
                        )
                    }

                    if (showAiSheet) {
                        AiAssistantSheet(
                            sheetState = aiSheetState,
                            vendors = vendors,
                            onDismiss = {
                                scope.launch {
                                    aiSheetState.hide()
                                    showAiSheet = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
