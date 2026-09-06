package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.local.entity.Vendor
import com.example.ui.components.FOOD_CATEGORIES
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ImageUtils
import com.example.util.LocationHelper
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVendorSheet(
    sheetState: SheetState,
    initialLat: Double,
    initialLng: Double,
    onVendorAdded: (Vendor) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var vendorName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Chai") }
    var customCategoryText by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("Rs 30 - 80") }
    var landmark by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }

    var stallLat by remember { mutableDoubleStateOf(initialLat) }
    var stallLng by remember { mutableDoubleStateOf(initialLng) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showCameraRationale by remember { mutableStateOf(false) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageUtils.saveBitmapToInternalStorage(context, bitmap)
            photoUri = savedPath
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            errorMessage = "Camera permission is needed to take a photo of the stall."
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageUtils.saveUriToInternalStorage(context, uri)
            if (savedPath != null) {
                photoUri = savedPath
            }
        }
    }

    fun handleCameraClick() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(null)
            }
            else -> {
                showCameraRationale = true
            }
        }
    }

    if (showCameraRationale) {
        AlertDialog(
            onDismissRequest = { showCameraRationale = false },
            title = {
                Text(
                    text = "Camera Permission",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Local Bites needs camera access to take a photo of this street stall so fellow food lovers can easily recognize it.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCameraRationale = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Grant Permission", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraRationale = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CharcoalCard
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBackground,
        dragHandle = null,
        modifier = Modifier.testTag("add_vendor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Header
            Surface(color = CharcoalSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Street Food Stall",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Put an unmapped stall on the community map",
                            fontSize = 12.sp,
                            color = AmberSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Form Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photo Upload Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Vendor / Stall Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (photoUri.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = File(photoUri),
                                    contentDescription = "Selected stall photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { photoUri = "" },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Camera Button
                                Button(
                                    onClick = { handleCameraClick() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("upload_camera_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceElevated),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = AmberPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera", fontSize = 13.sp, color = TextPrimary)
                                }

                                // Gallery Button
                                Button(
                                    onClick = {
                                        galleryLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("upload_gallery_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceElevated),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery", fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // Stall Name Input
                OutlinedTextField(
                    value = vendorName,
                    onValueChange = {
                        vendorName = it
                        errorMessage = null
                    },
                    label = { Text("Vendor / Stall Name *", color = TextSecondary) },
                    placeholder = { Text("e.g. Chacha's Chai Point, Bun Kabab Cart", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vendor_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CharcoalSurface,
                        unfocusedContainerColor = CharcoalSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Selection Chips
                Column {
                    Text(
                        text = "Category *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf("Tea & Breakfast", "Fast Food", "Snacks", "Juice", "Desi Food", "Other")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) AmberPrimary else CharcoalSurface)
                                    .border(1.dp, if (isSel) AmberPrimary else CharcoalBorder, RoundedCornerShape(16.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("category_select_$cat")
                            ) {
                                Text(
                                    text = "${getCategoryEmoji(cat)} $cat",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
                if (selectedCategory == "Other") {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text("Enter category name", color = TextSecondary) },
                        placeholder = { Text("e.g. Bakery, Ice Cream", color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CharcoalSurface,
                            unfocusedContainerColor = CharcoalSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                // Price Range Input + Suggested Quick Chips
                Column {
                    OutlinedTextField(
                        value = priceRange,
                        onValueChange = { priceRange = it },
                        label = { Text("Estimated Price Range", color = TextSecondary) },
                        placeholder = { Text("e.g. Rs 20 - 50", color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_price_range"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmberPrimary,
                            unfocusedBorderColor = CharcoalBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = CharcoalSurface,
                            unfocusedContainerColor = CharcoalSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick price chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Rs 20 - 50", "Rs 50 - 100", "Rs 100 - 200", "Rs 150 - 300").forEach { priceChip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalSurfaceElevated)
                                    .clickable { priceRange = priceChip }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = priceChip, fontSize = 11.sp, color = AmberSecondary)
                            }
                        }
                    }
                }

                // Landmark / Directions
                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark & Directions", color = TextSecondary) },
                    placeholder = { Text("e.g. Under Banyan tree opposite post office", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_landmark"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = CharcoalBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CharcoalSurface,
                        unfocusedContainerColor = CharcoalSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Interactive Map Pin Dropper
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Drop Pin on Map",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            // Use Current GPS
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val loc = LocationHelper.getCurrentLocation(context)
                                        if (loc != null) {
                                            stallLat = loc.latitude
                                            stallLng = loc.longitude
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("center_gps_pin_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = AmberSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Current GPS", fontSize = 12.sp, color = AmberSecondary)
                            }
                        }

                        Text(
                            text = "Tap on map or drag pin to fine-tune the stall's exact spot",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Location Picker Map
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                        ) {
                            val initialPos = remember { com.google.android.gms.maps.model.LatLng(stallLat, stallLng) }
val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
    position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(initialPos, 16f)
}
val markerState = com.google.maps.android.compose.rememberMarkerState(position = initialPos)

LaunchedEffect(stallLat, stallLng) {
    val target = com.google.android.gms.maps.model.LatLng(stallLat, stallLng)
    if (markerState.position.latitude != stallLat || markerState.position.longitude != stallLng) {
        markerState.position = target
    }
}

LaunchedEffect(markerState.position) {
    val pos = markerState.position
    if (pos.latitude != stallLat || pos.longitude != stallLng) {
        stallLat = pos.latitude
        stallLng = pos.longitude
    }
}

com.google.maps.android.compose.GoogleMap(
    modifier = Modifier
        .fillMaxSize()
        .testTag("location_pin_picker_map"),
    cameraPositionState = cameraPositionState,
    properties = com.google.maps.android.compose.MapProperties(
        mapType = com.google.maps.android.compose.MapType.NORMAL
    ),
    uiSettings = com.google.maps.android.compose.MapUiSettings(
        zoomControlsEnabled = true,
        myLocationButtonEnabled = false
    ),
    onMapClick = { latLng ->
        stallLat = latLng.latitude
        stallLng = latLng.longitude
        markerState.position = latLng
    }
) {
    com.google.maps.android.compose.Marker(
        state = markerState,
        draggable = true
    )
}
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Coordinates: ${String.format("%.5f", stallLat)}, ${String.format("%.5f", stallLng)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Error Message if any
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (vendorName.isBlank()) {
                            errorMessage = "Please enter the vendor/stall name."
                            return@Button
                        }

                        isSubmitting = true
                        val newVendor = Vendor(
                            name = vendorName.trim(),
                            category = if (selectedCategory == "Other" && customCategoryText.isNotBlank()) customCategoryText.trim() else selectedCategory,
                            priceRange = priceRange.trim().ifBlank { "Rs 20 - 50" },
                            latitude = stallLat,
                            longitude = stallLng,
                            addressOrLandmark = landmark.trim(),
                            photoUri = photoUri,
                            isOpen = true,
                            lastStatusUpdateTimestamp = System.currentTimeMillis(),
                            createdAtTimestamp = System.currentTimeMillis(),
                            avgRating = 5.0f,
                            ratingCount = 1
                        )
                        onVendorAdded(newVendor)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_vendor_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Publish Stall to Local Bites",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
