package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Vendor
import com.example.data.repository.VendorRepository
import com.example.ui.components.AboutDialog
import com.example.ui.components.AiAssistantSheet
import com.example.ui.components.CategoryFilterBar
import com.example.ui.components.DiscoveryViewMode
import com.example.ui.components.LocalBitesMapView
import com.example.ui.components.LocalBitesTopBar
import com.example.ui.components.MapVendorPreviewCard
import com.example.ui.components.SearchAndToggleBar
import com.example.ui.components.VendorCard
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.OnAmberPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: VendorRepository,
    onNavigateToVendor: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vendors by repository.allVendors.collectAsStateWithLifecycle(initialValue = emptyList())

    // UI state
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(DiscoveryViewMode.LIST) }
    var selectedVendorForPreview by remember { mutableStateOf<Vendor?>(null) }

    // User GPS location
    var userLat by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LATITUDE) }
    var userLng by remember { mutableDoubleStateOf(LocationHelper.DEFAULT_LONGITUDE) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showLocationRationaleBanner by remember { mutableStateOf(!hasLocationPermission) }

    // Sheets & Dialogs
    var showAddVendorSheet by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAiAssistantSheet by remember { mutableStateOf(false) }

    val addVendorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            hasLocationPermission = true
            showLocationRationaleBanner = false
            scope.launch {
                val loc = LocationHelper.getCurrentLocation(context)
                if (loc != null) {
                    userLat = loc.latitude
                    userLng = loc.longitude
                }
            }
        }
    }

    // Attempt GPS fetch on start if permitted
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                userLat = loc.latitude
                userLng = loc.longitude
            }
        }
    }

    // Filter vendors based on Category and Search Query
    val filteredVendors = remember(vendors, selectedCategory, searchQuery) {
        vendors.filter { vendor ->
            val matchesCat = if (selectedCategory == "All") true
            else vendor.category.equals(selectedCategory, ignoreCase = true)

            val matchesQuery = if (searchQuery.isBlank()) true
            else vendor.name.contains(searchQuery, ignoreCase = true) ||
                    vendor.category.contains(searchQuery, ignoreCase = true) ||
                    vendor.addressOrLandmark.contains(searchQuery, ignoreCase = true)

            matchesCat && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            LocalBitesTopBar(
                canNavigateBack = false,
                onOpenAiAssistant = { showAiAssistantSheet = true },
                onOpenAbout = { showAboutDialog = true }
            )
        },
        bottomBar = {
            // High Density Bottom Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = CharcoalBorder),
                color = CharcoalBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isHome = viewMode == DiscoveryViewMode.LIST
                    val isNearby = viewMode == DiscoveryViewMode.MAP

                    // Home button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewMode = DiscoveryViewMode.LIST }
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .testTag("nav_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (isHome) AmberPrimary else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home",
                            fontSize = 10.sp,
                            fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                            color = if (isHome) AmberPrimary else TextMuted
                        )
                    }

                    // Nearby button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewMode = DiscoveryViewMode.MAP }
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .testTag("nav_nearby_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Nearby",
                            tint = if (isNearby) AmberPrimary else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Nearby",
                            fontSize = 10.sp,
                            fontWeight = if (isNearby) FontWeight.Bold else FontWeight.Medium,
                            color = if (isNearby) AmberPrimary else TextMuted
                        )
                    }

                    // About button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAboutDialog = true }
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .testTag("nav_about_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "About",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddVendorSheet = true },
                containerColor = AmberPrimary,
                contentColor = OnAmberPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_vendor_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Stall",
                        tint = OnAmberPrimary
                    )
                    Text(
                        text = "Add Stall",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = OnAmberPrimary
                    )
                }
            }
        },
        containerColor = CharcoalBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Location Permission Friendly Banner
            if (showLocationRationaleBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("location_permission_banner"),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberPrimary.copy(alpha = 0.5f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable GPS to discover street carts closest to you!",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("enable_gps_btn")
                            ) {
                                Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = { showLocationRationaleBanner = false }) {
                                Text("Dismiss", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Search Bar & Map/List Toggle
            SearchAndToggleBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onLocateMe = {
                    if (hasLocationPermission) {
                        scope.launch {
                            val loc = LocationHelper.getCurrentLocation(context)
                            if (loc != null) {
                                userLat = loc.latitude
                                userLng = loc.longitude
                            }
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            )

            // Category Filter Chips
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // Main Content Area: Map vs List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (viewMode == DiscoveryViewMode.MAP) {
                    // Interactive Map View framed in High Density Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 6.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(22.dp))
                            .background(CharcoalSurfaceElevated)
                    ) {
                        LocalBitesMapView(
                            vendors = filteredVendors,
                            selectedVendorId = selectedVendorForPreview?.id,
                            userLatitude = userLat,
                            userLongitude = userLng,
                            onVendorSelected = { vendor ->
                                selectedVendorForPreview = vendor
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (filteredVendors.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CharcoalSurfaceElevated.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "It's quiet here... 🍲",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Be the first foodie to put a stall on the map!",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        // Floating Map Quick Action Button (Top-Right)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (hasLocationPermission) {
                                        scope.launch {
                                            val loc = LocationHelper.getCurrentLocation(context)
                                            if (loc != null) {
                                                userLat = loc.latitude
                                                userLng = loc.longitude
                                            }
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(CharcoalSurface)
                                    .border(1.dp, CharcoalBorder, RoundedCornerShape(11.dp))
                                    .testTag("map_quick_gps_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = "GPS Location",
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Floating Bottom Preview Card when a pin is selected
                        selectedVendorForPreview?.let { vendor ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                            ) {
                                MapVendorPreviewCard(
                                    vendor = vendor,
                                    onViewDetails = { v ->
                                        onNavigateToVendor(v.id)
                                    },
                                    onDismiss = { selectedVendorForPreview = null }
                                )
                            }
                        }
                    }
                } else {
                    // List View
                    if (filteredVendors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No street stalls found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing search filters or add this stall using the '+' button!",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 84.dp)
                        ) {
                            items(filteredVendors, key = { it.id }) { vendor ->
                                VendorCard(
                                    vendor = vendor,
                                    onClick = { onNavigateToVendor(vendor.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Vendor Bottom Sheet
        if (showAddVendorSheet) {
            AddVendorSheet(
                sheetState = addVendorSheetState,
                initialLat = userLat,
                initialLng = userLng,
                onVendorAdded = { newVendor ->
                    scope.launch {
                        val newId = repository.addVendor(newVendor)
                        addVendorSheetState.hide()
                        showAddVendorSheet = false
                        onNavigateToVendor(newId)
                    }
                },
                onDismiss = {
                    scope.launch {
                        addVendorSheetState.hide()
                        showAddVendorSheet = false
                    }
                }
            )
        }

        // AI Community Assistant Bottom Sheet
        if (showAiAssistantSheet) {
            AiAssistantSheet(
                sheetState = aiSheetState,
                vendors = vendors,
                onDismiss = {
                    scope.launch {
                        aiSheetState.hide()
                        showAiAssistantSheet = false
                    }
                }
            )
        }

        // About Dialog (Designed & Developed by Noor ul Huda)
        if (showAboutDialog) {
            AboutDialog(
                onDismiss = { showAboutDialog = false }
            )
        }
    }
}
