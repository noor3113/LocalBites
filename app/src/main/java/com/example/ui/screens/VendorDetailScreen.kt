package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Review
import com.example.data.local.entity.Vendor
import com.example.data.repository.VendorRepository
import com.example.ui.components.LocalBitesTopBar
import com.example.ui.components.StarRatingBar
import com.example.ui.components.VendorPhotoBanner
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils

@Composable
fun VendorDetailScreen(
    vendorId: Long,
    repository: VendorRepository,
    onNavigateBack: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vendorFlow = remember(vendorId) { repository.getVendorById(vendorId) }
    val vendor by vendorFlow.collectAsStateWithLifecycle(initialValue = null)

    val reviewsFlow = remember(vendorId) { repository.getReviewsForVendor(vendorId) }
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    // New review form state
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewUser by remember { mutableStateOf("") }
    var reviewComment by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LocalBitesTopBar(
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                onOpenAiAssistant = onOpenAiAssistant,
                onOpenAbout = onOpenAbout
            )
        },
        containerColor = CharcoalBackground
    ) { innerPadding ->
        if (vendor == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading stall details...", color = TextSecondary)
            }
            return@Scaffold
        }

        val currentVendor = vendor!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("vendor_detail_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Photo Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(CharcoalSurfaceElevated)
                ) {
                    VendorPhotoBanner(
                        photoUri = currentVendor.photoUri,
                        category = currentVendor.category,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, CharcoalBackground),
                                    startY = 180f
                                )
                            )
                    )

                    // Category Pill on Top Left
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalBackground.copy(alpha = 0.85f))
                            .border(1.dp, AmberPrimary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "${getCategoryEmoji(currentVendor.category)} ${currentVendor.category}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberSecondary
                        )
                    }

                    // Price Range Pill on Top Right
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberContainer.copy(alpha = 0.9f))
                            .border(1.dp, AmberPrimary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = currentVendor.priceRange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Vendor Title & Actions Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = currentVendor.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (currentVendor.addressOrLandmark.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentVendor.addressOrLandmark,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Directions Button
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${currentVendor.latitude},${currentVendor.longitude}?q=${currentVendor.latitude},${currentVendor.longitude}(${Uri.encode(currentVendor.name)})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("get_directions_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get Directions to Thela",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
                        item {
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp)
                        .testTag("delete_vendor_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D1F1F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Delete This Stall",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            if (showDeleteConfirmation) {
                item {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text("Delete Stall?", color = TextPrimary) },
                        text = { Text("This will permanently remove \"${currentVendor.name}\" from Local Bites. This cannot be undone.", color = TextSecondary) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.deleteVendor(currentVendor)
                                        showDeleteConfirmation = false
                                        onNavigateBack()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDeleteConfirmation = false },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceElevated)
                            ) {
                                Text("Cancel", color = TextPrimary)
                            }
                        },
                        containerColor = CharcoalCard
                    )
                }
            }

            // Community "Open Now" Live Toggle Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (currentVendor.isOpen) StatusOpen.copy(alpha = 0.6f) else StatusClosed.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("open_status_toggle_card"),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (currentVendor.isOpen) StatusOpen else StatusClosed)
                                    )
                                    Text(
                                        text = if (currentVendor.isOpen) "Open Now" else "Currently Closed",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentVendor.isOpen) StatusOpen else StatusClosed
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Last updated ${TimeUtils.formatRelativeTime(currentVendor.lastStatusUpdateTimestamp)} by community",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            // Interactive Toggle Switch
                            Switch(
                                checked = currentVendor.isOpen,
                                onCheckedChange = { newOpenState ->
                                    coroutineScope.launch {
                                        repository.updateVendorOpenStatus(currentVendor.id, newOpenState)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StatusOpen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = StatusClosed
                                ),
                                modifier = Modifier.testTag("toggle_open_now_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "💡 Are you here? Tap the switch to update this stall's live open status for the community.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Ratings Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = String.format("%.1f", currentVendor.avgRating),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "/ 5.0",
                                    fontSize = 16.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            StarRatingBar(
                                rating = currentVendor.avgRating.toInt(),
                                starSize = 18.dp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${currentVendor.ratingCount} Review${if (currentVendor.ratingCount == 1) "" else "s"}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "100% Local Community",
                                fontSize = 11.sp,
                                color = AmberSecondary
                            )
                        }
                    }
                }
            }

            // Leave a Rating & Review Form Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .testTag("add_review_card"),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Leave a Rating & Review",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Star selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "Your Rating:", fontSize = 13.sp, color = TextSecondary)
                            StarRatingBar(
                                rating = reviewRating,
                                onRatingChanged = { reviewRating = it },
                                starSize = 26.dp,
                                modifier = Modifier.testTag("interactive_star_rating")
                            )
                        }

                        // User Name (Optional)
                        OutlinedTextField(
                            value = reviewUser,
                            onValueChange = { reviewUser = it },
                            placeholder = { Text("Your Name or Handle (optional)", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_author_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CharcoalCard,
                                unfocusedContainerColor = CharcoalCard
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Comment
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            placeholder = { Text("How was the food? Taste, portion, specialty...", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("review_comment_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberPrimary,
                                unfocusedBorderColor = CharcoalBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = CharcoalCard,
                                unfocusedContainerColor = CharcoalCard
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Submit Button
                        Button(
                            onClick = {
                                if (reviewComment.isNotBlank()) {
                                    isSubmittingReview = true
                                    coroutineScope.launch {
                                        repository.addReview(
                                            vendorId = currentVendor.id,
                                            userName = reviewUser,
                                            rating = reviewRating,
                                            comment = reviewComment
                                        )
                                        reviewComment = ""
                                        isSubmittingReview = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("submit_review_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            shape = RoundedCornerShape(10.dp),
                            enabled = reviewComment.isNotBlank() && !isSubmittingReview
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Post Community Review",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Reviews List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Community Reviews (${reviews.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Reviews List items
            if (reviews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reviews yet. Be the first to review this stall!",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    ReviewItemCard(
                        review = review,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ReviewItemCard(
    review: Review,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = TimeUtils.formatRelativeTime(review.timestamp),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            StarRatingBar(
                rating = review.rating,
                starSize = 14.dp
            )

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = review.comment,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
